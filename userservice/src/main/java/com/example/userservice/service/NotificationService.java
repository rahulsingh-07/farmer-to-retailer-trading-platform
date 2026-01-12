package com.example.userservice.service;

import com.example.userservice.dto.NotificationDTO;
import com.example.userservice.dto.NotificationResponse;
import com.example.userservice.entity.Auction;
import com.example.userservice.entity.Crops;
import com.example.userservice.entity.Users;
import com.example.userservice.enums.NotificationType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface NotificationService {
    void createBidUpdateNotification(
            Users receiver,
            Auction auction,
            LocalDateTime time,
            String message,
            NotificationType type
    );
    void deleteNotifications(List<UUID> ids);
    void notifyFarmerAuctionWon(Auction auction, Crops crop, Users farmer, Users retailer, BigDecimal price);
    void notifyWinner(Auction auction,Crops crop,Users farmer,Users retailer,BigDecimal price);
    List<NotificationDTO> getNotifications(UUID receiverId);
    NotificationResponse getNotification(UUID id);
    void readNotifications(List<UUID> ids);
}
