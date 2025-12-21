package com.example.userservice.util;

import com.example.userservice.entity.Auction;
import com.example.userservice.enums.AuctionStatus;
import com.example.userservice.repository.AuctionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AuctionScheduler {
    private final AuctionRepository auctionRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Scheduled(fixedRate = 60000) // Every minute
    @Transactional
    public void checkExpiredAuctions() {
        List<Auction> expired = auctionRepository
                .findExpiredAuctions(AuctionStatus.ACTIVE, LocalDateTime.now());

        expired.forEach(auction -> {
            auction.setStatus(AuctionStatus.SOLD);
            auctionRepository.save(auction);

            //AUTO CREATE ORDER
            applicationEventPublisher.publishEvent(
                    new AuctionClosedEvent(this,auction)
            );
        });
    }
}
