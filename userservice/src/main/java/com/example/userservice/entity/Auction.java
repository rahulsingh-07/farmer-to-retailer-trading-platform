package com.example.userservice.entity;

import com.example.userservice.enums.AuctionStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(indexes={@Index(name = "idx_auction_crop_id",columnList = "crop_id")})
public class Auction {

    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne
    @JoinColumn(name = "crop_id", nullable = false)
    @JsonBackReference
    private Crops crop;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Column(precision = 10, scale = 2)
    private BigDecimal currentHighestBid = BigDecimal.ZERO;

    private UUID highestBidderId;

    @Enumerated(EnumType.STRING)
    private AuctionStatus status = AuctionStatus.ACTIVE;
}
