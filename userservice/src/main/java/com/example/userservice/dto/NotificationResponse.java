package com.example.userservice.dto;

import com.example.userservice.enums.AuctionStatus;
import com.example.userservice.enums.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class NotificationResponse {

    private UUID notificationId;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;
    private NotificationType type;
    private AuctionStatus status;
    //crop details
    private String cropName;
    private String category;
    private String variety;
    private Double quantity;
    private String unit;
    private List<String> imageUrl;
}
