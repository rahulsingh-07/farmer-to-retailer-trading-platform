package com.example.userservice.mapper;

import com.example.userservice.dto.CropImageResponse;
import com.example.userservice.dto.CropRequest;
import com.example.userservice.dto.CropResponse;
import com.example.userservice.entity.Auction;
import com.example.userservice.entity.Crops;
import com.example.userservice.entity.Users;
import com.example.userservice.enums.AuctionStatus;
import com.example.userservice.repository.AuctionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CropMapper {
    private final AuctionRepository auctionRepository;

    public Crops toEntity(CropRequest request, Users user) {
        Crops crops = new Crops();
        crops.setCropName(request.getCropName().trim());
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

        Auction auction = new Auction();
        auction.setCrop(crops);
        auction.setStartTime(LocalDateTime.now());
        auction.setEndTime(LocalDateTime.now().plusMinutes(5)); // example
        auction.setStatus(AuctionStatus.ACTIVE);
        auction.setCurrentHighestBid(BigDecimal.ZERO);

        crops.setAuction(auction);
        return crops;
    }

    public CropRequest toDto(Crops crops) {
        CropRequest request = new CropRequest();
        request.setCropName(crops.getCropName());
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

    public CropResponse toResponse(Crops crop) {
        Auction auction = auctionRepository.findByCropId(crop.getId())
                .orElseThrow(() -> new IllegalStateException("Auction not found"));

        List<CropImageResponse> imageResponses = crop.getImages()
                .stream()
                .map(img -> {
                    CropImageResponse imgRes = new CropImageResponse();
                    imgRes.setImageUrl(img.getImageUrl());
                    return imgRes;
                })
                .toList();

        return CropResponse.builder()
                .id(crop.getId())
                .cropName(crop.getCropName())
                .category(crop.getCategory())
                .variety(crop.getVariety())
                .quantity(crop.getQuantity())
                .unit(crop.getUnit())
                .location(crop.getLocation())
                .pricePerUnit(crop.getPricePerUnit())
                .description(crop.getDescription())
                .harvestDate(crop.getHarvestDate())
                .currentHighestBid(auction.getCurrentHighestBid())
                .auctionId(auction.getId())
                .daysLeft(calculateDaysLeft(auction.getEndTime()))
                .auctionStatus(auction.getStatus().name())

                .imageUrl(imageResponses)
                .farmerName(crop.getUser().getFullName()) // Assuming Users has fullName
                .build();
    }

    private Long calculateDaysLeft(LocalDateTime endTime) {
        return ChronoUnit.DAYS.between(LocalDateTime.now(), endTime);
    }

    // For Page<Crops> → Page<CropResponse>
    public Page<CropResponse> toResponsePage(Page<Crops> cropsPage) {
        return cropsPage.map(this::toResponse);
    }

    public List<CropResponse> toResponse(List<Crops> crops) {
        return crops.stream()
                .map(this::toResponse)
                .toList();
    }

}
