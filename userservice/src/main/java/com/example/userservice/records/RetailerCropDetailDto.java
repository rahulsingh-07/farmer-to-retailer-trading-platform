package com.example.userservice.records;
import com.example.userservice.enums.CropType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record RetailerCropDetailDto(
        UUID cropId,
        String cropName,
        CropType cropType,
        String category,
        String variety,
        Double quantity,
        String unit,
        BigDecimal pricePerUnit,
        String location,
        LocalDate harvestDate,
        String description,
        List<String> imageUrl,
        UUID auctionId,
        LocalDateTime auctionEndTime,
        BigDecimal currentHighestBid
) {
}
