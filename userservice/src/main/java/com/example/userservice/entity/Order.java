package com.example.userservice.entity;

import com.example.userservice.enums.OrderStatus;
import com.example.userservice.enums.PaymentStatus;
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

    @ManyToOne
    @JoinColumn(name = "auction_id")
    private Auction auction;           // Your auction link

    @Column(precision = 10, scale = 2)
    private BigDecimal finalPrice;

    @ManyToOne
    @JoinColumn(name = "crop_id")
    private Crops crop;               // Crop details

    @ManyToOne
    @JoinColumn(name = "farmer_id")
    private Users farmer;             // Crop.user

    @ManyToOne
    @JoinColumn(name = "retailer_id")
    private Users retailer;           // highestBidderId → Users

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime shippedAt;

    @PrePersist
    private void init() {
        if (orderStatus == null) orderStatus = OrderStatus.PENDING;
    }


}
