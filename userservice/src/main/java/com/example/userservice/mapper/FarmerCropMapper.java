package com.example.userservice.mapper;

import com.example.userservice.dto.BidResponse;
import com.example.userservice.dto.FarmerCropDetailDto;
import com.example.userservice.entity.Auction;
import com.example.userservice.entity.Bid;
import com.example.userservice.entity.CropImage;
import com.example.userservice.entity.Crops;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FarmerCropMapper {
    public FarmerCropDetailDto toFarmerCropDetailDto(Crops crop, List<Bid> bids) {

        Auction auction = crop.getAuction();

        List<String> imageUrls = crop.getImages() != null
                ? crop.getImages()
                .stream()
                .map(CropImage::getImageUrl)
                .toList()
                : List.of();

        FarmerCropDetailDto.FarmerCropDetailDtoBuilder builder =
                FarmerCropDetailDto.builder()
                        .cropId(crop.getId())
                        .cropName(crop.getCropName())
                        .cropType(crop.getCropType())
                        .category(crop.getCategory())
                        .variety(crop.getVariety())
                        .quantity(crop.getQuantity())
                        .unit(crop.getUnit())
                        .pricePerUnit(crop.getPricePerUnit())
                        .location(crop.getLocation())
                        .harvestDate(crop.getHarvestDate())
                        .description(crop.getDescription())
                        .createdAt(crop.getCreatedAt())
                        .imageUrl(imageUrls)
                        .bids(mapBids(bids));

        // Auction fields ONLY if auction exists
        if (auction != null) {
            builder
                    .auctionId(auction.getId())
                    .auctionStartTime(auction.getStartTime())
                    .auctionEndTime(auction.getEndTime())
                    .currentHighestBid(auction.getCurrentHighestBid())
                    .highestBidderId(auction.getHighestBidderId())
                    .auctionStatus(auction.getStatus());
        }

        return builder.build();
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
                .bidderName(bid.getBidder().getFullName())
                .createdAt(bid.getCreatedAt())
                .build();
    }
}
