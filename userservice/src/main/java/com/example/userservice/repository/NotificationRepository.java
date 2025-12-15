package com.example.userservice.repository;

import com.example.userservice.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByFarmerIdOrderByCreatedAtDesc( UUID farmerId);

    @Query("SELECT n FROM Notification n " +
            "JOIN FETCH n.bidder b " +
            "JOIN FETCH b.retailerDetails rd " +
            "JOIN FETCH n.auction a " +
            "JOIN FETCH a.crop c " +
            "WHERE n.id = :id")
    Optional<Notification> findNotificationWithDetails(UUID id);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.id = :id")
    int markAsRead(@Param("id") UUID id);
}
