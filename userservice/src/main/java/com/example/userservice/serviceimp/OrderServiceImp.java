package com.example.userservice.serviceimp;

import com.example.userservice.dto.OrderResponse;
import com.example.userservice.entity.Crops;
import com.example.userservice.entity.Order;
import com.example.userservice.entity.Review;
import com.example.userservice.entity.Users;
import com.example.userservice.enums.CropAvailability;
import com.example.userservice.enums.CropType;
import com.example.userservice.enums.OrderStatus;
import com.example.userservice.enums.UserRole;
import com.example.userservice.exception.CropNotFoundException;
import com.example.userservice.exception.OrderNotFoundException;
import com.example.userservice.exception.PaymentGatewayException;
import com.example.userservice.exception.UserNotFoundException;
import com.example.userservice.mapper.OrderMapper;
import com.example.userservice.records.OrderCardDto;
import com.example.userservice.records.ReviewRequest;
import com.example.userservice.records.VerifyPaymentRequest;
import com.example.userservice.repository.CropRepository;
import com.example.userservice.repository.OrderRepository;
import com.example.userservice.repository.ReviewRepository;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.OrderService;
import com.example.userservice.util.OtpUtil;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImp implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final CropRepository cropRepository;
    private final UserRepository userRepository;
    private final RazorpayService razorpayService;
    private final EmailServiceImp emailServiceImp;
    private final ReviewRepository reviewRepository;
    private final InvoiceService invoiceService;


    @Override
    public Page<OrderCardDto> getAllUserOrder(Pageable pageable,UUID userId,UserRole role,OrderStatus status) {
        Page<Order> page;
        switch (role) {
            case FARMER -> page = orderRepository.findFarmerOrders(pageable, userId, status);
            case RETAILER -> page = orderRepository.findRetailerOrders(pageable, userId, status);
            default -> throw new IllegalStateException("Unsupported role: " + role);
        }
        return page.map(orderMapper::toResponseDto);
    }

    @Override
    public OrderResponse getOrderById(UUID orderId){
        Order order=orderRepository.findById(orderId)
                .orElseThrow(()->new RuntimeException("Order Not Found"));
        Review review = reviewRepository
                .findByOrderId(orderId)
                .orElse(null);
        return orderMapper.toDto(order,review);

    }

    @Override
    public void getOrderConfirmed(UUID orderId) {
        Order order=orderRepository.findById(orderId)
                .orElseThrow(()->new OrderNotFoundException("Order Not Found"));
        order.setOrderStatus(OrderStatus.CONFIRMED);
        order.setConfirmedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    @Override
    public void markAsShipped(UUID orderId, UUID farmerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found for Shipping"));
        //Status validation
        if (order.getOrderStatus() != OrderStatus.PAID) {
            throw new IllegalStateException("Only PAID orders can be shipped");
        }
        if (!order.getFarmer().getId().equals(farmerId)) {
            throw new SecurityException("You are not allowed to ship this order");
        }
        String otp = OtpUtil.generateOtp();
        order.setDeliveryOtp(otp);
        order.setOrderStatus(OrderStatus.SHIPPED);
        order.setShippedAt(LocalDateTime.now());
        orderRepository.save(order);
        emailServiceImp.sendOtp(order.getRetailer().getEmail(), order.getId(), otp);
    }

    @Override
    public void markAsDelivered(UUID orderId, UUID userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found for Delivering"));
        if (order.getOrderStatus() != OrderStatus.SHIPPED) {
            throw new IllegalStateException("Only shipped Order can Delivered");
        }
        if (!order.getFarmer().getId().equals(userId)) {
            throw new SecurityException("You are not allowed to Confirm this order");
        }
        order.setDeliveredAt(LocalDateTime.now());
        order.setOrderStatus(OrderStatus.DELIVERED);
        orderRepository.save(order);
    }

    @Override
    public void createReview(UUID orderId, ReviewRequest request, CustomUserDetails user) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found for rating"));

        if (order.getOrderStatus() != OrderStatus.DELIVERED) {
            throw new IllegalStateException("Review allowed only after delivery");
        }
        if (!order.getRetailer().getId().equals(user.getUserId())) {
            throw new SecurityException("You are not allowed to review this order");
        }
        boolean alreadyReviewed = reviewRepository.existsByOrderIdAndReviewerId(orderId, user.getUserId());

        if (alreadyReviewed) {
            throw new IllegalStateException("You have already reviewed this order");
        }

        Review review = new Review();
        review.setOrder(order);
        review.setReviewer(order.getRetailer());
        review.setFarmer(order.getFarmer());
        review.setRating(request.rating());
        review.setComment(request.comment());
        reviewRepository.save(review);
        order.setReviewed(true);
    }


    @Override
    @Transactional
    public UUID createOrder(UUID cropId, UUID retailerId) {

        Crops crop = cropRepository.findById(cropId)
                .orElseThrow(() -> new CropNotFoundException("Crop not found"));

        Users retailer = userRepository.findById(retailerId)
                .orElseThrow(() -> new UserNotFoundException("Retailer not found"));

        Optional<Order> existingOrder =orderRepository.findActiveOrder(cropId,retailerId,OrderStatus.CONFIRMED);

        if (existingOrder.isPresent()) {
            return existingOrder.get().getId();
        }

        Users farmer = crop.getUser();
        LocalDateTime now = LocalDateTime.now();

        Order order = new Order();
        order.setFinalPrice(crop.getPricePerUnit());
        order.setOrderStatus(OrderStatus.CONFIRMED);
        order.setCrop(crop);
        order.setFarmer(farmer);
        order.setRetailer(retailer);
        order.setCreatedAt(now);
        order.setConfirmedAt(now);

        orderRepository.save(order);

        return order.getId();
    }


    // Handle payment ------------------------------------------------//
    @Override
    @Transactional
    public Map<String, Object> createPaymentOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        if (order.getOrderStatus() == OrderStatus.PAID) {
            throw new IllegalStateException("Order already paid");
        }
        if (order.getCrop() == null) {
            throw new IllegalStateException("Order has no crop");
        }
        if (order.getFinalPrice() == null ||
                order.getFinalPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Invalid order amount");
        }
        int amountInPaise = order.getFinalPrice()
                .multiply(BigDecimal.valueOf(order.getCrop().getQuantity()))
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .intValueExact();
        try {
            Map<String, Object> rpOrder = razorpayService.createOrder(amountInPaise);
            order.setOrderStatus(OrderStatus.PAYMENT_PENDING);
            order.getCrop().setAvailability(CropAvailability.RESERVED);
            return rpOrder;
        } catch (RazorpayException ex) {
            if(order.getCrop().getCropType()== CropType.FIXED){
                order.getCrop().setAvailability(CropAvailability.AVAILABLE);
                orderRepository.delete(order);
            }else{
                order.setOrderStatus(OrderStatus.CONFIRMED);
            }
            throw new PaymentGatewayException("Payment gateway error", ex);
        }
    }

    @Override
    @Transactional
    public void verifyAndMarkPaid(VerifyPaymentRequest req) {

        boolean valid = razorpayService.verifyPayment(
                req.razorpayOrderId(),
                req.razorpayPaymentId(),
                req.razorpaySignature()
        );
        if (!valid) {
            handlePaymentFailure(req.orderId());
            throw new IllegalStateException("Invalid payment");
        }
        Order order = orderRepository.findById(req.orderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));
        if (order.getOrderStatus() == OrderStatus.PAID) {
            return;
        }
        order.setOrderStatus(OrderStatus.PAID);
        order.setPaymentAt(LocalDateTime.now());
        order.getCrop().setAvailability(CropAvailability.SOLD);
        // Generate invoice PDF
        byte[] pdf = invoiceService.generateInvoicePdf(order.getId());
        // Send email
        emailServiceImp.sendInvoiceEmail(
                order.getRetailer().getEmail(),
                order.getRetailer().getFullName(),
                pdf,
                "INV-" + order.getId()
        );
        orderRepository.save(order);
    }

    private void handlePaymentFailure(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow();
        if (order.getOrderStatus() == OrderStatus.PAID) return;
        if(order.getCrop().getCropType()== CropType.FIXED){
            order.getCrop().setAvailability(CropAvailability.AVAILABLE);
            orderRepository.delete(order);
        }else{
            order.setOrderStatus(OrderStatus.CONFIRMED);
        }
    }


}
