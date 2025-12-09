package com.example.userservice.service;

import com.example.userservice.dto.LoginResponse;
import com.example.userservice.dto.LoginUser;
import com.example.userservice.entity.PasswordResetToken;
import com.example.userservice.entity.Users;
import com.example.userservice.enums.UserRole;
import com.example.userservice.enums.UserStatus;
import com.example.userservice.exception.UserNotFoundException;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.serviceImp.AuthServiceImp;
import com.example.userservice.serviceImp.TokenServiceImp;
import com.example.userservice.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private  UserRepository userRepository;
    @Mock
    private  JwtUtil jwtUtil;
    @Mock
    private  AuthenticationManager authenticationManager;
    @Mock
    private  TokenServiceImp tokenService;
    @Mock
    private  PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImp authService;


    @Test
    void login_success() {
        LoginUser loginRequest = new LoginUser();
        loginRequest.setUsername("veer");
        loginRequest.setPassword("veer@123");
        Authentication auth = mock(Authentication.class);

        when(auth.isAuthenticated()).thenReturn(true);
        when(authenticationManager.authenticate(any())).thenReturn(auth);

        Users user = new Users();
        UUID id = UUID.randomUUID();
        user.setId(id);
        user.setUsername("veer");
        user.setRole(UserRole.FARMER);
        user.setFullName("veer");
        user.setStatus(UserStatus.INACTIVE);
        user.setPhoneNumber("4578457845");
        user.setEmail("veer@gmail.com");

        when(userRepository.findByUsername("veer")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("veer", "FARMER")).thenReturn("fake-jwt-token");

        LoginResponse response = authService.login(loginRequest);

        assertEquals("fake-jwt-token", response.getToken());
        assertEquals("Login successful", response.getMessage());

    }

//    @Transactional
//    public void setPassword(String token, String rawPassword) {
//        PasswordResetToken resetToken = tokenService.getToken(token);
//
//        // Validate token
//        if (!tokenService.validateToken(token)) {
//            throw new RuntimeException("Invalid or expired token");
//        }
//
//        // Set password for user
//        UUID userId = resetToken.getUserId();
//        Users user = userRepository.findById(userId)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        user.setPassword(passwordEncoder.encode(rawPassword));
//        userRepository.save(user);
//        tokenService.deleteToken(token);
//    }

}
