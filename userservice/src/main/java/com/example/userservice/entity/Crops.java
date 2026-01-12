package com.example.userservice.entity;

import com.example.userservice.enums.CropAvailability;
import com.example.userservice.enums.CropType;

import com.fasterxml.jackson.annotation.JsonManagedReference;
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

    @Enumerated(EnumType.STRING)
    private CropType cropType;

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

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CropAvailability availability;

    @OneToOne(mappedBy = "crop", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonManagedReference
    private Auction auction;

    private LocalDateTime createdAt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farmer_id", nullable = false)
    private Users user;

    @OneToMany(mappedBy = "crop", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<CropImage> images=new ArrayList<>();

}
