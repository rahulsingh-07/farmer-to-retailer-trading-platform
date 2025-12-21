package com.example.userservice.repository;

import com.example.userservice.entity.Crops;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface CropRepository extends JpaRepository<Crops,UUID>, JpaSpecificationExecutor<Crops> {


    // Find crops with active auctions only
    @Query("SELECT c FROM Crops c JOIN c.auction a WHERE a.status = 'ACTIVE' AND a.endTime > :now ORDER BY c.createdAt DESC")
    Page<Crops>
    findAvailableCrops(Pageable pageable, @Param("now") LocalDateTime now);

    // Custom query with filters
    @Query("""
        SELECT c FROM Crops c\s
        JOIN c.auction a\s
        WHERE a.status = 'ACTIVE'\s
        AND a.endTime > :now
        AND (:category IS NULL OR c.category = :category)
        AND (:location IS NULL OR c.location = :location)
        AND (:variety IS NULL OR c.variety = :variety)
        ORDER BY c.createdAt DESC
       \s""")
    Page<Crops> findAvailableFiltered(
            Pageable pageable,
            @Param("now") LocalDateTime now,
            @Param("category") String category,
            @Param("location") String location,
            @Param("variety") String variety
    );

    @Query("SELECT c FROM Crops c " +
            "LEFT JOIN FETCH c.images " +
            "LEFT JOIN FETCH c.auction " +
            "LEFT JOIN FETCH c.user " +
            "WHERE c.user.id = :userId")
    List<Crops> findByUserId(UUID userId);

    @Query("SELECT COUNT(c) FROM Crops c WHERE c.user.id = :farmerId")
    long totalCrops(@Param("farmerId") UUID farmerId);

    @Query("""
        SELECT COUNT(a)
        FROM Auction a
        JOIN a.crop c
        WHERE c.user.id = :farmerId
        AND a.status = 'ACTIVE'
    """)
    long totalActiveAuctions(
            @Param("farmerId") UUID farmerId
    );
}
