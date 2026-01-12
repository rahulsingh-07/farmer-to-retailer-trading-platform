package com.example.userservice.records;

import com.example.userservice.enums.UserRole;
import com.example.userservice.enums.UserStatus;

import java.util.UUID;

public record PendingUserDTO(
        UUID userId,
        String fullName,
        String email,
        String phoneNumber,
        UserRole role,
        UserStatus status,
        String pmKisanId,      // only for FARMER
        String tradeLicenseUrl  // only for RETAILER
) {
}
