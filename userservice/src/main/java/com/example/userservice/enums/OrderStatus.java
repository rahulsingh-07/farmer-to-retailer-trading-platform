package com.example.userservice.enums;

public enum OrderStatus {
    PENDING,           // Auction closed, farmer review
    CONFIRMED,         // Farmer clicked confirm btn
    EMD_PAID,          // 5% deposit received
    SHIPPED,           // Crop dispatched
    DELIVERED,         // Retailer received
    COMPLETED,         // Payment settled
    CANCELLED,         // Any stage cancellation
    DISPUTED           // Quality/delivery issue
}
