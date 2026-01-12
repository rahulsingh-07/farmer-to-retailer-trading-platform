package com.example.userservice.repository;

import com.example.userservice.entity.Users;
import com.example.userservice.enums.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<Users, UUID> {
    boolean existsByUsername(String username);

    Optional<Users> findByUsername(String username);

    List<Users> findByUpdatePasswordRequired(boolean required);

    @Query("SELECT u FROM Users u WHERE u.status = :status")
    Page<Users> findByStatus(@Param("status") UserStatus status, Pageable pageable);
    @Query("SELECT COUNT(u) FROM Users u WHERE u.status = 'ACTIVE'")
    long totalUsers();

    @Query("SELECT COUNT(u) FROM Users u WHERE u.status = 'ACTIVE' AND u.role = 'FARMER'")
    long totalFarmer();

    @Query("SELECT COUNT(u) FROM Users u WHERE u.status = 'ACTIVE' AND u.role = 'RETAILER'")
    long totalRetailer();

    @Query("SELECT COUNT(u) FROM Users u WHERE u.status = 'PENDING'")
    long totalPending();

    @Query("SELECT COUNT(u) FROM Users u WHERE u.status = 'ACTIVE' AND u.role = 'ADMIN'")
    long totalAdmin();

    Optional<Users> findByEmail(String email);

    @Query("SELECT COUNT(u) FROM Users u WHERE u.approvedBy =:username")
    long totalApproved(@Param("username") String username);

    boolean existsByEmail(@Email(message = "Invalid email") @NotBlank(message = "Email cannot be empty") String email);
}
