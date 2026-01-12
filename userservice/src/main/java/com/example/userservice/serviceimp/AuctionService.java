package com.example.userservice.serviceimp;

import com.example.userservice.entity.Auction;
import com.example.userservice.entity.Crops;
import com.example.userservice.entity.Users;
import com.example.userservice.enums.NotificationType;
import com.example.userservice.repository.AuctionRepository;
import com.example.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionService {
    private final AuctionRepository auctionRepository;
    private final EmailServiceImp emailService;
    private final NotificationServiceImp notificationServiceImp;
    private final UserRepository userRepository;


    public boolean isAuctionActive(UUID auctionId) {
        return auctionRepository.findActiveAuctionById(auctionId, LocalDateTime.now())
                .isPresent();
    }

    @Scheduled(cron = "0 0 12 * * ?") // Daily 12AM
    @Transactional
    public void sendDailyHighestBidNotifications() {
        LocalDateTime now = LocalDateTime.now();
        List<Auction> activeAuctions = auctionRepository.findActiveAuctionsBefore(now);

        activeAuctions.forEach(auction -> {
            try {
                //Validate auction data
                Crops crop = auction.getCrop();
                if (crop == null) {
                    log.warn("Skipping auction {} - no crop", auction.getId());
                    return;
                }

                Users farmer = crop.getUser();
                if (farmer == null) {
                    log.warn("Skipping auction {} - no farmer", auction.getId());
                    return;
                }

                UUID bidderId = auction.getHighestBidderId();
                if (bidderId == null) {
                    log.warn("Skipping auction {} - no bidder", auction.getId());
                    return;
                }

                // Get bidder SAFELY
                Users highestBidder = userRepository.findById(bidderId)
                        .orElse(null);
                if (highestBidder == null) {
                    log.warn("Skipping auction {} - bidder {} not found", auction.getId(), bidderId);
                    return;
                }

                BigDecimal highestBid = auction.getCurrentHighestBid() != null
                        ? auction.getCurrentHighestBid()
                        : BigDecimal.ZERO;

                // Create notification
                notificationServiceImp.createBidUpdateNotification(
                        farmer,
                        auction,
                        now,
                        String.format("₹%s for %s (%d days left)",
                                highestBid, crop.getCropName(),
                                ChronoUnit.DAYS.between(now, auction.getEndTime())),
                        NotificationType.BID_UPDATE
                );

                // 4. Send email ASYNC
                CompletableFuture.runAsync(() ->
                        emailService.sendDailyBidNotification(
                                farmer.getId(),
                                crop.getCropName(),
                                ChronoUnit.DAYS.between(now, auction.getEndTime()),
                                highestBid)
                );

            } catch (Exception e) {
                log.error("Failed to process auction notifications for auction {}: {}",
                        auction.getId(), e.getMessage(), e);
                // Continue to next auction - DON'T break batch!
            }
        });
    }
}
