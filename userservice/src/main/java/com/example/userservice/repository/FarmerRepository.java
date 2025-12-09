package com.example.userservice.repository;

import com.example.userservice.entity.FarmerDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FarmerRepository extends JpaRepository<FarmerDetails, UUID> {
}
