package com.example.userservice.dto;

import com.example.userservice.enums.CropType;
import com.example.userservice.enums.OrderStatus;
import com.example.userservice.records.ReviewDto;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class OrderResponse {
    private UUID orderId;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime paymentAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime shippedAt;
    private ReviewDto review;

    //farmer details
    private String farmerName;
    private String farmerEmail;
    private String farmerPhoneNumber;
    private String farmerAddress;

    //retailer details
    private String retailerName;
    private String retailerEmail;
    private String retailerPhoneNumber;
    private String retailerAddress;

    //crop details
    private String cropName;
    private String category;
    private CropType cropType;
    private String variety;
    private Double quantity;
    private String unit;
    private String location;
    private String description;
    private BigDecimal pricePerUnit;        // Base price
    private LocalDate harvestDate;
    private List<String> imageUrl;
    private BigDecimal finalPrice;
    private UUID auctionId;

}
