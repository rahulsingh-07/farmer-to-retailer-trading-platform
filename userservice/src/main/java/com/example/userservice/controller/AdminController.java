package com.example.userservice.controller;

import com.example.userservice.enums.UserStatus;
import com.example.userservice.records.AdminCreateRequest;
import com.example.userservice.records.AdminStatsDto;
import com.example.userservice.records.ApiResponse;
import com.example.userservice.records.PendingUserDTO;
import com.example.userservice.serviceimp.AdminServiceImp;
import com.example.userservice.serviceimp.CustomUserDetails;
import com.example.userservice.serviceimp.UserServiceImp;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class
AdminController {
    private final AdminServiceImp adminServiceImp;
    private final UserServiceImp userServiceImp;

    // approved and reject pending request
    @PatchMapping("/user/{id}/status")
    public ResponseEntity<ApiResponse<Void>> changeStatus(@PathVariable UUID id,
                                                          @RequestParam UserStatus status,
                                                          @AuthenticationPrincipal CustomUserDetails principal) {

        adminServiceImp.updateStatus(id,status,principal.getUsername());

        return ResponseEntity.ok(
                new ApiResponse<Void>(
                        "Status updated successfully",
                        null)
        );
    }

    // fetch total pending users
    @GetMapping("/pendingUsers")
    public ResponseEntity<ApiResponse<Page<PendingUserDTO>>> pendingUsers(Pageable pageable) {
        return ResponseEntity
                .ok(new ApiResponse<>(
                        "Pending Users fetch successfully",
                        adminServiceImp.getPending(pageable)
                ));
    }

    // create new admin
    @PostMapping("/newAdmin")
    public ResponseEntity<ApiResponse<Void>> retailer(@Valid @RequestBody AdminCreateRequest admin,
                                                      @AuthenticationPrincipal CustomUserDetails principal){
        adminServiceImp.createAdminUser(admin,principal.getUsername());
        return ResponseEntity.status(HttpStatus.SC_CREATED)
                .body(new ApiResponse<>(
                        "Admin created successfully",
                        null));

    }

    // get stats for admin dashboard
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AdminStatsDto>> adminStats(@AuthenticationPrincipal CustomUserDetails principal){
        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Dashboard stats",
                        adminServiceImp.getUserStats(principal.getUsername())
                )
        );
    }


}
