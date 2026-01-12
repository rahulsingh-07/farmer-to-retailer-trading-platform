package com.example.userservice.repository;

import com.example.userservice.entity.Crops;
import com.example.userservice.enums.CropType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.UUID;

public interface CropRepository extends JpaRepository<Crops,UUID>, JpaSpecificationExecutor<Crops> {

    @Query("""
    SELECT c
    FROM Crops c
    LEFT JOIN c.auction a
    WHERE c.availability = 'AVAILABLE'
      AND (
            c.cropType = 'FIXED'
            OR a.status = 'ACTIVE'
          )
    ORDER BY c.createdAt DESC
""")
    Page<Crops> findAvailableCrops(Pageable pageable);


    @Query("""
    SELECT c
    FROM Crops c
    LEFT JOIN c.auction a
    WHERE c.availability = 'AVAILABLE'
      AND (
            c.cropType = 'FIXED'
            OR (c.cropType = 'AUCTION' AND a.status = 'ACTIVE')
          )
      AND (:cropType IS NULL OR c.cropType = :cropType)
      AND (:category IS NULL OR c.category = :category)
      AND (:location IS NULL OR c.location = :location)
      AND (:variety IS NULL OR c.variety = :variety)
    ORDER BY c.createdAt DESC
""")
    Page<Crops> findAvailableFiltered(
            Pageable pageable,
            @Param("cropType") CropType cropType,
            @Param("category") String category,
            @Param("location") String location,
            @Param("variety") String variety
    );

    @Query("""
    SELECT c FROM Crops c
    LEFT JOIN c.auction a
    WHERE c.user.id = :userId
    AND c.availability='AVAILABLE'
    AND (
        c.cropType = 'FIXED'
        OR (c.cropType = 'AUCTION' AND a.status = 'ACTIVE')
        )
    AND (:cropType IS NULL OR c.cropType = :cropType)
    AND (:category IS NULL OR c.category = :category)
    AND (:location IS NULL OR c.location = :location)
    AND (:variety IS NULL OR c.variety = :variety)
    ORDER BY c.createdAt DESC
""")
    Page<Crops> findByUserIdFilter(
            @Param("userId") UUID userId,
            Pageable pageable,
            @Param("cropType") CropType cropType,
            @Param("category") String category,
            @Param("location") String location,
            @Param("variety") String variety

    );


    @Query("""
    SELECT c FROM Crops c
    LEFT JOIN c.auction a
    WHERE c.user.id = :userId
    AND c.availability='AVAILABLE'
    AND (
           c.cropType = 'FIXED'
           OR (c.cropType = 'AUCTION' AND a.status = 'ACTIVE')
          )
    ORDER BY c.createdAt DESC
""")
    Page<Crops> findByUserId(
            @Param("userId") UUID userId,
            Pageable pageable
    );


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
