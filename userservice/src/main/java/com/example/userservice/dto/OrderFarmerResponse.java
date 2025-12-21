package com.example.userservice.dto;

import com.example.userservice.entity.Crops;
import com.example.userservice.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class OrderFarmerResponse {
    private UUID orderId;
    private String retailerName;
    private OrderStatus status;
    private String cropName;
    private String category;
    private String variety;
    private Double quantity;
    private List<CropImageResponse> imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime shippedAt;
}
