package com.example.userservice.util;

import com.example.userservice.entity.Auction;
import com.example.userservice.entity.Crops;
import com.example.userservice.entity.Order;
import com.example.userservice.entity.Users;
import com.example.userservice.enums.OrderStatus;
import com.example.userservice.enums.PaymentStatus;
import com.example.userservice.farmer.NotificationService;
import com.example.userservice.notification.EmailService;
import com.example.userservice.repository.OrderRepository;
import com.example.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuctionOrderListener {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository; // For highestBidderId
    private final EmailService emailService;
    private final NotificationService notificationService;

    @EventListener
    @Transactional
    public void autoCreateOrder(AuctionClosedEvent event) {
        Auction auction = event.getAuction();

        //YOUR EXISTING DATA PATH
        Order order = Order.builder()
                .auction(auction)
                .crop(auction.getCrop())
                .farmer(auction.getCrop().getUser())
                .retailer(getRetailerById(auction.getHighestBidderId()))
                .finalPrice(auction.getCurrentHighestBid())
                .orderStatus(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        orderRepository.save(order);
        String farmerEmail=auction.getCrop().getUser().getEmail();
        String farmerName=auction.getCrop().getUser().getFullName();
        String cropName   = order.getAuction().getCrop().getCropName();
        BigDecimal price  = order.getFinalPrice();
        String retailerName = order.getRetailer().getFullName();
        String retailerEmail=order.getRetailer().getEmail();

        notificationService.notifyFarmerAuctionWon(order.getAuction(),order.getCrop(),order.getFarmer(),order.getRetailer(),price);
        notificationService.notifyWinner(order.getAuction(),order.getCrop(),order.getFarmer(),order.getRetailer(),price);


        log.info("Calling emailService.notifyFarmerAuctionWon for farmerId");
        emailService.notifyFarmerAuctionWon(farmerEmail,farmerName,cropName,price,retailerName);

        log.info("Calling emailService.notifyWinner for retailerId={}",getRetailerById(auction.getHighestBidderId()).getId());
        emailService.notifyWinner(retailerEmail,retailerName,cropName,price,farmerName);
        log.info("autoCreateOrder END auctionId");
    }

    private Users getRetailerById(UUID retailerId) {
        return userRepository.findById(retailerId)
                .orElseThrow(() -> new RuntimeException("Winner not found"));
    }
}

