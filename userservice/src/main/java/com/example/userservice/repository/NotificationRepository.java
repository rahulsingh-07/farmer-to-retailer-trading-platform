package com.example.userservice.repository;

import com.example.userservice.entity.Notification;
import com.example.userservice.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {


    List<Notification> findByRoleIdAndRoleOrderByCreatedAtDesc(UUID roleId, UserRole role);

    @Query("SELECT n FROM Notification n " +
            "JOIN FETCH n.bidder b " +
            "JOIN FETCH b.retailerDetails rd " +
            "JOIN FETCH n.auction a " +
            "JOIN FETCH a.crop c " +
            "WHERE n.id = :id ")
    Optional<Notification> findNotificationWithDetails(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.id = :id")
    void markAsRead(@Param("id") UUID id);

    @Query("SELECT COUNT(n) FROM Notification n where n.roleId=:userId AND n.read=false")
    Long countUnreadByUserId(UUID userId);
}
