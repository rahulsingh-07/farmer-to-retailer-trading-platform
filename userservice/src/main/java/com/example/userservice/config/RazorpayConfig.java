package com.example.userservice.config;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RazorpayConfig {
    @Value("${razorpay.api.key}")
    private String apiKey;

    @Value("${razorpay.api.secret}")
    private String apiSecret;

    @Bean
    public RazorpayClient razorpayClient() {
        try {
            if (apiKey == null || apiKey.isEmpty() || apiSecret == null || apiSecret.isEmpty()) {
                throw new IllegalStateException("Razorpay API keys missing in application.properties");
            }
            return new RazorpayClient(apiKey, apiSecret);
        } catch (RazorpayException e) {
            throw new IllegalStateException("Invalid Razorpay API keys", e);
        }
    }
}
