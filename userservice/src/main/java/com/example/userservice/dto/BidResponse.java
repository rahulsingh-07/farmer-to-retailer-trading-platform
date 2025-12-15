package com.example.userservice.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class BidResponse {
    private UUID id;
    private UUID auctionId;
    private BigDecimal amount;
    private UUID bidderId;
    private LocalDateTime timestamp;
    private BigDecimal currentHighestBid;
    private UUID highestBidderId;
}
