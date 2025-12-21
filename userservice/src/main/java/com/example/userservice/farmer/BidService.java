package com.example.userservice.farmer;

import com.example.userservice.dto.BidRequest;
import com.example.userservice.entity.Auction;
import com.example.userservice.entity.Bid;
import com.example.userservice.entity.Users;
import com.example.userservice.exception.AuctionNotFoundException;
import com.example.userservice.exception.InvalidBidException;
import com.example.userservice.exception.UserNotFoundException;
import com.example.userservice.repository.AuctionRepository;
import com.example.userservice.repository.BidRepository;
import com.example.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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
        Users user=userRepository.findById(userId)
                .orElseThrow(()-> new UserNotFoundException("User Not Found"));
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new AuctionNotFoundException("Place Higher bid"));

        if (!validateBid(auction, request.getAmount())) {
            throw new InvalidBidException("Bid must be higher than current highest bid");
        }

        Bid bid = new Bid();
        bid.setAmount(request.getAmount());
        bid.setUser(user);
        bid.setCreatedAt(LocalDateTime.now());
        bid.setAuction(auction);
        bidRepository.save(bid);
        auction.setCurrentHighestBid(request.getAmount());
        auction.setHighestBidderId(user.getId());
        auctionRepository.save(auction);

        return "Bid Place Successfully";
    }

    public boolean validateBid(Auction auction, BigDecimal amount){
        BigDecimal currentHighest = auctionRepository.findHighestBid(auction.getId());
        // Minimum increment rule (e.g., +5 higher)
        BigDecimal minIncrement =currentHighest.add(BigDecimal.valueOf(5));
        return amount.compareTo(minIncrement) > 0;
    }
}
