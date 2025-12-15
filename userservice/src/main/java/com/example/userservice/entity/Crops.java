package com.example.userservice.entity;

import com.example.userservice.enums.AuctionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Crops {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String cropName;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String variety;

    @Column(nullable = false)
    private Double quantity;

    @Column(nullable = false)
    private String unit;

    @Column(nullable = false)
    private BigDecimal pricePerUnit;

    @Column(nullable = false)
    private String location;

    @Column(name = "harvest_date")
    private LocalDate harvestDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    // ✅ NEW: One-to-One Auction relationship
    @OneToOne(mappedBy = "crop", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Auction auction;

    private LocalDateTime createdAt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farmer_id", nullable = false)
    private Users user;

    @OneToMany(mappedBy = "crop", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CropImage> images=new ArrayList<>();
}
