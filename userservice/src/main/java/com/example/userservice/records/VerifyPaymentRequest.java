package com.example.userservice.records;

import java.util.UUID;

public record VerifyPaymentRequest(
        String razorpayOrderId,
        String razorpayPaymentId,
        String razorpaySignature,
        UUID orderId
) {}
