package com.example.userservice.farmerService;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.example.userservice.dto.*;
import com.example.userservice.entity.*;
import com.example.userservice.enums.AuctionStatus;
import com.example.userservice.enums.NotificationType;
import com.example.userservice.mapper.CropMapper;
import com.example.userservice.repository.AuctionRepository;
import com.example.userservice.repository.CropRepository;
import com.example.userservice.repository.NotificationRepository;
import com.example.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FarmerServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CropRepository cropRepository;

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private AuctionRepository auctionRepository;

    @Mock
    private NotificationRepository notificationRepository;

    private FarmerService farmerService; // uses real CropMapper

    @BeforeEach
    void setUp() {
        CropMapper realMapper = new CropMapper(auctionRepository); // real mapper
        farmerService = new FarmerService(
                userRepository,
                realMapper,
                cropRepository,
                cloudinary,
                notificationRepository
        );
    }

    @Test
    void createCrop_shouldUploadImagesAndSaveCrop() throws Exception {
        UUID userId = UUID.randomUUID();
        Users user = new Users();
        user.setId(userId);

        CropRequest req = new CropRequest();
        req.setCropName("Wheat");
        req.setCategory("Grain");
        req.setVariety("Variety A");
        req.setQuantity(100.0);
        req.setUnit("KG");
        req.setPricePerUnit(new BigDecimal("10"));
        req.setLocation("Delhi");
        req.setHarvestDate(LocalDate.now());
        req.setDescription("Test crop");

        Crops crop = new Crops();
        crop.setImages(new ArrayList<>());

        MultipartFile file1 = mock(MultipartFile.class);
        MultipartFile file2 = mock(MultipartFile.class);
        MultipartFile[] files = new MultipartFile[]{file1, file2};

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(file1.getBytes()).thenReturn("img1".getBytes());
        when(file2.getBytes()).thenReturn("img2".getBytes());

        Uploader uploader = mock(Uploader.class);
        when(cloudinary.uploader()).thenReturn(uploader);

        Map<String, Object> uploadResult1 = Map.of("url", "http://image1", "public_id", "public1");
        Map<String, Object> uploadResult2 = Map.of("url", "http://image2", "public_id", "public2");

        when(uploader.upload(eq(file1.getBytes()), any(Map.class))).thenReturn(uploadResult1);
        when(uploader.upload(eq(file2.getBytes()), any(Map.class))).thenReturn(uploadResult2);

        String result = farmerService.createCrop(req, userId, files);

        assertEquals("Crop added successfully", result);
        verify(userRepository).findById(userId);
        verify(cropRepository).save(any(Crops.class));
    }


    @Test
    void createCrop_shouldThrowWhenUserNotFound() {
        UUID userId = UUID.randomUUID();
        CropRequest req = new CropRequest();
        MultipartFile[] files = new MultipartFile[0];

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> farmerService.createCrop(req, userId, files));
        assertEquals("User not found", ex.getMessage());
        verify(cropRepository, never()).save(any());
    }

    @Test
    void getMyCrop_shouldReturnMappedDtos() {
        UUID userId = UUID.randomUUID();

        Crops crop1 = new Crops();
        crop1.setCropName("A");
        Crops crop2 = new Crops();
        crop2.setCropName("B");

        when(cropRepository.findByUserId(userId)).thenReturn(List.of(crop1, crop2));

        List<CropRequest> result = farmerService.getMyCrop(userId);

        assertEquals(2, result.size());

        Set<String> names = new HashSet<>();
        names.add(result.get(0).getCropName());
        names.add(result.get(1).getCropName());

        assertEquals(Set.of("A", "B"), names);
    }

    @Test
    void getNotifications_shouldMapNotificationsToDTOs() {
        UUID farmerId = UUID.randomUUID();

        Users bidder = new Users();
        bidder.setFullName("Bidder Name");

        Auction auction = new Auction();
        auction.setId(UUID.randomUUID());

        Notification n1 = new Notification();
        n1.setId(UUID.randomUUID());
        n1.setBidder(bidder);
        n1.setAuction(auction);
        n1.setType(NotificationType.BID_UPDATE);
        n1.setCreatedAt(LocalDateTime.now());
        n1.setRead(false);

        when(notificationRepository.findByFarmerIdOrderByCreatedAtDesc(farmerId))
                .thenReturn(List.of(n1));

        List<NotificationDTO> result = farmerService.getNotifications(farmerId);

        assertEquals(1, result.size());
        NotificationDTO dto = result.get(0);

        assertEquals(n1.getId(), dto.getId());
        assertEquals("Bidder Name", dto.getBidderFullName());
        assertEquals(NotificationType.BID_UPDATE, dto.getType());
        assertEquals(auction.getId(), dto.getAuctionId());
        assertEquals(n1.getCreatedAt(), dto.getCreatedAt());
        assertEquals(n1.isRead(), dto.isRead());
    }

    @Test
    void getNotificationsDetails_shouldReturnResponseAndMarkAsRead() {
        UUID notifId = UUID.randomUUID();

        Users bidder = new Users();
        bidder.setFullName("Bidder Name");
        bidder.setPhoneNumber("1234567890");

        RetailerDetails retailerDetails = new RetailerDetails();
        retailerDetails.setBusinessAddress("Some Address");
        bidder.setRetailerDetails(retailerDetails);

        CropImage img1 = new CropImage();
        img1.setImageUrl("http://img1");

        Crops crop = new Crops();
        crop.setId(UUID.randomUUID());
        crop.setCropName("Wheat");
        crop.setCategory("Grain");
        crop.setVariety("Variety A");
        crop.setQuantity(100.0);
        crop.setUnit("KG");
        crop.setImages(List.of(img1));

        Auction auction = new Auction();
        auction.setId(UUID.randomUUID());
        auction.setStatus(AuctionStatus.ACTIVE);
        auction.setCrop(crop);

        Notification n = new Notification();
        n.setId(notifId);
        n.setMessage("You have a new bid");
        n.setRead(false);
        n.setCreatedAt(LocalDateTime.now());
        n.setType(NotificationType.BID_UPDATE);
        n.setAuction(auction);
        n.setBidder(bidder);

        when(notificationRepository.findNotificationWithDetails(notifId))
                .thenReturn(Optional.of(n));

        NotificationResponse response = farmerService.getNotificationsDetails(notifId);

        assertEquals(notifId, response.getNotificationId());
        assertEquals("You have a new bid", response.getMessage());
        assertEquals(n.isRead(), response.isRead());
        assertEquals(n.getCreatedAt(), response.getCreatedAt());
        assertEquals(NotificationType.BID_UPDATE, response.getType());
        assertEquals(AuctionStatus.ACTIVE, response.getStatus());

        assertEquals(crop.getId(), response.getCropId());
        assertEquals("Wheat", response.getCropName());
        assertEquals("Grain", response.getCategory());
        assertEquals("Variety A", response.getVariety());
        assertEquals(100.0, response.getQuantity());
        assertEquals("KG", response.getUnit());
        assertEquals(1, response.getImageUrl().size());
        assertEquals("http://img1", response.getImageUrl().get(0).getImageUrl());

        assertEquals("Bidder Name", response.getBidderFullName());
        assertEquals("1234567890", response.getBidderPhoneNumber());
        assertEquals("Some Address", response.getBidderAddress());

        verify(notificationRepository, times(1)).markAsRead(notifId);
    }

    @Test
    void getNotificationsDetails_shouldThrowWhenNotFound() {
        UUID notifId = UUID.randomUUID();
        when(notificationRepository.findNotificationWithDetails(notifId))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> farmerService.getNotificationsDetails(notifId));
        assertEquals("Notification not found", ex.getMessage());
        verify(notificationRepository, never()).markAsRead(any());
    }
}
