package com.example.userservice.dto;

import com.example.userservice.enums.AuctionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FarmerCropDetailDto {

    private UUID cropId;
    private String cropName;
    private String category;
    private String variety;
    private Double quantity;
    private String unit;
    private BigDecimal pricePerUnit;
    private String location;
    private LocalDate harvestDate;
    private String description;
    private LocalDateTime createdAt;

    // Auction details
    private UUID auctionId;
    private LocalDateTime auctionStartTime;
    private LocalDateTime auctionEndTime;
    private BigDecimal currentHighestBid;
    private UUID highestBidderId;
    private AuctionStatus auctionStatus;
    private List<BidResponse> bids;


}
