package com.example.userservice.mapper;

import com.example.userservice.dto.CropRequest;
import com.example.userservice.entity.Auction;
import com.example.userservice.entity.Crops;
import com.example.userservice.entity.Users;
import com.example.userservice.enums.AuctionStatus;
import com.example.userservice.enums.CropAvailability;
import com.example.userservice.enums.CropType;
import com.example.userservice.records.CropCardDto;
import com.example.userservice.repository.AuctionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CropMapper {
    private final AuctionRepository auctionRepository;

    public Crops toEntity(CropRequest request, Users user) {
        Crops crops = new Crops();
        crops.setCropName(request.getCropName().trim());
        crops.setCropType(request.getCropType());
        crops.setAvailability(CropAvailability.AVAILABLE);
        crops.setCategory(request.getCategory().trim());
        crops.setVariety(request.getVariety().trim());
        crops.setQuantity(request.getQuantity());
        crops.setUnit(request.getUnit().trim());
        crops.setPricePerUnit(request.getPricePerUnit());
        crops.setLocation(request.getLocation().trim());
        crops.setHarvestDate(request.getHarvestDate());
        crops.setDescription(request.getDescription() != null ?
                request.getDescription().trim() : null);
        crops.setUser(user);
        crops.setCreatedAt(LocalDateTime.now());

        if (request.getCropType() == CropType.AUCTION) {
            Auction auction = new Auction();
            auction.setCrop(crops);
            auction.setStartTime(LocalDateTime.now());
            auction.setEndTime(LocalDateTime.now().plusDays(7));
            auction.setStatus(AuctionStatus.ACTIVE);
            auction.setCurrentHighestBid(BigDecimal.ZERO);
            crops.setAuction(auction);
        }
        return crops;
    }

    public CropRequest toDto(Crops crops) {
        CropRequest request = new CropRequest();
        request.setCropName(crops.getCropName());
        request.setCropType(crops.getCropType());
        request.setCategory(crops.getCategory());
        request.setVariety(crops.getVariety());
        request.setQuantity(crops.getQuantity());
        request.setUnit(crops.getUnit());
        request.setPricePerUnit(crops.getPricePerUnit());
        request.setLocation(crops.getLocation());
        request.setHarvestDate(crops.getHarvestDate());
        request.setDescription(crops.getDescription());
        return request;
    }

    public CropCardDto toCard(Crops crop) {
        BigDecimal currentHighestBid = null;
        LocalDateTime auctionEndTime = null;

        if (crop.getCropType() == CropType.AUCTION && crop.getAuction() != null) {
            currentHighestBid = crop.getAuction().getCurrentHighestBid();
            auctionEndTime = crop.getAuction().getEndTime();
        }

        return new CropCardDto(
                crop.getId(),
                crop.getCropName(),
                crop.getCropType(),
                crop.getVariety(),
                crop.getQuantity(),
                crop.getUnit(),
                crop.getLocation(),
                crop.getPricePerUnit(),
                crop.getHarvestDate(),
                currentHighestBid,
                auctionEndTime,
                crop.getImages().get(0)
        );
    }

    // For Page<Crops> → Page<CropCardDto>
    public Page<CropCardDto> toCard(Page<Crops> cropsPage) {
        return cropsPage.map(this::toCard);
    }

}