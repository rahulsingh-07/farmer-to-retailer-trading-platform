package com.example.userservice.farmerService;

import com.example.userservice.entity.Auction;
import com.example.userservice.entity.Crops;
import com.example.userservice.entity.Users;
import com.example.userservice.enums.AuctionStatus;
import com.example.userservice.notification.EmailService;
import com.example.userservice.repository.AuctionRepository;
import com.example.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class AuctionService {
    private final AuctionRepository auctionRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;



    // ✅ Use repository method
    public boolean isAuctionActive(UUID auctionId) {
        return auctionRepository.findActiveAuctionById(auctionId, LocalDateTime.now())
                .isPresent();
    }

    @Scheduled(cron = "0 0 8 * * ?") // Daily 8AM
    public void sendDailyHighestBidNotifications() {
        System.out.println("Scheduler running...");
        LocalDateTime now = LocalDateTime.now();
        List<Auction> activeAuctions = auctionRepository.findActiveAuctionsBefore(now);

        activeAuctions.forEach(auction -> {
            Crops crop = auction.getCrop();
            Users farmer = crop.getUser();
            UUID bidderId = auction.getHighestBidderId();
            System.out.println(" bidder : "+bidderId);
            Users highestBidder = userRepository.findById(bidderId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            BigDecimal highestBid = auction.getCurrentHighestBid();

            notificationService.createBidUpdateNotification(
                    farmer,
                    highestBidder,
                    auction,
                    now,
                    "New Highest Bid",
                    String.format("₹%s for %s (%d days left)",
                            highestBid, crop.getCropName(),
                            ChronoUnit.DAYS.between(now, auction.getEndTime()))
            );


            // 2. Send Gmail (async)
            CompletableFuture.runAsync(() ->
                    emailService.sendDailyBidNotification(
                            farmer.getId(), crop.getCropName(),ChronoUnit.DAYS.between(now, auction.getEndTime()), highestBid)
            );
        });
    }
}
