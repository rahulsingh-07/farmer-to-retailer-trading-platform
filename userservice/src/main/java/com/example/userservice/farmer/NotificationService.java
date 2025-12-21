package com.example.userservice.farmerService;

import com.example.userservice.entity.Auction;
import com.example.userservice.entity.Notification;
import com.example.userservice.entity.Users;
import com.example.userservice.enums.NotificationType;
import com.example.userservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void createBidUpdateNotification(
            Users farmer,
            Users bidder,
            Auction auction,
            LocalDateTime time,
            String title,
            String message
    ) {
        Notification notification = Notification.builder()
                .farmer(farmer)
                .bidder(bidder)
                .auction(auction)
                .createdAt(time)
                .title(title)
                .message(message)
                .type(NotificationType.BID_UPDATE)
                .build();
        notificationRepository.save(notification);
    }

    public String deleteNotifications(List<UUID> ids){
        notificationRepository.deleteAllById(ids);

        return "notification delete successfully";
    }


}
