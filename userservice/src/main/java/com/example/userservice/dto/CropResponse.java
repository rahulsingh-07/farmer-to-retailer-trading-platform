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
    private BigDecimal pricePerUnit;
    private LocalDate harvestDate;
    private List<CropImageResponse> imageUrl;
    private String farmerName;

    private BigDecimal currentHighestBid;
    private UUID auctionId;
    private Long daysLeft;
    private String auctionStatus;

}
