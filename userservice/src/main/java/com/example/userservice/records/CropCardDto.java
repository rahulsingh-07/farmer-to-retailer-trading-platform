package com.example.userservice.records;

import com.example.userservice.entity.CropImage;
import com.example.userservice.enums.CropType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record CropCardDto(
        UUID id,
        String cropName,
        CropType cropType,
        String variety,
        Double quantity,
        String unit,
        String location,
        BigDecimal pricePerUnit,
        LocalDate harvestDate,
        BigDecimal currentHighestBid,
        LocalDateTime timeLeft,
        CropImage imageUrl
) {
}
