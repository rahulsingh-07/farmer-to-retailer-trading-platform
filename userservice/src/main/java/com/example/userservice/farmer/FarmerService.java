package com.example.userservice.farmer;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.userservice.dto.*;
import com.example.userservice.entity.*;
import com.example.userservice.enums.OrderStatus;
import com.example.userservice.enums.UserRole;
import com.example.userservice.exception.CropNotFoundException;
import com.example.userservice.exception.ImageUploadException;
import com.example.userservice.mapper.CropMapper;
import com.example.userservice.mapper.FarmerCropMapper;
import com.example.userservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FarmerService {
    private final UserRepository userRepository;
    private final CropMapper cropMapper;
    private final CropRepository cropRepository;
    private final Cloudinary cloudinary;
    private final NotificationRepository notificationRepository;
    private final BidRepository bidRepository;
    private final FarmerCropMapper farmerCropMapper;
    private final OrderRepository orderRepository;

    public String createCrop(CropRequest request, UUID userId, MultipartFile[] files) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Crops crop = cropMapper.toEntity(request, user);

        // Upload each image to Cloudinary and save
        for (MultipartFile file : files) {
            try {
                Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
                CropImage image = new CropImage();
                image.setImageUrl(uploadResult.get("url").toString());
                image.setCloudinaryPublicId(uploadResult.get("public_id").toString());
                image.setCrop(crop);

                crop.getImages().add(image);
            } catch (IOException e){
                throw new ImageUploadException("Failed to upload image",e);
            }
        }

        cropRepository.save(crop);
        return "Crop added successfully";
    }

    public List<CropResponse> getMyCrops(UUID userId) {
        List<Crops> crops = cropRepository.findByUserId(userId);
        return cropMapper.toResponse(crops);  // Uses existing CropMapper
    }

    public Map<String,Long> getCropNumber(UUID farmerId){
        Map<String,Long> map=new HashMap<>();
        map.put("totalCrops",cropRepository.totalCrops(farmerId));
        map.put("totalActiveAuction",cropRepository.totalActiveAuctions(farmerId));
        map.put("totalPendingOrders",orderRepository.countOrdersByFarmer(farmerId, OrderStatus.PENDING));
        map.put("totalWaitingPayment",orderRepository.countOrdersByFarmer(farmerId,OrderStatus.CONFIRMED));
        map.put("totalShippedOrders",orderRepository.countOrdersByFarmer(farmerId,OrderStatus.SHIPPED));
        map.put("totalCompletedDelivery", orderRepository.countOrdersByFarmer(farmerId,OrderStatus.DELIVERED));
        map.put("totalActiveOrders", orderRepository.countActiveOrdersByFarmer(farmerId));
        return map;
    }

    public FarmerCropDetailDto getFarmerCrop(UUID cropId) {
        Crops crop = cropRepository.findById(cropId)
                .orElseThrow(() -> new CropNotFoundException("Crop not found: " + cropId));

        UUID auctionId = crop.getAuction().getId();
        List<Bid> bids = bidRepository.findByAuctionIdOrderByCreatedAtDesc(auctionId);

        return farmerCropMapper.toFarmerCropDetailDto(crop, bids);
    }

}
