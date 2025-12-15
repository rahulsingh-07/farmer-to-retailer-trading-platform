package com.example.userservice.farmerService;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.userservice.dto.CropImageResponse;
import com.example.userservice.dto.CropRequest;
import com.example.userservice.dto.NotificationDTO;
import com.example.userservice.dto.NotificationResponse;
import com.example.userservice.entity.Crops;
import com.example.userservice.entity.CropImage;
import com.example.userservice.entity.Notification;
import com.example.userservice.entity.Users;
import com.example.userservice.mapper.CropMapper;
import com.example.userservice.repository.CropRepository;
import com.example.userservice.repository.NotificationRepository;
import com.example.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FarmerService {
    private final UserRepository userRepository;
    private final CropMapper cropMapper;
    private final CropRepository cropRepository;
    private final Cloudinary cloudinary;
    private final NotificationRepository notificationRepository;

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
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload image", e);
            }
        }

        cropRepository.save(crop);
        return "Crop added successfully";
    }

    public List<CropRequest> getMyCrop(UUID userId){

        return cropRepository.findByUserId(userId).stream()
                .map(cropMapper::toDto)
                .toList();
    }

    public List<NotificationDTO> getNotifications(UUID userId) {
        List<Notification> notifications =
                notificationRepository.findByFarmerIdOrderByCreatedAtDesc(userId);

        return notifications.stream()
                .map(notification -> NotificationDTO.builder()
                        .id(notification.getId())
                        .bidderFullName(notification.getBidder().getFullName())
                        .type(notification.getType())
                        .auctionId(notification.getAuction().getId())
                        .createdAt(notification.getCreatedAt())
                        .read(notification.isRead())
                        .build())
                .toList();
    }

    @Transactional
    public NotificationResponse getNotificationsDetails(UUID id) {
        Notification n=notificationRepository.findNotificationWithDetails(id)
                .orElseThrow(()->new RuntimeException("Notification not found"));
        notificationRepository.markAsRead(id);

        return NotificationResponse.builder()
                .notificationId(n.getId())
                .message(n.getMessage())
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .type(n.getType())
                .status(n.getAuction().getStatus())

                // Crop
                .cropId(n.getAuction().getCrop().getId())
                .cropName(n.getAuction().getCrop().getCropName())
                .category(n.getAuction().getCrop().getCategory())
                .variety(n.getAuction().getCrop().getVariety())
                .quantity(n.getAuction().getCrop().getQuantity())
                .unit(n.getAuction().getCrop().getUnit())
                .imageUrl(n.getAuction().getCrop().getImages()
                        .stream()
                        .map(img -> {
                            CropImageResponse imgRes = new CropImageResponse();
                            imgRes.setImageUrl(img.getImageUrl());
                            return imgRes;
                            })
                        .toList())

                // Bidder
                .bidderFullName(n.getBidder().getFullName())
                .bidderPhoneNumber(n.getBidder().getPhoneNumber())
                .bidderAddress(n.getBidder().getRetailerDetails().getBusinessAddress())
                .build();
    }
}
