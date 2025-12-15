package com.example.userservice.enums;

public enum UserStatus {
    ACTIVE,      // User can login and perform actions
    INACTIVE,    // User account exists but cannot login
    BLOCKED,     // Admin blocked the user due to some
    PENDING      // Optional: For new users awaiting approval
}
