package com.example.userservice.repository;

import com.example.userservice.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    boolean existsByOrderIdAndReviewerId(UUID orderId, UUID userId);

    Optional<Review> findByOrderId(UUID orderId);
}
