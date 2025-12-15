package com.example.userservice.serviceImp;

import com.example.userservice.entity.Users;
import com.example.userservice.enums.UserStatus;
import com.example.userservice.exception.UserRejectedException;
import com.example.userservice.notification.EmailService;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
@Service
@RequiredArgsConstructor
public class AdminServiceImp implements AdminService {
    private final UserRepository userRepository;
    private final TokenServiceImp tokenService;
    private final EmailService emailService;

    @Override
    public Users updateStatus(UUID id, UserStatus newStatus) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (newStatus == UserStatus.ACTIVE) {
            user.setStatus(newStatus);
            user.setUpdateAt(LocalDateTime.now());
            user.setUpdatePasswordRequired(true);
            user.setPasswordResetAttempts(0);
            String msg=tokenService.generateToken(user.getId());
            String userName=user.getUsername();
            emailService.sendPasswordSetupEmail(user.getEmail(),msg,userName,user.getFullName());
        } else if (newStatus == UserStatus.INACTIVE) {
            emailService.sendRejectionNotice(user.getEmail(),user.getFullName());
            userRepository.delete(user);
            throw new UserRejectedException("User rejected and removed from system");
        }
        return user;
    }


    public Page<Users> getPending(Pageable pageable) {
        return userRepository.findByStatus(UserStatus.PENDING, pageable);
    }




}
