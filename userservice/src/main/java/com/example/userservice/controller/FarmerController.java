package com.example.userservice.controller;

import com.example.userservice.dto.*;
import com.example.userservice.enums.CropType;
import com.example.userservice.records.ApiResponse;
import com.example.userservice.records.CropCardDto;
import com.example.userservice.records.FarmerStats;
import com.example.userservice.serviceimp.*;
import com.example.userservice.mapper.CropMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/farmer")
@Slf4j
public class FarmerController {
    private final CropServiceImp cropServiceImp;
    private final NotificationServiceImp notificationServiceImp;
    private final OrderServiceImp orderServiceImp;
    private final CropMapper cropMapper;


    // add crop
    @PostMapping("/addCrop")
    public ResponseEntity<ApiResponse<Void>> createCrop(
            @Valid @RequestPart("request") CropRequest request,
            @RequestPart("images") MultipartFile[] files,
            @AuthenticationPrincipal CustomUserDetails principal) {

        cropServiceImp.createCrop(request, principal.getUserId(), files);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        "Crop added successfully",
                        null
                ));
    }

    // get farmer stats
    @GetMapping("/totalCrops")
    public ResponseEntity<ApiResponse<FarmerStats>> getFarmerStats(@AuthenticationPrincipal CustomUserDetails principal){
        return ResponseEntity
                .ok(new ApiResponse<>(
                        "Farmer stats",
                        cropServiceImp.getFarmerStats(principal.getUserId())
                ));
    }

    // Fetch all crops
    @GetMapping("/crops")
    public ResponseEntity<ApiResponse<Page<CropCardDto>>> getFilteredCrops(
            @RequestParam(required = false) CropType cropType,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String variety,
            @AuthenticationPrincipal CustomUserDetails principal,
            Pageable pageable) {

        return ResponseEntity
                .ok(new ApiResponse<>(
                        "crops fetches successfully",
                        cropServiceImp.findFarmerCrops(principal.getUserId(),
                                pageable,cropType, category, location, variety)
                ));
    }


    @GetMapping("/crop/{id}")
    public ResponseEntity<ApiResponse<FarmerCropDetailDto>> cropDetails(@PathVariable UUID id){
        return ResponseEntity
                .ok(new ApiResponse<>(
                        "Fetch Crop by id",
                        cropServiceImp.getFarmerCropById(id)
                ));
    }

    // update crop
    @PatchMapping("/crop/{id}/update")
    public ResponseEntity<ApiResponse<Void>> updateCrop(@PathVariable UUID id,@RequestBody CropUpdateRequest request,
                                                         @AuthenticationPrincipal CustomUserDetails principal){
        cropServiceImp.updateCrop(id,request,principal.getUserId());
        return ResponseEntity
                .ok(new ApiResponse<>(
                        "Crop Updated successfully",
                        null
                ));
    }


    // confirm order
    @PostMapping("/order/{orderId}/confirmed")
    public ResponseEntity<ApiResponse<Void>> confirmOrder(@PathVariable UUID orderId){
        orderServiceImp.getOrderConfirmed(orderId);
        return ResponseEntity
                .ok(new ApiResponse<>(
                        "Order Confirmed",
                        null
                ));
    }

    // ship order
    @PostMapping("/order/{orderId}/shipped")
    public ResponseEntity<ApiResponse<String>> shipOrder(
            @PathVariable UUID orderId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        orderServiceImp.markAsShipped(orderId, user.getUserId());

        return ResponseEntity.ok(
                new ApiResponse<>("Order marked as shipped. OTP sent to retailer email", null)
        );
    }

    // confirm order delivery
    @PostMapping("/order/{orderId}/delivered")
    public ResponseEntity<ApiResponse<Void>> orderDelivered(@PathVariable UUID orderId,
                                                            @AuthenticationPrincipal CustomUserDetails user){
        orderServiceImp.markAsDelivered(orderId,user.getUserId());

        return ResponseEntity
                .ok(new ApiResponse<>(
                        "Order Delivered Successfully",
                        null
                        )
                );
    }


}
