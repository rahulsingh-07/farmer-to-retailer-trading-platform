package com.example.userservice.enums;

public enum PaymentStatus {
    PENDING,     // order created, not paid yet
    PAID,        // retailer paid
    FAILED,      // payment attempt failed
    REFUNDED
}
