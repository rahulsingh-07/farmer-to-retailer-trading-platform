package com.example.userservice.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class BidResponse {
    private UUID id;
    private UUID auctionId;
    private BigDecimal amount;
    private UUID bidderId;
    private String bidderName;
    private LocalDateTime timestamp;
    private BigDecimal currentHighestBid;
    private LocalDateTime createdAt;
    private UUID highestBidderId;
}
