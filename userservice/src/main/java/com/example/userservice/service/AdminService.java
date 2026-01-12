package com.example.userservice.service;

import com.example.userservice.enums.UserStatus;
import com.example.userservice.records.AdminCreateRequest;
import com.example.userservice.records.AdminStatsDto;
import com.example.userservice.records.PendingUserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdminService {
    void updateStatus(UUID id, UserStatus newStatus,String admin);
    void createAdminUser(AdminCreateRequest admin, String approvedBy);
    Page<PendingUserDTO> getPending(Pageable pageable);
    AdminStatsDto getUserStats(String username);
}
