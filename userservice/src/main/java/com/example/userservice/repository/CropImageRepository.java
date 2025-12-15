package com.example.userservice.repository;

import com.example.userservice.entity.CropImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CropImageRepository extends JpaRepository<CropImage, UUID> {
}
