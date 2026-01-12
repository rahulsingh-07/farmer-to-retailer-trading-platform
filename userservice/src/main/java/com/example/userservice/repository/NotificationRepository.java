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

    @Query("""
    SELECT n FROM Notification n
    WHERE n.receiver.id = :receiverId
    ORDER BY n.createdAt DESC
""")
    List<Notification> findByReceiverId(UUID receiverId);

    @Query("""
    SELECT n FROM Notification n
    LEFT JOIN FETCH n.auction a
    LEFT JOIN FETCH a.crop c
    WHERE n.id = :id
    """)
    Optional<Notification> findNotificationWithDetails(@Param("id") UUID id);

    @Query("SELECT COUNT(n) FROM Notification n where n.receiver.id=:receiverId AND n.read=false ")
    long countUnreadByUserId(@Param("receiverId")UUID receiverId);

    @Modifying
    @Query("""
    UPDATE Notification n
    SET n.read = true
    WHERE n.id IN :ids
""")
    void markAsRead(@Param("ids") List<UUID> ids);

}
