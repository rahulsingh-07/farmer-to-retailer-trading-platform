package com.example.userservice.serviceimp;

import com.example.userservice.dto.LoginResponse;
import com.example.userservice.dto.LoginUser;
import com.example.userservice.entity.PasswordResetToken;
import com.example.userservice.entity.Users;
import com.example.userservice.enums.UserRole;
import com.example.userservice.exception.UserNotFoundException;
import com.example.userservice.notification.EmailService;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ott.InvalidOneTimeTokenException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImpTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private TokenServiceImp tokenService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmailService emailService;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthServiceImp authService;

    private Users user;

    @BeforeEach
    void setup() {
        user = new Users();
        user.setId(UUID.randomUUID());
        user.setUsername("veer");
        user.setEmail("veer@test.com");
        user.setFullName("Veer Kumar");
        user.setRole(UserRole.ADMIN);
    }

    /* -------------------- LOGIN TESTS -------------------- */

    @Test
    void login_success() {
        LoginUser request = new LoginUser("veer", "password");

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userRepository.findByUsername("veer"))
                .thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(any(), any(), any()))
                .thenReturn("jwt-token");

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("Login successful", response.getMessage());
    }

    @Test
    void login_authenticationFails() {
        LoginUser request = new LoginUser("veer", "wrong");

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        assertThrows(BadCredentialsException.class,
                () -> authService.login(request));
    }

    @Test
    void login_badCredentialsException() {
        LoginUser request = new LoginUser("veer", "bad");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid"));

        assertThrows(BadCredentialsException.class,
                () -> authService.login(request));
    }

    @Test
    void login_userNotFoundAfterAuth() {
        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userRepository.findByUsername("veer"))
                .thenReturn(Optional.empty());
        LoginUser request = new LoginUser("veer", "bad");

        assertThrows(UserNotFoundException.class,
                () -> authService.login(request));
    }

    /* -------------------- SET PASSWORD TESTS -------------------- */

    @Test
    void setPassword_success() {
        String token = "valid-token";
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUserId(user.getId());

        when(tokenService.getToken(token)).thenReturn(resetToken);
        when(tokenService.validateToken(token)).thenReturn(true);
        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPass"))
                .thenReturn("encoded");

        authService.setPassword(token, "newPass");

        assertEquals("encoded", user.getPassword());
        assertFalse(user.isUpdatePasswordRequired());
        assertEquals(0, user.getPasswordResetAttempts());

        verify(userRepository).save(user);
        verify(tokenService).deleteToken(token);
    }

    @Test
    void setPassword_invalidToken() {
        when(tokenService.validateToken("bad-token"))
                .thenReturn(false);

        assertThrows(InvalidOneTimeTokenException.class,
                () -> authService.setPassword("bad-token", "pass"));
    }

    @Test
    void setPassword_userNotFound() {
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUserId(UUID.randomUUID());

        when(tokenService.getToken("token")).thenReturn(resetToken);
        when(tokenService.validateToken("token")).thenReturn(true);
        when(userRepository.findById(any()))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> authService.setPassword("token", "pass"));
    }

    /* -------------------- FORGET PASSWORD TESTS -------------------- */

    @Test
    void forgetPassword_success() {
        when(userRepository.findByEmail("veer@test.com"))
                .thenReturn(Optional.of(user));
        when(tokenService.generateToken(user.getId()))
                .thenReturn("reset-link");

        String response = authService.forgetPassword("veer@test.com");

        assertEquals("link send to your email", response);
        assertNotNull(user.getUpdateAt());

        verify(emailService).sendPasswordSetupEmail(
                "veer@test.com",
                "reset-link",
                "veer",
                "Veer Kumar"
        );

    }

    @Test
    void forgetPassword_emailNotFound() {
        when(userRepository.findByEmail("unknown@test.com"))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> authService.forgetPassword("unknown@test.com"));
    }
}
