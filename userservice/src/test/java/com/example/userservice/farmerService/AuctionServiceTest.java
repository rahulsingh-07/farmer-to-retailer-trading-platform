package com.example.userservice.farmerService;

import com.example.userservice.entity.Auction;
import com.example.userservice.entity.Crops;
import com.example.userservice.entity.Users;
import com.example.userservice.notification.EmailService;
import com.example.userservice.repository.AuctionRepository;
import com.example.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuctionServiceTest {

    @Mock
    private AuctionRepository auctionRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuctionService auctionService;

    @Test
    void isAuctionActive_shouldReturnTrue_whenRepositoryFindsActiveAuction() {
        UUID auctionId = UUID.randomUUID();
        Auction auction = new Auction();

        when(auctionRepository.findActiveAuctionById(eq(auctionId), any(LocalDateTime.class)))
                .thenReturn(Optional.of(auction));

        boolean result = auctionService.isAuctionActive(auctionId);

        assertTrue(result);
        verify(auctionRepository, times(1))
                .findActiveAuctionById(eq(auctionId), any(LocalDateTime.class));
    }

    @Test
    void isAuctionActive_shouldReturnFalse_whenRepositoryDoesNotFindAuction() {
        UUID auctionId = UUID.randomUUID();

        when(auctionRepository.findActiveAuctionById(eq(auctionId), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        boolean result = auctionService.isAuctionActive(auctionId);

        assertFalse(result);
        verify(auctionRepository, times(1))
                .findActiveAuctionById(eq(auctionId), any(LocalDateTime.class));
    }

    @Test
    void sendDailyHighestBidNotifications_shouldCreateNotificationAndSendEmail() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = now.plusDays(3);

        // farmer
        Users farmer = new Users();
        farmer.setId(UUID.randomUUID());
        farmer.setFullName("Farmer Name");
        farmer.setEmail("farmer@example.com");

        // highest bidder
        UUID bidderId = UUID.randomUUID();
        Users bidder = new Users();
        bidder.setId(bidderId);
        bidder.setFullName("Bidder Name");
        bidder.setEmail("bidder@example.com");

        // crop & auction
        Crops crop = new Crops();
        crop.setCropName("Wheat");
        crop.setUser(farmer);

        Auction auction = new Auction();
        auction.setCrop(crop);
        auction.setHighestBidderId(bidderId);
        auction.setCurrentHighestBid(new BigDecimal("1200.00"));
        auction.setEndTime(endTime);

        when(auctionRepository.findActiveAuctionsBefore(any(LocalDateTime.class)))
                .thenReturn(List.of(auction));
        when(userRepository.findById(bidderId)).thenReturn(Optional.of(bidder));

        auctionService.sendDailyHighestBidNotifications();

        // verify notification creation
        verify(notificationService, times(1)).createBidUpdateNotification(
                eq(farmer),
                eq(bidder),
                eq(auction),
                eq("New Highest Bid"),
                anyString() // message with amount, crop name and days left
        );

        // CompletableFuture.runAsync runs on a different thread; give it a tiny time window
        Thread.sleep(100); // keep small just to let async execution fire

        verify(emailService, times(1)).sendDailyBidNotification(
                eq(farmer.getId()),
                eq("Wheat"),
                anyLong(), // days left
                eq(new BigDecimal("1200.00"))
        );
    }
}
