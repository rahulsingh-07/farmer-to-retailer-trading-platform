package com.example.userservice.records;

import com.example.userservice.enums.AuctionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record BidDto(
        UUID auctionId,
        UUID cropId,
        String cropName,
        String variety,
        BigDecimal currentHighestBid,
        BigDecimal yourBid,
        Double quantity,
        String unit,
        LocalDateTime expiry,
        AuctionStatus status
) {
}
