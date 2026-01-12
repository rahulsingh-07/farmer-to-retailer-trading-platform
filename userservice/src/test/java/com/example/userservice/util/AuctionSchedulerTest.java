package com.example.userservice.util;

import com.example.userservice.entity.Auction;
import com.example.userservice.entity.Crops;
import com.example.userservice.enums.AuctionStatus;
import com.example.userservice.enums.CropAvailability;
import com.example.userservice.repository.AuctionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuctionSchedulerTest {

    @Mock private AuctionRepository auctionRepository;
    @Mock private ApplicationEventPublisher applicationEventPublisher;
    @InjectMocks
    private AuctionScheduler auctionScheduler;

    @Test
    void checkExpiredAuctions_noBids_shouldCloseAuctionWithoutEvent() {
        Auction auction = new Auction();
        auction.setStatus(AuctionStatus.ACTIVE);
        auction.setHighestBidderId(null);

        when(auctionRepository.findExpiredAuctions(
                eq(AuctionStatus.ACTIVE), any(LocalDateTime.class)))
                .thenReturn(List.of(auction));

        auctionScheduler.checkExpiredAuctions();

        assertEquals(AuctionStatus.CLOSED, auction.getStatus());
        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    void checkExpiredAuctions_withBids_shouldSellAuctionAndPublishEvent() {
        Crops crop = new Crops();
        crop.setAvailability(CropAvailability.AVAILABLE);

        Auction auction = new Auction();
        auction.setStatus(AuctionStatus.ACTIVE);
        auction.setHighestBidderId(UUID.randomUUID());
        auction.setCrop(crop);

        when(auctionRepository.findExpiredAuctions(
                eq(AuctionStatus.ACTIVE), any(LocalDateTime.class)))
                .thenReturn(List.of(auction));

        auctionScheduler.checkExpiredAuctions();

        assertEquals(AuctionStatus.SOLD, auction.getStatus());
        assertEquals(CropAvailability.RESERVED, crop.getAvailability());

        verify(applicationEventPublisher).publishEvent(
                argThat(event ->
                        event instanceof AuctionClosedEvent &&
                                ((AuctionClosedEvent) event).getAuction() == auction
                )
        );
    }

    @Test
    void checkExpiredAuctions_noExpiredAuctions_shouldDoNothing() {
        when(auctionRepository.findExpiredAuctions(
                eq(AuctionStatus.ACTIVE), any(LocalDateTime.class)))
                .thenReturn(List.of());

        auctionScheduler.checkExpiredAuctions();

        verify(applicationEventPublisher, never()).publishEvent(any());
        verifyNoMoreInteractions(applicationEventPublisher);
    }



}
