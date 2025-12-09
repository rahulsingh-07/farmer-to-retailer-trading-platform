package com.example.userservice.repository;

import com.example.userservice.entity.Users;
import com.example.userservice.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<Users, UUID> {
    boolean existsByUsername(String username);

    Optional<Users> findByUsername(String username);

    @Query("SELECT u FROM Users u WHERE u.status = :status")
    List<Users> findByStatus(@Param("status") UserStatus status);

    @Query("SELECT COUNT(u) FROM Users u WHERE u.status = 'ACTIVE'")
    int totalUsers();

    @Query("SELECT COUNT(u) FROM Users u WHERE u.status = 'ACTIVE' AND u.role = 'FARMER'")
    int totalFarmer();

    @Query("SELECT COUNT(u) FROM Users u WHERE u.status = 'ACTIVE' AND u.role = 'RETAILER'")
    int totalRetailer();

    @Query("SELECT COUNT(u) FROM Users u WHERE u.status = 'PENDING'")
    int totalPending();

    @Query("SELECT COUNT(u) FROM Users u WHERE u.status = 'ACTIVE' AND u.role = 'ADMIN'")
    int totalAdmin();

}
