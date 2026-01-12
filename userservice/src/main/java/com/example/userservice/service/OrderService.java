package com.example.userservice.service;

import com.example.userservice.dto.OrderResponse;
import com.example.userservice.enums.OrderStatus;
import com.example.userservice.enums.UserRole;
import com.example.userservice.records.OrderCardDto;
import com.example.userservice.records.ReviewRequest;
import com.example.userservice.records.VerifyPaymentRequest;
import com.example.userservice.serviceimp.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.UUID;

public interface OrderService {
    Page<OrderCardDto> getAllUserOrder(Pageable pageable, UUID userId, UserRole role, OrderStatus status);
    OrderResponse getOrderById(UUID orderId);
    void getOrderConfirmed(UUID orderId);
    void markAsShipped(UUID orderId, UUID farmerId);
    void markAsDelivered(UUID orderId, UUID userId);
    void createReview(UUID orderId, ReviewRequest request, CustomUserDetails user);

    // create Order of fixed price
    UUID createOrder(UUID cropId, UUID retailerId);

    //payment process
    Map<String, Object> createPaymentOrder(UUID orderId);
    void verifyAndMarkPaid(VerifyPaymentRequest req);
}
