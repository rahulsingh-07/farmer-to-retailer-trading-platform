package com.example.userservice.controller;

import com.example.userservice.dto.*;
import com.example.userservice.enums.OrderStatus;
import com.example.userservice.enums.UserRole;
import com.example.userservice.records.*;
import com.example.userservice.serviceimp.NotificationServiceImp;
import com.example.userservice.serviceimp.OrderServiceImp;
import com.example.userservice.serviceimp.CustomUserDetails;
import com.example.userservice.serviceimp.UserServiceImp;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/user")
public class UserController {
    private final UserServiceImp userService;
    private final OrderServiceImp orderServiceImp;
    private final NotificationServiceImp notificationServiceImp;

    // update user details
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateUserPartially(
            @PathVariable UUID id,
            @Valid @RequestBody UserUpdateDTO patchDTO) {
        userService.updateUserPartially(id, patchDTO);
        return ResponseEntity
                .ok(new ApiResponse<>(
                "Update successfully",
                null
        ));
    }

    // Get Orders
    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<Page<OrderCardDto>>> getAllUserOrder(
            @RequestParam(required = false) OrderStatus status,
            @AuthenticationPrincipal CustomUserDetails principal,
            Pageable pageable){
        UserRole role = principal.getUserRole();
        return ResponseEntity
                .ok(new ApiResponse<>(
                        "get all orders",
                        orderServiceImp.getAllUserOrder(pageable,principal.getUserId(),role,status)
                ));
    }

    // get order by id
    @GetMapping("/orders/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderFarmer(@PathVariable UUID id){
        return ResponseEntity
                .ok(new ApiResponse<>(
                        "Get Order by id",
                        orderServiceImp.getOrderById(id)
                ));
    }

    //give feedback and rating
    @PostMapping("/orders/{id}/review")
    public ResponseEntity<ApiResponse<Void>> makeReview(@PathVariable UUID id,
                                                    @RequestBody ReviewRequest request,
                                                    @AuthenticationPrincipal CustomUserDetails principal){
        orderServiceImp.createReview(id, request, principal);
        return ResponseEntity
                .ok(new ApiResponse<>(
                        "Successfully make review",
                        null
                ));
    }



    // Get Notifications
    @GetMapping("/notifications")
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getNotifications(@AuthenticationPrincipal CustomUserDetails principal ) {
        return ResponseEntity
                .ok(new ApiResponse<>(
                        "Get all Notifications",
                        notificationServiceImp.getNotifications(principal.getUserId())
                ));
    }

    // Get Notification by id
    @GetMapping("/notifications/{id}")
    public ResponseEntity<ApiResponse<NotificationResponse>> getNotificationDetails(@PathVariable UUID id){
        return ResponseEntity
                .ok(new ApiResponse<>(
                        "Get Notification by id",
                        notificationServiceImp.getNotification(id)
                ));
    }

    // Delete Notification
    @DeleteMapping("/notifications/delete")
    public ResponseEntity<ApiResponse<Void>> delete(@RequestParam("ids") List<UUID> ids){
        notificationServiceImp.deleteNotifications(ids);
        return ResponseEntity
                .ok(new ApiResponse<>(
                        "Notifications Delete successfully",
                        null
                ));
    }

    // read Notification
    @DeleteMapping("/notifications/read")
    public ResponseEntity<ApiResponse<Void>> readNotifications(@RequestParam("ids") List<UUID> ids){
        notificationServiceImp.readNotifications(ids);
        return ResponseEntity
                .ok(new ApiResponse<>(
                        "Notifications read successfully",
                        null
                ));
    }

    // Retailer stats
    @GetMapping("retailer/dashboard")
    public ResponseEntity<ApiResponse<RetailerStats>> getValues(@AuthenticationPrincipal CustomUserDetails principal){
        return ResponseEntity
                .ok(new ApiResponse<>(
                        "Retailer Dashboard stats",
                        userService.dashboardValues(principal.getUserId())
                ));
    }

}
