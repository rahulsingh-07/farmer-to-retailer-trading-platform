package com.example.userservice.farmerService;

import com.example.userservice.entity.Auction;
import com.example.userservice.entity.Notification;
import com.example.userservice.entity.Users;
import com.example.userservice.enums.NotificationType;
import com.example.userservice.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void createBidUpdateNotification_shouldSaveNotificationWithCorrectFields() {
        // Given
        Users farmer = new Users(); farmer.setId(UUID.randomUUID());
        Users bidder = new Users(); bidder.setId(UUID.randomUUID());
        Auction auction = new Auction(); auction.setId(UUID.randomUUID());
        String title = "New Bid";
        String message = "Bid increased to $100";

        // When
        notificationService.createBidUpdateNotification(farmer, bidder, auction, title, message);

        // Then
        verify(notificationRepository, times(1)).save(any(Notification.class));

        verify(notificationRepository).save(argThat(notification ->
                notification.getFarmer().getId().equals(farmer.getId()) &&
                        notification.getBidder().getId().equals(bidder.getId()) &&
                        notification.getAuction().getId().equals(auction.getId()) &&
                        notification.getTitle().equals(title) &&
                        notification.getMessage().equals(message) &&
                        notification.getType() == NotificationType.BID_UPDATE
        ));
    }

    @Test
    void createBidUpdateNotification_shouldSetBidUpdateType() {
        // Given
        Users farmer = new Users();
        Users bidder = new Users();
        Auction auction = new Auction();
        String title = "Test Title";
        String message = "Test Message";

        // When
        notificationService.createBidUpdateNotification(farmer, bidder, auction, title, message);

        // Then
        verify(notificationRepository).save(argThat(n ->
                n.getType() == NotificationType.BID_UPDATE
        ));
    }
}
