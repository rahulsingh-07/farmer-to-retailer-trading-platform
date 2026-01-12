package com.example.userservice.service;

import java.math.BigDecimal;
import java.util.UUID;

public interface EmailService {

    void sendPasswordSetupEmail(String to, String token, String username, String fullName);
    void sendRejectionNotice(String to,String name);
    void sendReminderEmail(String to,String name);
    void sendDeletionEmail(String to,String name);
    void sendDailyBidNotification(UUID farmerId, String cropName, long dayLeft, BigDecimal highestBid);
    void notifyFarmerAuctionWon(String farmerEmail,String farmerName,String cropName,BigDecimal price,String retailerName);
    void notifyWinner( String retailerEmail,String retailerName,String cropName,BigDecimal price,String farmerName);
    void sendOtp(String retailerEmail,UUID orderId,String otp);
}
