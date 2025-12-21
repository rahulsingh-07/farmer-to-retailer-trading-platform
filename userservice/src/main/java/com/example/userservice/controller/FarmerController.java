package com.example.userservice.controller;

import com.example.userservice.dto.*;
import com.example.userservice.farmer.FarmerService;
import com.example.userservice.farmer.NotificationService;
import com.example.userservice.order.OrderService;
import com.example.userservice.serviceimp.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/farmer")
@Slf4j
public class FarmerController {
    private final FarmerService farmerService;
    private final NotificationService notificationService;
    private final OrderService orderService;


    @PostMapping("/addCrop")
    public ResponseEntity<Map<String,String>> createCrop(
            @Valid @RequestPart("request") CropRequest request,
            @RequestPart("images") MultipartFile[] files,
            @AuthenticationPrincipal CustomUserDetails principal) {

        String msg = farmerService.createCrop(request, principal.getUserId(), files);
        return ResponseEntity.ok(Map.of("message", msg));
    }

    @GetMapping("/totalCrops")
    public ResponseEntity<Map<String,Long>> totalCrops(@AuthenticationPrincipal CustomUserDetails principal){
        return ResponseEntity.ok(farmerService.getCropNumber(principal.getUserId()));
    }

    @GetMapping("/crop/{id}")
    public ResponseEntity<FarmerCropDetailDto> cropDetails(@PathVariable UUID id){
        return ResponseEntity.ok(farmerService.getFarmerCrop(id));
    }

    @GetMapping("/crops")
    public ResponseEntity<List<CropResponse>> getMyCrops(@AuthenticationPrincipal CustomUserDetails principal) {
        List<CropResponse> crops = farmerService.getMyCrops(principal.getUserId());
        return ResponseEntity.ok(crops);
    }

    @PostMapping("/order/{orderId}/confirmed")
    public ResponseEntity<Map<String,String>> confirmOrder(@PathVariable UUID orderId){
        return ResponseEntity.ok(Map.of("message",orderService.getOrderConfirmed(orderId)));
    }








}
