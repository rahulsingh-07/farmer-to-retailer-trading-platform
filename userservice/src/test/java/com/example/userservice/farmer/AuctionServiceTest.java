package com.example.userservice.farmer;

import com.example.userservice.entity.Auction;
import com.example.userservice.entity.Crops;
import com.example.userservice.entity.Users;
import com.example.userservice.enums.NotificationType;
import com.example.userservice.enums.UserRole;
import com.example.userservice.notification.EmailService;
import com.example.userservice.repository.AuctionRepository;
import com.example.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
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

    private Users farmer;
    private Users bidder;
    private Crops crop;
    private Auction validAuction;

    @BeforeEach
    void setUp() {
        farmer = new Users();
        farmer.setId(UUID.randomUUID());
        farmer.setRole(UserRole.FARMER);
        farmer.setFullName("Farmer Ram");

        bidder = new Users();
        bidder.setId(UUID.randomUUID());
        bidder.setRole(UserRole.RETAILER);
        bidder.setFullName("Retailer Shyam");

        crop = new Crops();
        crop.setCropName("Wheat");
        crop.setUser(farmer);

        validAuction = new Auction();
        validAuction.setId(UUID.randomUUID());
        validAuction.setCrop(crop);
        validAuction.setHighestBidderId(bidder.getId());
        validAuction.setCurrentHighestBid(BigDecimal.valueOf(5000));
        validAuction.setEndTime(LocalDateTime.now().plusDays(3));
    }

    // ================= isAuctionActive =================

    @Test
    void isAuctionActive_whenAuctionExists_shouldReturnTrue() {
        when(auctionRepository.findActiveAuctionById(any(), any()))
                .thenReturn(Optional.of(validAuction));

        boolean result = auctionService.isAuctionActive(validAuction.getId());

        assertTrue(result);
    }

    @Test
    void isAuctionActive_whenAuctionDoesNotExist_shouldReturnFalse() {
        when(auctionRepository.findActiveAuctionById(any(), any()))
                .thenReturn(Optional.empty());

        boolean result = auctionService.isAuctionActive(UUID.randomUUID());

        assertFalse(result);
    }

    // ================= sendDailyHighestBidNotifications =================

    @Test
    void sendDailyHighestBidNotifications_successFlow() {
        when(auctionRepository.findActiveAuctionsBefore(any()))
                .thenReturn(List.of(validAuction));

        when(userRepository.findById(bidder.getId()))
                .thenReturn(Optional.of(bidder));

        auctionService.sendDailyHighestBidNotifications();

        verify(notificationService).createBidUpdateNotification(
                eq(UserRole.FARMER),
                eq(farmer.getId()),
                eq(farmer),
                eq(bidder),
                eq(validAuction),
                any(LocalDateTime.class),
                eq("New Highest Bid"),
                contains("Wheat"),
                eq(NotificationType.BID_UPDATE)
        );

        verify(emailService, timeout(1000))
                .sendDailyBidNotification(
                        eq(farmer.getId()),
                        eq("Wheat"),
                        anyLong(),
                        eq(BigDecimal.valueOf(5000))
                );
    }

    @Test
    void sendDailyHighestBidNotifications_whenCropIsNull_shouldSkip() {
        validAuction.setCrop(null);

        when(auctionRepository.findActiveAuctionsBefore(any()))
                .thenReturn(List.of(validAuction));

        auctionService.sendDailyHighestBidNotifications();

        verifyNoInteractions(notificationService, emailService, userRepository);
    }

    @Test
    void sendDailyHighestBidNotifications_whenFarmerIsNull_shouldSkip() {
        crop.setUser(null);

        when(auctionRepository.findActiveAuctionsBefore(any()))
                .thenReturn(List.of(validAuction));

        auctionService.sendDailyHighestBidNotifications();

        verifyNoInteractions(notificationService, emailService, userRepository);
    }

    @Test
    void sendDailyHighestBidNotifications_whenBidderIdIsNull_shouldSkip() {
        validAuction.setHighestBidderId(null);

        when(auctionRepository.findActiveAuctionsBefore(any()))
                .thenReturn(List.of(validAuction));

        auctionService.sendDailyHighestBidNotifications();

        verifyNoInteractions(notificationService, emailService, userRepository);
    }

    @Test
    void sendDailyHighestBidNotifications_whenBidderNotFound_shouldSkipGracefully() {
        when(auctionRepository.findActiveAuctionsBefore(any()))
                .thenReturn(List.of(validAuction));

        when(userRepository.findById(bidder.getId()))
                .thenReturn(Optional.empty());

        auctionService.sendDailyHighestBidNotifications();

        verifyNoInteractions(notificationService, emailService);
    }

    @Test
    void sendDailyHighestBidNotifications_whenHighestBidIsNull_shouldUseZero() {
        validAuction.setCurrentHighestBid(null);

        when(auctionRepository.findActiveAuctionsBefore(any()))
                .thenReturn(List.of(validAuction));

        when(userRepository.findById(bidder.getId()))
                .thenReturn(Optional.of(bidder));

        auctionService.sendDailyHighestBidNotifications();

        verify(notificationService).createBidUpdateNotification(
                any(), any(), any(), any(), any(), any(),
                any(), contains("₹0"), any()
        );
    }

    @Test
    void sendDailyHighestBidNotifications_whenMultipleAuctions_andOneFails_shouldContinue() {
        Auction brokenAuction = new Auction();
        brokenAuction.setId(UUID.randomUUID());

        when(auctionRepository.findActiveAuctionsBefore(any()))
                .thenReturn(List.of(brokenAuction, validAuction));

        when(userRepository.findById(bidder.getId()))
                .thenReturn(Optional.of(bidder));

        auctionService.sendDailyHighestBidNotifications();

        verify(notificationService, times(1))
                .createBidUpdateNotification(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void sendDailyHighestBidNotifications_whenNoAuctions_shouldDoNothing() {
        when(auctionRepository.findActiveAuctionsBefore(any()))
                .thenReturn(List.of());

        auctionService.sendDailyHighestBidNotifications();

        verifyNoInteractions(notificationService, emailService, userRepository);
    }
}
