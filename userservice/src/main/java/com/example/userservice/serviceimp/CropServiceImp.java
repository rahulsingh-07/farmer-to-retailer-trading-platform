package com.example.userservice.serviceimp;

import com.cloudinary.Cloudinary;
import com.example.userservice.dto.CropRequest;
import com.example.userservice.dto.CropUpdateRequest;
import com.example.userservice.dto.FarmerCropDetailDto;
import com.example.userservice.entity.*;
import com.example.userservice.enums.CropType;
import com.example.userservice.enums.OrderStatus;
import com.example.userservice.exception.CropNotFoundException;
import com.example.userservice.exception.FileUploadException;
import com.example.userservice.mapper.CropMapper;
import com.example.userservice.mapper.FarmerCropMapper;
import com.example.userservice.records.CropCardDto;
import com.example.userservice.records.FarmerStats;
import com.example.userservice.records.RetailerCropDetailDto;
import com.example.userservice.repository.BidRepository;
import com.example.userservice.repository.CropRepository;
import com.example.userservice.repository.OrderRepository;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.CropService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class CropServiceImp implements CropService {
    private final CropRepository cropRepository;
    private final UserRepository userRepository;
    private final CropMapper cropMapper;
    private final Cloudinary cloudinary;
    private final BidRepository bidRepository;
    private final FarmerCropMapper farmerCropMapper;
    private final OrderRepository orderRepository;
    private final CloudinaryServiceImp cloudinaryServiceImp;

    @Override
    @Transactional
    public void createCrop(CropRequest request, UUID userId, MultipartFile[] files) {

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Crops crop = cropMapper.toEntity(request, user);
        List<CropImage> images = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                Map<String, String> uploadResult = cloudinaryServiceImp.uploadFile(file, "crop-images");

                CropImage image = new CropImage();
                image.setImageUrl(uploadResult.get("url"));
                image.setCloudinaryPublicId(uploadResult.get("publicId"));
                image.setCrop(crop);

                images.add(image);
            } catch (IOException e) {
                throw new FileUploadException("Failed to upload image", e);
            }
        }
        crop.setImages(images);
        cropRepository.save(crop);
    }

    @Override
    public Page<CropCardDto> findFarmerCrops(UUID userId, Pageable pageable, CropType cropType, String category, String location, String variety) {
        Page<Crops> page;
        if ((cropType != null)||(category != null && !category.trim().isEmpty()) ||
                (location != null && !location.trim().isEmpty()) ||
                (variety != null && !variety.trim().isEmpty())) {

            page=cropRepository.findByUserIdFilter(userId,pageable,cropType, category, location, variety);
        }else{
            page=cropRepository.findByUserId(userId,pageable);
        }

        return cropMapper.toCard(page);
    }

    @Override
    public Page<CropCardDto> findAvailableCrops(Pageable pageable, CropType cropType, String category, String location, String variety) {
        Page<Crops> page;
        if ((cropType != null)||(category != null && !category.trim().isEmpty()) ||
                (location != null && !location.trim().isEmpty()) ||
                (variety != null && !variety.trim().isEmpty())) {

            page=cropRepository.findAvailableFiltered(
                    pageable,cropType, category, location, variety);
        }else{
            page=cropRepository.findAvailableCrops(pageable);
        }
        return cropMapper.toCard(page);
    }


    @Override
    public FarmerStats getFarmerStats(UUID farmerId){

        return new FarmerStats(
                cropRepository.totalCrops(farmerId),
                cropRepository.totalActiveAuctions(farmerId),
                orderRepository.countOrdersByFarmer(farmerId, OrderStatus.PENDING),
                orderRepository.countOrdersByFarmer(farmerId,OrderStatus.CONFIRMED),
                orderRepository.countOrdersByFarmer(farmerId,OrderStatus.SHIPPED),
                orderRepository.countOrdersByFarmer(farmerId,OrderStatus.DELIVERED),
                orderRepository.countActiveOrdersByFarmer(farmerId)
        );
    }

    @Override
    public FarmerCropDetailDto getFarmerCropById(UUID cropId) {
        Crops crop = cropRepository.findById(cropId)
                .orElseThrow(() -> new CropNotFoundException("Crop not found: " + cropId));
        List<Bid> bids = Collections.emptyList();
        if (crop.getCropType() == CropType.AUCTION) {
            if (crop.getAuction() == null) {
                throw new IllegalStateException("Auction crop without auction data: " + cropId);
            }
            UUID auctionId = crop.getAuction().getId();
            bids = bidRepository.findByAuctionIdOrderByCreatedAtDesc(auctionId);
        }
        return farmerCropMapper.toFarmerCropDetailDto(crop, bids);
    }


    @Override
    public void updateCrop(UUID id, CropUpdateRequest request, UUID userId) {
        Crops crop = cropRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Crop not found"));

        if (!crop.getUser().getId().equals(userId)){
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You are not allowed to update this crop");
        }
        try {
            boolean hasBids = bidRepository.existsByAuctionId(crop.getAuction().getId());
            if (hasBids) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Crop cannot be updated because bidding has already started");
            }
        }catch (RuntimeException e){
            log.error("has bids error : "+e);
        }
        if (request.getPricePerUnit() != null)
            crop.setPricePerUnit(request.getPricePerUnit());

        if (request.getQuantity() != null)
            crop.setQuantity(request.getQuantity());

        if (request.getDescription() != null)
            crop.setDescription(request.getDescription());
        cropRepository.save(crop);
    }




    @Override
    public RetailerCropDetailDto getCropDetail(UUID id) {
        Crops crop = cropRepository.findById(id)
                .orElseThrow(() -> new CropNotFoundException("Crop not found: " + id));
        UUID auctionId=crop.getAuction()!=null ?crop.getAuction().getId() : null;
        LocalDateTime auctionEndTime = crop.getAuction() != null ? crop.getAuction().getEndTime() : null;
        BigDecimal currentHighestBid = crop.getAuction() != null ? crop.getAuction().getCurrentHighestBid() : null;

        return new RetailerCropDetailDto(
                crop.getId(),
                crop.getCropName(),
                crop.getCropType(),
                crop.getCategory(),
                crop.getVariety(),
                crop.getQuantity(),
                crop.getUnit(),
                crop.getPricePerUnit(),
                crop.getLocation(),
                crop.getHarvestDate(),
                crop.getDescription(),
                crop.getImages() != null
                        ? crop.getImages().stream()
                        .map(CropImage::getImageUrl)
                        .toList()
                        : List.of(),
                auctionId,
                auctionEndTime,
                currentHighestBid
        );
    }
}
