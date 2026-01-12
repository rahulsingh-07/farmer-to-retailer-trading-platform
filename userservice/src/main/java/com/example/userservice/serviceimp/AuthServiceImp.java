package com.example.userservice.serviceimp;

import com.example.userservice.dto.LoginUser;
import com.example.userservice.entity.PasswordResetToken;
import com.example.userservice.entity.Users;
import com.example.userservice.enums.UserStatus;
import com.example.userservice.exception.UserNotFoundException;
import com.example.userservice.records.LoginResponse;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.AuthService;
import com.example.userservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.ott.InvalidOneTimeTokenException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImp implements AuthService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final TokenServiceImp tokenServiceImp;
    private final PasswordEncoder passwordEncoder;
    private final EmailServiceImp emailServiceImp;

    @Override
    public LoginResponse login(LoginUser loginRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );
        Users user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        String token = jwtUtil.generateToken(
                user.getUsername(),
                user.getRole().name(),
                user.getId()
        );
        return new LoginResponse(token);
    }

    @Override
    @Transactional
    public void setPassword(String token, String rawPassword) {
        PasswordResetToken resetToken = tokenServiceImp.getToken(token);
        // Validate token
        if (!tokenServiceImp.validateToken(token)) {
            throw new InvalidOneTimeTokenException("Invalid or expired token");
        }

        UUID userId = resetToken.getUserId();
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setStatus(UserStatus.ACTIVE);
        user.setUpdatePasswordRequired(false);
        user.setPasswordResetAttempts(0);
        userRepository.save(user);
        tokenServiceImp.deleteToken(token);
    }

    @Override
    public void forgetPassword(String email){
        Users user=userRepository.findByEmail(email)
                .orElseThrow(()->new UserNotFoundException("User with email not exist"));

        user.setUpdateAt(LocalDateTime.now());
        String msg= tokenServiceImp.generateToken(user.getId());
        String userName=user.getUsername();
        emailServiceImp.sendPasswordSetupEmail(user.getEmail(),msg,userName,user.getFullName());
    }

}
