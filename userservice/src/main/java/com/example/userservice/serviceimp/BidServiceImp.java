package com.example.userservice.serviceimp;

import com.example.userservice.dto.BidRequest;
import com.example.userservice.entity.Auction;
import com.example.userservice.entity.Bid;
import com.example.userservice.entity.Users;
import com.example.userservice.enums.AuctionStatus;
import com.example.userservice.exception.AuctionNotFoundException;
import com.example.userservice.exception.InvalidBidException;
import com.example.userservice.exception.UserNotFoundException;
import com.example.userservice.records.BidDto;
import com.example.userservice.repository.AuctionRepository;
import com.example.userservice.repository.BidRepository;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.BidService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BidServiceImp implements BidService {
    private final BidRepository bidRepository;
    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;

    private static final BigDecimal MIN_INCREMENT = BigDecimal.valueOf(5);

    @Override
    @Transactional
    public void placeBid(UUID auctionId, BidRequest request, UUID userId) {
        Users user=userRepository.findById(userId)
                .orElseThrow(()-> new UserNotFoundException("User Not Found"));
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new AuctionNotFoundException("Place Higher bid"));
        if (auction.getStatus() != AuctionStatus.ACTIVE) {
            throw new InvalidBidException("Auction is not active");
        }
        if (!validateBid(auction, request.getAmount())) {
            throw new InvalidBidException("Bid must be higher than current highest bid");
        }

        Bid bid = new Bid();
        bid.setAmount(request.getAmount());
        bid.setBidder(user);
        bid.setCreatedAt(LocalDateTime.now());
        bid.setAuction(auction);
        bidRepository.save(bid);
        auction.setCurrentHighestBid(request.getAmount());
        auction.setHighestBidderId(user.getId());
        auctionRepository.save(auction);

    }

    @Override
    public boolean validateBid(Auction auction, BigDecimal amount) {
        BigDecimal effectiveCurrent =
                Optional.ofNullable(auctionRepository.findHighestBid(auction.getId()))
                        .orElse(auction.getCrop().getPricePerUnit());

        return amount.compareTo(effectiveCurrent.add(MIN_INCREMENT)) > 0;
    }


    @Override
    public List<BidDto> getActiveBidsByRetailer(UUID retailerId) {
        List<Bid> bids=bidRepository.findActiveAllByUserId(retailerId);
        return bids
                .stream()
                .map(this::toBidDto)
                .toList();
    }

    private BidDto toBidDto(Bid bid) {
        return new BidDto(
                bid.getId(),
                bid.getAuction().getCrop().getId(),
                bid.getAuction().getCrop().getCropName(),
                bid.getAuction().getCrop().getVariety(),
                bid.getAuction().getCurrentHighestBid(),
                bid.getAmount(),
                bid.getAuction().getCrop().getQuantity(),
                bid.getAuction().getCrop().getUnit(),
                bid.getAuction().getEndTime(),
                bid.getAuction().getStatus()
        );
    }
}
