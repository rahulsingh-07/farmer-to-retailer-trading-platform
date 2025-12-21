package com.example.userservice.farmer;

import com.example.userservice.dto.CropImageResponse;
import com.example.userservice.dto.NotificationDTO;
import com.example.userservice.dto.NotificationResponse;
import com.example.userservice.entity.*;
import com.example.userservice.enums.NotificationType;
import com.example.userservice.enums.UserRole;
import com.example.userservice.repository.NotificationRepository;
import com.example.userservice.repository.UserRepository;
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
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public void createBidUpdateNotification(
            UserRole role,
            UUID roleId,
            Users farmer,
            Users bidder,
            Auction auction,
            LocalDateTime time,
            String title,
            String message,
            NotificationType type
    ) {
        Notification notification = Notification.builder()
                .role(role)
                .roleId(roleId)
                .farmer(farmer)
                .bidder(bidder)
                .auction(auction)
                .createdAt(time)
                .title(title)
                .message(message)
                .type(type)
                .build();
        notificationRepository.save(notification);
    }

    public String deleteNotifications(List<UUID> ids){
        notificationRepository.deleteAllById(ids);

        return "notification delete successfully";
    }

    @Transactional
    public void notifyFarmerAuctionWon(Auction auction,Crops crop,Users farmer,Users retailer,BigDecimal price) {

        String title = "Auction Won";
        String message = String.format(
                "Your crop %s has been sold at ₹%s to %s.",
                crop.getCropName(),
                price.toPlainString(),
                retailer.getFullName()
        );

        createBidUpdateNotification(
                UserRole.FARMER,
                farmer.getId(),
                farmer,
                retailer,
                auction,
                LocalDateTime.now(),
                title,
                message,
                NotificationType.AUCTION_WON
        );
        log.info("notifyFarmerAuctionWon END for auction={}", farmer.getId());
    }

    @Transactional
    public void notifyWinner(Auction auction,Crops crop,Users farmer,Users retailer,BigDecimal price) {

            String cropName = crop.getCropName();
            String farmerName = farmer.getFullName();
            String title = "You Won the Auction";
            String message = String.format(
                    "Congratulations! You have won the auction for %s at ₹%s from %s.",
                    cropName,
                    price.toPlainString(),
                    farmerName
            );

            createBidUpdateNotification(
                    UserRole.RETAILER,
                    retailer.getId(),
                    farmer,
                    retailer,
                    auction,
                    LocalDateTime.now(),
                    title,
                    message,
                    NotificationType.AUCTION_WON
            );
        }

    public List<NotificationDTO> getNotifications(UUID userId, UserRole role) {
        List<Notification> notifications= notificationRepository.findByRoleIdAndRoleOrderByCreatedAtDesc(userId,role);
        return notifications.stream()
                .map(notification -> NotificationDTO.builder()
                        .id(notification.getId())
                        .bidderFullName(notification.getBidder().getFullName())
                        .type(notification.getType())
                        .auctionId(notification.getAuction().getId())
                        .createdAt(notification.getCreatedAt())
                        .read(notification.isRead())
                        .build())
                .toList();


    }
    @Transactional
    public NotificationResponse getNotification(UUID id) {
        Notification n=notificationRepository.findNotificationWithDetails(id)
                    .orElseThrow(()->new RuntimeException("Notification not found"));
        notificationRepository.markAsRead(id);

        return NotificationResponse.builder()
                .notificationId(n.getId())
                .message(n.getMessage())
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .type(n.getType())
                .status(n.getAuction().getStatus())
                // Crop
                .cropId(n.getAuction().getCrop().getId())
                .cropName(n.getAuction().getCrop().getCropName())
                .category(n.getAuction().getCrop().getCategory())
                .variety(n.getAuction().getCrop().getVariety())
                .quantity(n.getAuction().getCrop().getQuantity())
                .unit(n.getAuction().getCrop().getUnit())
                .imageUrl(n.getAuction().getCrop().getImages()
                        .stream()
                        .map(img -> {
                            CropImageResponse imgRes = new CropImageResponse();
                            imgRes.setImageUrl(img.getImageUrl());
                            return imgRes;
                            })
                            .toList())

                // Bidder
                .bidderFullName(n.getBidder().getFullName())
                .bidderPhoneNumber(n.getBidder().getPhoneNumber())
                .bidderAddress(n.getBidder().getRetailerDetails().getBusinessAddress())
                .build();
    }

}
