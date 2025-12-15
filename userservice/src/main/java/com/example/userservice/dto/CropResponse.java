package com.example.userservice.dto;


import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CropResponse {

    private UUID id;
    private String cropName;
    private String category;
    private String variety;
    private Double quantity;
    private String unit;
    private String location;
    private String description;
    private BigDecimal pricePerUnit;        // Base price
    private LocalDate harvestDate;
    private List<CropImageResponse> imageUrl;                // First image
    private String farmerName;

    private BigDecimal currentHighestBid;   // Auction highest bid
    private UUID auctionId;
    private Long daysLeft;
    private String auctionStatus;

}
