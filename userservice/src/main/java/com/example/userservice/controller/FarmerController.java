package com.example.userservice.controller;

import com.example.userservice.dto.CropRequest;
import com.example.userservice.dto.CropResponse;
import com.example.userservice.dto.NotificationDTO;
import com.example.userservice.dto.NotificationResponse;
import com.example.userservice.entity.Notification;
import com.example.userservice.entity.Users;
import com.example.userservice.farmerService.FarmerService;
import com.example.userservice.serviceImp.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/farmer")
public class FarmerController {
    private final FarmerService farmerService;


    @PostMapping("/addCrop")
    public ResponseEntity<?> createCrop(
            @Valid @RequestPart("request") CropRequest request,
            @RequestPart("images") MultipartFile[] files,
            @AuthenticationPrincipal CustomUserDetails principal) {

        String msg = farmerService.createCrop(request, principal.getUserId(), files);
        return ResponseEntity.ok(Map.of("message", msg));
    }


    @GetMapping("/crops")
    public List<CropRequest> getMyCrops(@AuthenticationPrincipal CustomUserDetails principal) {
        return farmerService.getMyCrop(principal.getUserId());
    }

    @GetMapping("/notifications")
    public List<NotificationDTO> getFarmerNotifications(@AuthenticationPrincipal CustomUserDetails principal ) {
        return farmerService.getNotifications(principal.getUserId());
    }

    @GetMapping("/notifications/{id}")
    public NotificationResponse getNotificationDetails(@PathVariable UUID id){
        return farmerService.getNotificationsDetails(id);
    }
}
