package com.example.userservice.enums;

public enum OrderStatus {
    PENDING,           // Auction closed, farmer review
    CONFIRMED,         // Order confirmed by Farmer
    PAYMENT_PENDING,   // Payment pending
    PAID,              // Paid
    SHIPPED,           // Crop dispatched
    DELIVERED,         // Retailer received
}
