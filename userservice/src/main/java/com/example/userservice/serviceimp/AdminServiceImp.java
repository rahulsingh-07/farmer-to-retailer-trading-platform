package com.example.userservice.serviceimp;

import com.example.userservice.config.UsernameGenerator;
import com.example.userservice.dto.AdminDTO;
import com.example.userservice.entity.Users;
import com.example.userservice.enums.UserRole;
import com.example.userservice.enums.UserStatus;
import com.example.userservice.exception.UserRejectedException;
import com.example.userservice.notification.EmailService;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
@Service
@RequiredArgsConstructor
public class AdminServiceImp implements AdminService {
    private final UserRepository userRepository;
    private final TokenServiceImp tokenService;
    private final EmailService emailService;
    private final UsernameGenerator usernameGenerator;
    private final PasswordEncoder passwordEncoder;

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


    public String createAdminUser(AdminDTO admin) {
        try {
            String generatedPwd = usernameGenerator.generatePassword();
            String generatedUsername = usernameGenerator.generate(UserRole.ADMIN);

            Users req = new Users();
            req.setEmail(admin.getEmail());
            req.setPhoneNumber(admin.getPhoneNumber());
            req.setFullName(admin.getFullName());
            req.setUsername(generatedUsername);
            req.setPassword(passwordEncoder.encode(generatedPwd));
            req.setRole(UserRole.ADMIN);
            req.setStatus(UserStatus.ACTIVE);
            req.setCreatedAt(LocalDateTime.now());
            Users savedUser = userRepository.save(req);

            String msg = tokenService.generateToken(savedUser.getId());
            String userName = req.getUsername();
            emailService.sendPasswordSetupEmail(req.getEmail(), msg, userName, req.getFullName());
            return "Admin create successfully";
        } catch (RuntimeException e) {
            throw new RuntimeException("Fail To Create Admin",e);
        }
    }




}
