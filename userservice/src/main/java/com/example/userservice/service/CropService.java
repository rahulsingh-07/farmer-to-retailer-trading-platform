package com.example.userservice.service;

import com.example.userservice.dto.CropRequest;
import com.example.userservice.dto.CropUpdateRequest;
import com.example.userservice.dto.FarmerCropDetailDto;
import com.example.userservice.enums.CropType;
import com.example.userservice.records.CropCardDto;
import com.example.userservice.records.FarmerStats;
import com.example.userservice.records.RetailerCropDetailDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface CropService {
    // farmer methods
    void createCrop(CropRequest request, UUID userId, MultipartFile[] files);
    Page<CropCardDto> findFarmerCrops(UUID userId, Pageable pageable, CropType cropType, String category, String location, String variety);
    FarmerStats getFarmerStats(UUID farmerId);
    FarmerCropDetailDto getFarmerCropById(UUID cropId);
    void updateCrop(UUID id, CropUpdateRequest request, UUID userId);

    // retailer methods
    Page<CropCardDto> findAvailableCrops(Pageable pageable, CropType cropType, String category, String location, String variety);

    // common methods
    RetailerCropDetailDto getCropDetail(UUID id);

}
