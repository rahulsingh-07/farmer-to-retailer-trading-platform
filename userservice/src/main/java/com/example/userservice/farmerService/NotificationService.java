package com.example.userservice.farmerService;

import com.example.userservice.entity.Auction;
import com.example.userservice.entity.Notification;
import com.example.userservice.entity.Users;
import com.example.userservice.enums.NotificationType;
import com.example.userservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void createBidUpdateNotification(
            Users farmer,
            Users bidder,
            Auction auction,
            String title,
            String message
    ) {
        Notification notification = Notification.builder()
                .farmer(farmer)
                .bidder(bidder)
                .auction(auction)
                .title(title)
                .message(message)
                .type(NotificationType.BID_UPDATE)
                .build();
        notificationRepository.save(notification);
    }


}
