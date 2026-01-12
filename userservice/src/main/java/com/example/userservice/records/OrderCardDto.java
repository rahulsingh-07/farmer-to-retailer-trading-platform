package com.example.userservice.records;


import com.example.userservice.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderCardDto(
        UUID orderId,
        String farmerName,
        String retailerName,
        OrderStatus status,
        String cropName,
        LocalDateTime createAt,
        String variety,
        Double quantity,
        String imageUrl
) {}
