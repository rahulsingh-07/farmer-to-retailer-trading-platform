package com.example.userservice.entity;

import com.example.userservice.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
public class Order {
    @Id @GeneratedValue private UUID id;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal finalPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crop_id", nullable = true)
    private Crops crop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farmer_id", nullable = true)
    private Users farmer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "retailer_id", nullable = true)
    private Users retailer;
    private String deliveryOtp;
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;
    private String paymentReferenceId;
    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime paymentAt;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;

    @Column(nullable = false)
    private boolean reviewed = false;

    @PrePersist
    private void init() {
        if (orderStatus == null) orderStatus = OrderStatus.PENDING;
        if (createdAt == null) createdAt = LocalDateTime.now();
    }


}
