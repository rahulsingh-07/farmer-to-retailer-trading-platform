package com.example.userservice.entity;

import com.example.userservice.enums.NotificationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue
    private UUID id;

    // Farmer who receives the notification
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farmer_id")
    private Users farmer;

    // Highest bidder for this notification
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bidder_id")
    private Users bidder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id")
    private Auction auction;

    @Enumerated(EnumType.STRING)
    private NotificationType type; // BID_UPDATE, AUCTION_WON, AUCTION_RUNNING...

    private String title;
    private String message;

    private boolean read = false;
    private LocalDateTime createdAt = LocalDateTime.now();
}
