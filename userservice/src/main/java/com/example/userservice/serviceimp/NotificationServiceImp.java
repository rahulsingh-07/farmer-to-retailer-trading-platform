package com.example.userservice.serviceimp;

import com.example.userservice.dto.NotificationDTO;
import com.example.userservice.dto.NotificationResponse;
import com.example.userservice.entity.*;
import com.example.userservice.enums.NotificationType;
import com.example.userservice.repository.NotificationRepository;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImp implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    public void createBidUpdateNotification(Users receiver,Auction auction, LocalDateTime time, String message, NotificationType type) {

        Notification notification = Notification.builder()
                .receiver(receiver)
                .auction(auction)
                .createdAt(time)
                .message(message)
                .type(type)
                .build();
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void deleteNotifications(List<UUID> ids){
        notificationRepository.deleteAllById(ids);
    }

    @Override
    @Transactional
    public void notifyFarmerAuctionWon(Auction auction,Crops crop,Users farmer,Users retailer,BigDecimal price) {

        String message = String.format(
                "Your crop %s has been sold at ₹%s to %s.",
                crop.getCropName(),
                price.toPlainString(),
                retailer.getFullName()
        );

        createBidUpdateNotification(
                farmer,
                auction,
                LocalDateTime.now(),
                message,
                NotificationType.AUCTION_WON
        );
        log.info("notifyFarmerAuctionWon END for auction={}", farmer.getId());
    }

    @Override
    @Transactional
    public void notifyWinner(Auction auction,Crops crop,Users farmer,Users retailer,BigDecimal price) {

        String cropName = crop.getCropName();
        String farmerName = farmer.getFullName();
        String message = String.format(
                "Congratulations! You have won the auction for %s at ₹%s from %s.",
                cropName,
                price.toPlainString(),
                farmerName
            );

        createBidUpdateNotification(
                retailer,
                auction,
                LocalDateTime.now(),
                message,
                NotificationType.AUCTION_WON
            );
    }

    @Override
    public List<NotificationDTO> getNotifications(UUID userId) {
        List<Notification> notifications= notificationRepository.findByReceiverId(userId);
        return notifications.stream()
                .map(notification -> NotificationDTO.builder()
                        .id(notification.getId())
                        .type(notification.getType())
                        .auctionId(notification.getAuction().getId())
                        .createdAt(notification.getCreatedAt())
                        .read(notification.isRead())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public NotificationResponse getNotification(UUID id) {
        Notification n = notificationRepository.findNotificationWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        n.setRead(true);
        Auction a = n.getAuction();
        Crops c = a != null ? a.getCrop() : null;
        assert c != null;
        return NotificationResponse.builder()
                .notificationId(n.getId())
                .message(n.getMessage())
                .read(true)
                .createdAt(n.getCreatedAt())
                .type(n.getType())
                .status(a.getStatus())
                .cropName(c.getCropName())
                .category(c.getCategory())
                .variety(c.getVariety())
                .quantity(c.getQuantity())
                .unit(c.getUnit())
                .imageUrl(c.getImages().stream()
                        .map(CropImage::getImageUrl)
                        .toList())
                .build();

    }

    @Override
    @Transactional
    public void readNotifications(List<UUID> ids) {
        notificationRepository.markAsRead(ids);
    }

}
