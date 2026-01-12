package com.example.userservice.repository;

import com.example.userservice.entity.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface BidRepository extends JpaRepository<Bid, UUID> {

    List<Bid> findByAuctionIdOrderByCreatedAtDesc(UUID auctionId);

    boolean existsByAuctionId(UUID auctionId);

    @Query("""
            SELECT b
            FROM Bid b
            JOIN b.auction a
            where a.status='ACTIVE'
            AND b.bidder.id = :retailerId
            AND b.amount = (
                      SELECT MAX(b2.amount)
                      FROM Bid b2
                      WHERE b2.auction.id = a.id
                        AND b2.bidder.id = :retailerId
                  )
            """)
    List<Bid> findActiveAllByUserId(UUID retailerId);
}
