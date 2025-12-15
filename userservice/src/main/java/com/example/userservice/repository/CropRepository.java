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
    List<Crops> findByUserId(UUID userId);

    // Find crops with active auctions only
    @Query("SELECT c FROM Crops c JOIN c.auction a WHERE a.status = 'ACTIVE' AND a.endTime > :now ORDER BY c.createdAt DESC")
    Page<Crops> findAvailableCrops(Pageable pageable, @Param("now") LocalDateTime now);

    // Custom query with filters
    @Query("""
        SELECT c FROM Crops c 
        JOIN c.auction a 
        WHERE a.status = 'ACTIVE' 
        AND a.endTime > :now
        AND (:category IS NULL OR c.category = :category)
        AND (:location IS NULL OR c.location = :location)
        AND (:variety IS NULL OR c.variety = :variety)
        ORDER BY c.createdAt DESC
        """)
    Page<Crops> findAvailableFiltered(
            Pageable pageable,
            @Param("now") LocalDateTime now,
            @Param("category") String category,
            @Param("location") String location,
            @Param("variety") String variety
    );

}
