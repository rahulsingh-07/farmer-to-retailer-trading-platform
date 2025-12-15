package com.example.userservice.repository;

import com.example.userservice.entity.Auction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuctionRepository extends JpaRepository<Auction, UUID> {


    // Or better: Custom JPQL query
    @Query("SELECT a FROM Auction a WHERE a.id = :id AND a.status = 'ACTIVE' AND a.endTime > :now")
    Optional<Auction> findActiveAuctionById(@Param("id") UUID id, @Param("now") LocalDateTime now);

    @Query("SELECT currentHighestBid from Auction a WHERE a.id= :id")
    BigDecimal findHighestBid(@Param("id") UUID id);

    // Find all active auctions (for daily notifications)
    @Query("SELECT a FROM Auction a WHERE a.status = 'ACTIVE' AND a.endTime > :now AND a.highestBidderId IS NOT NULL")
    List<Auction> findActiveAuctionsBefore(@Param("now") LocalDateTime now);

    Optional<Auction> findByCropId(UUID id);
}
