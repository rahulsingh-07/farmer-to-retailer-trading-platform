package com.example.userservice.serviceimp;

import com.example.userservice.entity.RetailerDetails;
import com.example.userservice.exception.UserNotFoundException;
import com.example.userservice.records.AdminCreateRequest;
import com.example.userservice.records.AdminStatsDto;
import com.example.userservice.records.PendingUserDTO;
import com.example.userservice.util.UsernameGenerator;
import com.example.userservice.entity.Users;
import com.example.userservice.enums.UserRole;
import com.example.userservice.enums.UserStatus;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImp implements AdminService {
    private final UserRepository userRepository;
    private final TokenServiceImp tokenService;
    private final EmailServiceImp emailServiceImp;
    private final UsernameGenerator usernameGenerator;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryServiceImp cloudinaryServiceImp;

    @Override
    @Transactional
    public void updateStatus(UUID id, UserStatus newStatus, String admin) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        if (newStatus == UserStatus.INACTIVE) {
            user.setStatus(UserStatus.INACTIVE);
            user.setUpdateAt(LocalDateTime.now());
            user.setUpdatePasswordRequired(true);
            user.setPasswordResetAttempts(0);
            user.setApprovedBy(admin);
            String token = tokenService.generateToken(user.getId());
            try {
                emailServiceImp.sendPasswordSetupEmail(
                        user.getEmail(),
                        token,
                        user.getUsername(),
                        user.getFullName()
                );
            } catch (Exception e) {
                log.error("Password setup email failed", e);
            }
            userRepository.save(user);
            return;
        }
        if (newStatus == UserStatus.REJECTED) {
            try {
                emailServiceImp.sendRejectionNotice(
                        user.getEmail(),
                        user.getFullName()
                );
            } catch (Exception e) {
                log.error("Rejection email failed", e);
            }
            RetailerDetails rd = user.getRetailerDetails();
            if (rd != null && rd.getTradeLicenseCloudinaryPublicId() != null) {
                cloudinaryServiceImp.deleteFile(
                        rd.getTradeLicenseCloudinaryPublicId()
                );
                log.info("Deleted trade license from Cloudinary for user {}", user.getId());
            }
            userRepository.delete(user);
            return;
        }
        throw new IllegalArgumentException("Unsupported status: " + newStatus);
    }



    @Override
    public Page<PendingUserDTO> getPending(Pageable pageable) {
        Page<Users> pendingUsersPage=userRepository.findByStatus(UserStatus.PENDING, pageable);
        return pendingUsersPage.map(user -> {
            String pmKisanId = null;
            String tradeLicenseUrl = null;
            if (user.getRole() == UserRole.FARMER && user.getFarmerDetails() != null) {
                pmKisanId = user.getFarmerDetails().getPmKisanId();
            }
            if (user.getRole() == UserRole.RETAILER && user.getRetailerDetails() != null) {
                tradeLicenseUrl = user.getRetailerDetails().getTradeLicenseUrl();
            }
            return new PendingUserDTO(
                    user.getId(),
                    user.getFullName(),
                    user.getEmail(),
                    user.getPhoneNumber(),
                    user.getRole(),
                    user.getStatus(),
                    pmKisanId,
                    tradeLicenseUrl
            );
        });
    }

    @Override
    public void createAdminUser(AdminCreateRequest admin, String approvedBy) {
            String generatedPwd = usernameGenerator.generatePassword();
            String generatedUsername = usernameGenerator.generate(UserRole.ADMIN);
            Users req = new Users();
            req.setEmail(admin.email());
            req.setPhoneNumber(admin.phoneNumber());
            req.setApprovedBy(approvedBy);
            req.setFullName(admin.fullName());
            req.setUsername(generatedUsername);
            req.setPassword(passwordEncoder.encode(generatedPwd));
            req.setRole(UserRole.ADMIN);
            req.setStatus(UserStatus.INACTIVE);
            req.setCreatedAt(LocalDateTime.now());
            Users savedUser = userRepository.save(req);
            String msg = tokenService.generateToken(savedUser.getId());
            String userName = req.getUsername();
            try{
                emailServiceImp.sendPasswordSetupEmail(req.getEmail(), msg, userName, req.getFullName());
            } catch (Exception e) {
                log.error("Failed to send set Password email to {}",savedUser.getEmail(),e);
            }
    }
    @Override
    public AdminStatsDto getUserStats(String username) {
        return new AdminStatsDto(
                userRepository.totalUsers(),
                userRepository.totalPending(),
                userRepository.totalAdmin(),
                userRepository.totalApproved(username)
        );
    }
}
