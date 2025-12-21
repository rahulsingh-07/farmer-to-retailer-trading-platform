package com.example.userservice.dto;


import com.example.userservice.enums.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class NotificationDTO {
    private UUID id;
    private String bidderFullName;
    private NotificationType type;
    private UUID auctionId;
    private LocalDateTime createdAt;
    private boolean read;
}
