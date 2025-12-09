package com.example.userservice.service;

import com.example.userservice.entity.Users;
import com.example.userservice.enums.UserStatus;

import java.util.UUID;

public interface AdminService {
    Users updateStatus(UUID id, UserStatus newStatus);
}
