package com.example.userservice.mapper;

import com.example.userservice.dto.BidResponse;
import com.example.userservice.dto.FarmerCropDetailDto;
import com.example.userservice.entity.Auction;
import com.example.userservice.entity.Bid;
import com.example.userservice.entity.Crops;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FarmerCropMapper {
    public FarmerCropDetailDto toFarmerCropDetailDto(Crops crop, List<Bid> bids) {
        Auction auction = crop.getAuction();

        return FarmerCropDetailDto.builder()
                .cropId(crop.getId())
                .cropName(crop.getCropName())
                .category(crop.getCategory())
                .variety(crop.getVariety())
                .quantity(crop.getQuantity())
                .unit(crop.getUnit())
                .pricePerUnit(crop.getPricePerUnit())
                .location(crop.getLocation())
                .harvestDate(crop.getHarvestDate())
                .description(crop.getDescription())
                .createdAt(crop.getCreatedAt())

                // Auction details
                .auctionId(auction.getId())
                .auctionStartTime(auction.getStartTime() )
                .auctionEndTime( auction.getEndTime())
                .currentHighestBid( auction.getCurrentHighestBid())
                .highestBidderId( auction.getHighestBidderId() )
                .auctionStatus(auction.getStatus())

                // Bids passed from service layer
                .bids(mapBids(bids))
                .build();
    }

    private List<BidResponse> mapBids(List<Bid> bids) {

        return bids.stream()
                .map(this::toBidResponseDto)
                .toList();
    }

    private BidResponse toBidResponseDto(Bid bid) {
        if (bid == null) {
            return null;
        }

        return BidResponse.builder()
                .id(bid.getId())
                .amount(bid.getAmount())
                .bidderName(bid.getUser().getFullName())
                .createdAt(bid.getCreatedAt())
                .build();
    }
}
