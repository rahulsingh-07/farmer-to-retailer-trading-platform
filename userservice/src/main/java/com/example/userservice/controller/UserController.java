package com.example.userservice.controller;

import com.example.userservice.dto.*;
import com.example.userservice.entity.Users;
import com.example.userservice.enums.OrderStatus;
import com.example.userservice.enums.UserRole;
import com.example.userservice.farmer.NotificationService;
import com.example.userservice.order.OrderService;
import com.example.userservice.serviceimp.CustomUserDetails;
import com.example.userservice.serviceimp.UserServiceImp;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/user")
public class UserController {
    private final UserServiceImp userService;
    private final OrderService orderService;
    private final NotificationService notificationService;

    @PatchMapping("/{id}")
    public ResponseEntity<Users> updateUserPartially(
            @PathVariable UUID id,
            @Valid @RequestBody UserUpdateDTO patchDTO) {
        Users updatedUser = userService.updateUserPartially(id, patchDTO);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/farmer/orders")
    public ResponseEntity<List<OrderFarmerResponse>> getAllFarmerOrder( @RequestParam(required = false)OrderStatus status,@AuthenticationPrincipal CustomUserDetails principal){
        return ResponseEntity.ok(orderService.getAllFarmerOrder(principal.getUserId(),status));
    }

    @GetMapping("/retailer/orders")
    public ResponseEntity<List<OrderRetailerResponse>> getAllRetailerOrder(@RequestParam(required = false) OrderStatus status,@AuthenticationPrincipal CustomUserDetails principal){
        return ResponseEntity.ok(orderService.getAllRetailerOrder(principal.getUserId(),status));
    }

    @GetMapping("/retailer/orders/{id}")
    public ResponseEntity<OrderResponse> getOrderRetailer(@PathVariable UUID id){
        return ResponseEntity.ok(orderService.getRetailerOrder(id));
    }

    @GetMapping("/farmer/orders/{id}")
    public ResponseEntity<OrderResponse> getOrderFarmer(@PathVariable UUID id){
        return ResponseEntity.ok(orderService.getFarmerOrder(id));
    }
    @GetMapping("/notifications")
    public List<NotificationDTO> getFarmerNotifications(@AuthenticationPrincipal CustomUserDetails principal ) {
        String authority = principal.getAuthorities().iterator().next().getAuthority();
        String roleName = authority.substring(5);  // Remove "ROLE_"

        UserRole role = UserRole.valueOf(roleName);
        return notificationService.getNotifications(principal.getUserId(),role);
    }

    @GetMapping("/notifications/{id}")
    public NotificationResponse getNotificationDetails(@PathVariable UUID id){
        return notificationService.getNotification(id);
    }
    @DeleteMapping("/notifications/delete")
    public ResponseEntity<Map<String,String>> delete(@RequestParam("ids") List<UUID> ids){
        return ResponseEntity.ok(Map.of("message",notificationService.deleteNotifications(ids)));
    }

    @GetMapping("retailer/dashboard")
    public ResponseEntity<Map<String,Long>> getValues(@AuthenticationPrincipal CustomUserDetails principal){
        return ResponseEntity.ok(userService.dashboardValues(principal.getUserId()));
    }
}
