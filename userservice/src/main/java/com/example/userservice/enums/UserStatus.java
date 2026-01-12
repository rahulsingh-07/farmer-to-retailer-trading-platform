package com.example.userservice.enums;

public enum UserStatus {
    PENDING, // registration requirest
    INACTIVE, // approve by admin
    ACTIVE,   // update password
    REJECTED  // rejected by admin
}
