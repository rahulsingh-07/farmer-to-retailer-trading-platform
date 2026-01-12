package com.example.userservice.service;

import com.example.userservice.dto.BidRequest;
import com.example.userservice.entity.Auction;
import com.example.userservice.records.BidDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface BidService {
    void placeBid(UUID auctionId, BidRequest request, UUID userId);
    boolean validateBid(Auction auction, BigDecimal amount);
    List<BidDto> getActiveBidsByRetailer(UUID retailerId);
}
