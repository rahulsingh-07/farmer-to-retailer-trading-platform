package com.example.userservice.util;

import com.example.userservice.entity.Auction;
import com.example.userservice.enums.AuctionStatus;
import com.example.userservice.repository.AuctionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuctionSchedulerTest {

    @Mock
    private AuctionRepository auctionRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private AuctionScheduler auctionScheduler;

    @Test
    void checkExpiredAuctions_shouldMarkAuctionsAsSold_andPublishEvent() {
        // Arrange
        Auction auction1 = new Auction();
        auction1.setStatus(AuctionStatus.ACTIVE);

        Auction auction2 = new Auction();
        auction2.setStatus(AuctionStatus.ACTIVE);

        when(auctionRepository.findExpiredAuctions(
                eq(AuctionStatus.ACTIVE),
                any(LocalDateTime.class)
        )).thenReturn(List.of(auction1, auction2));

        // Act
        auctionScheduler.checkExpiredAuctions();

        // Assert: status update
        assertThat(auction1.getStatus()).isEqualTo(AuctionStatus.SOLD);
        assertThat(auction2.getStatus()).isEqualTo(AuctionStatus.SOLD);

        // Assert: save called
        verify(auctionRepository, times(2)).save(any(Auction.class));

        // Assert: event published
        ArgumentCaptor<AuctionClosedEvent> eventCaptor =
                ArgumentCaptor.forClass(AuctionClosedEvent.class);

        verify(applicationEventPublisher, times(2))
                .publishEvent(eventCaptor.capture());

        List<AuctionClosedEvent> events = eventCaptor.getAllValues();
        assertThat(events).hasSize(2);
        assertThat(events.get(0).getAuction()).isIn(auction1, auction2);
    }

    @Test
    void checkExpiredAuctions_shouldDoNothing_whenNoExpiredAuctions() {
        // Arrange
        when(auctionRepository.findExpiredAuctions(
                eq(AuctionStatus.ACTIVE),
                any(LocalDateTime.class)
        )).thenReturn(List.of());

        // Act
        auctionScheduler.checkExpiredAuctions();

        // Assert
        verify(auctionRepository, never()).save(any());
        verify(applicationEventPublisher, never()).publishEvent(any());
    }
}
