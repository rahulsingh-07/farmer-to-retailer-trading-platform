package com.example.userservice.controller;

import com.example.userservice.entity.Users;
import com.example.userservice.enums.UserStatus;
import com.example.userservice.serviceImp.AdminServiceImp;
import com.example.userservice.serviceImp.UserServiceImp;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    private final AdminServiceImp adminService;
    private final UserServiceImp userService;

    @PatchMapping("/user/{id}/status")
    public ResponseEntity<?> changeStatus(@PathVariable UUID id, @RequestParam UserStatus status) {
        Users updatedUser = adminService.updateStatus(id, status);

        return ResponseEntity.ok(
                Map.of(
                        "message", "Status updated successfully",
                        "userId", updatedUser.getId(),
                        "newStatus", updatedUser.getStatus()
                )
        );
    }



    @GetMapping("/pendingUsers")
    public ResponseEntity<?> pendingUser(){

        List<Users> pendingUsers = adminService.getPending();

        List<Map<String, Object>> response = pendingUsers.stream()
                .map(user -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("userId", user.getId());
                    map.put("fullName", user.getFullName());
                    map.put("email", user.getEmail());
                    map.put("phoneNumber", user.getPhoneNumber());
                    map.put("status", user.getStatus());
                    return map;
                })
                .toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/newAdmin")
    public ResponseEntity<Users> retailer(@Valid @RequestBody Users user){
        return ResponseEntity.ok(userService.createAdminUser(user));

    }

    @GetMapping("/totalUsers")
    public ResponseEntity<Map<String,Integer>> totalNumbers(){
        return ResponseEntity.ok(userService.getAdminNumbers());
    }


}
