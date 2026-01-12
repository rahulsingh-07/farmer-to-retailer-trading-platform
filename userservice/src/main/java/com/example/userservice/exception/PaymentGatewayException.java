package com.example.userservice.exception;

import com.razorpay.RazorpayException;

public class PaymentGatewayException extends RuntimeException {
    public PaymentGatewayException(String message, RazorpayException ex) {
        super(message,ex);
    }
}
