package com.example.userservice.farmerService;

import com.example.userservice.dto.BidRequest;
import com.example.userservice.entity.Auction;
import com.example.userservice.entity.Bid;
import com.example.userservice.entity.Users;
import com.example.userservice.enums.AuctionStatus;
import com.example.userservice.exception.AuctionNotFoundException;
import com.example.userservice.exception.InvalidBidException;
import com.example.userservice.exception.UserNotFoundException;
import com.example.userservice.repository.AuctionRepository;
import com.example.userservice.repository.BidRepository;
import com.example.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BidService {
    private final BidRepository bidRepository;
    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;

    @Transactional
    public String placeBid(UUID auctionId, BidRequest request, UUID userId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new AuctionNotFoundException("Place Higher bid"));

        if (!validateBid(auction, request.getAmount())) {
            throw new InvalidBidException("Bid must be higher than current highest bid");
        }
        Bid bid = new Bid();
        bid.setAmount(request.getAmount());
        bid.setBidderId(userId);
        bid.setCreatedAt(LocalDateTime.now());
        bid.setAuction(auction);
        bidRepository.save(bid);
        auction.setCurrentHighestBid(request.getAmount());
        auction.setHighestBidderId(userId);
        auctionRepository.save(auction);

        return "Bid Place Successfully";
    }

    public boolean validateBid(Auction auction, BigDecimal amount){


        BigDecimal currentHighest = auctionRepository.findHighestBid(auction.getId());

        // Minimum increment rule (e.g., 5% higher)
        BigDecimal minIncrement = currentHighest.multiply(BigDecimal.valueOf(0.05)).add(currentHighest);

        return amount.compareTo(minIncrement) > 0;
    }
}
