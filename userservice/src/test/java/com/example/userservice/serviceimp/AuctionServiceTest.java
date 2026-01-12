package com.example.userservice.serviceimp;

import com.example.userservice.entity.Auction;
import com.example.userservice.entity.Crops;
import com.example.userservice.entity.Users;
import com.example.userservice.enums.NotificationType;
import com.example.userservice.repository.AuctionRepository;
import com.example.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuctionServiceTest {
    @Mock private  AuctionRepository auctionRepository;
    @Mock private  EmailServiceImp emailService;
    @Mock private  NotificationServiceImp notificationServiceImp;
    @Mock private  UserRepository userRepository;
    @InjectMocks
    private AuctionService auctionService;

    //=== IS AUCTION ACTIVE ===
    @Test
    void testIsAuctionActive_ReturnsTrue_WhenAuctionExists() {
        UUID auctionId = UUID.randomUUID();
        when(auctionRepository.findActiveAuctionById(eq(auctionId), any(LocalDateTime.class)))
                .thenReturn(Optional.of(new Auction()));

        boolean result = auctionService.isAuctionActive(auctionId);

        assertTrue(result);
        verify(auctionRepository).findActiveAuctionById(eq(auctionId), any(LocalDateTime.class));
    }

    @Test
    void testIsAuctionActive_ReturnsFalse_WhenAuctionNotExists() {
        UUID auctionId = UUID.randomUUID();
        when(auctionRepository.findActiveAuctionById(eq(auctionId), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        boolean result = auctionService.isAuctionActive(auctionId);

        assertFalse(result);
        verify(auctionRepository).findActiveAuctionById(eq(auctionId), any(LocalDateTime.class));
    }

    //=== SEND DAILY HIGHEST BID TESTING ===
    @Test
    void testSendDailyHighestBidNotifications(){
        LocalDateTime now=LocalDateTime.now();

        Users farmer=new Users();
        farmer.setId(UUID.randomUUID());

        Crops crop = new Crops();
        crop.setCropName("Wheat");
        crop.setUser(farmer);

        UUID bidderId = UUID.randomUUID();
        Users bidder = new Users();
        bidder.setId(bidderId);

        Auction auction = new Auction();
        auction.setId(UUID.randomUUID());
        auction.setCrop(crop);
        auction.setHighestBidderId(bidderId);
        auction.setCurrentHighestBid(BigDecimal.valueOf(1000));
        auction.setEndTime(now.plusDays(7));

        when(auctionRepository.findActiveAuctionsBefore(any(LocalDateTime.class)))
                .thenReturn(List.of(auction));
        when(userRepository.findById(bidderId)).thenReturn(Optional.of(bidder));

        auctionService.sendDailyHighestBidNotifications();

        verify(notificationServiceImp).createBidUpdateNotification(
                eq(farmer),
                eq(auction),
                any(LocalDateTime.class),
                contains("₹1000"),
                eq(NotificationType.BID_UPDATE)
        );
        verify(emailService).sendDailyBidNotification(
                eq(farmer.getId()),
                eq("Wheat"),
                anyLong(),
                eq(BigDecimal.valueOf(1000))
        );
    }

    @Test
    void testSendDailyHighestBidNotifications_SkipsAuction_WhenNoCrop(){
        Auction auction = new Auction();
        auction.setId(UUID.randomUUID());
        auction.setCrop(null);

        when(auctionRepository.findActiveAuctionsBefore(any(LocalDateTime.class)))
                .thenReturn(List.of(auction));

        auctionService.sendDailyHighestBidNotifications();

        verify(notificationServiceImp, never()).createBidUpdateNotification(any(), any(), any(), anyString(), any());
        verify(emailService, never()).sendDailyBidNotification(any(), anyString(), anyLong(), any());
    }
}
