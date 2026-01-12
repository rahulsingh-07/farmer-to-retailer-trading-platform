package com.example.userservice.serviceimp;

import com.example.userservice.dto.LoginUser;
import com.example.userservice.entity.PasswordResetToken;
import com.example.userservice.entity.Users;
import com.example.userservice.enums.UserRole;
import com.example.userservice.enums.UserStatus;
import com.example.userservice.exception.UserNotFoundException;
import com.example.userservice.records.LoginResponse;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.ott.InvalidOneTimeTokenException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImpTest {
    @Mock private UserRepository userRepository;
    @Mock private JwtUtil jwtUtil;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private TokenServiceImp tokenServiceImp;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmailServiceImp emailServiceImp;
    @InjectMocks
    private AuthServiceImp authServiceImp;

    //=== LOGIN TESTING ===
    @Test
    void login_success_shouldReturnToken(){
        LoginUser loginUser=new LoginUser();
        loginUser.setUsername("ADMIN-123");
        loginUser.setPassword("password@123");

        Users user = new Users();
        user.setId(UUID.randomUUID());
        user.setUsername("ADMIN-123");
        user.setRole(UserRole.ADMIN);

        when(userRepository.findByUsername("ADMIN-123")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(user.getUsername(),user.getRole().name(),user.getId()))
                .thenReturn("mock-jwt-token");

        LoginResponse response=authServiceImp.login(loginUser);
        assertNotNull(response);
        assertEquals("mock-jwt-token",response.token());

        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken(loginUser.getUsername(),loginUser.getPassword())
        );
        verify(userRepository).findByUsername("ADMIN-123");
        verify(jwtUtil).generateToken(
                "ADMIN-123",
                "ADMIN",
                user.getId()
        );
    }

    @Test
    void login_userNotFound_shouldThrowException(){
        LoginUser loginUser=new LoginUser();
        loginUser.setUsername("ADMIN-123");
        loginUser.setPassword("password123");

        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any()))
                .thenReturn(auth);

        when(userRepository.findByUsername("ADMIN-123")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                ()->authServiceImp.login(loginUser));
        verify(authenticationManager).authenticate(any());
        verify(userRepository).findByUsername("ADMIN-123");
        verify(jwtUtil,never()).generateToken(any(),any(),any());
    }

    @Test
    void login_invalidCredentials_shouldThrowException() {
        // Arrange
        LoginUser loginUser = new LoginUser("rahul", "wrong-pass");

        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager)
                .authenticate(any());

        // Act + Assert
        assertThrows(BadCredentialsException.class,
                () -> authServiceImp.login(loginUser));

        verify(authenticationManager).authenticate(any());
        verify(userRepository, never()).findByUsername(any());
        verify(jwtUtil, never()).generateToken(any(), any(), any());
    }

    //=== SET PASSWORD TESTING ===
    @Test
    void setPassword_success_shouldUpdateAndDeleteToken(){
        String token = "valid-token";
        String rawPassword = "newPassword@123";
        UUID userId = UUID.randomUUID();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUserId(userId);
        Users user = new Users();
        user.setId(userId);
        user.setUpdatePasswordRequired(true);
        user.setStatus(UserStatus.INACTIVE);
        user.setPasswordResetAttempts(3);

        when(tokenServiceImp.getToken(token)).thenReturn(resetToken);
        when(tokenServiceImp.validateToken(token)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(rawPassword)).thenReturn("encoded-password");

        authServiceImp.setPassword(token, rawPassword);
        assertEquals("encoded-password", user.getPassword());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertFalse(user.isUpdatePasswordRequired());
        assertEquals(0, user.getPasswordResetAttempts());

        verify(tokenServiceImp).getToken(token);
        verify(tokenServiceImp).validateToken(token);
        verify(userRepository).findById(userId);
        verify(passwordEncoder).encode(rawPassword);
        verify(userRepository).save(user);
        verify(tokenServiceImp).deleteToken(token);
    }

    @Test
    void setPassword_invalidToken_shouldThrowException(){
        String token = "invalid-token";
        String rawPassword = "password";

        when(tokenServiceImp.getToken(token)).thenReturn(new PasswordResetToken());
        when(tokenServiceImp.validateToken(token)).thenReturn(false);

        assertThrows(InvalidOneTimeTokenException.class,
                ()-> authServiceImp.setPassword(token,rawPassword));
        verify(tokenServiceImp).getToken(token);
        verify(tokenServiceImp).validateToken(token);
        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).save(any());
        verify(tokenServiceImp, never()).deleteToken(any());
    }

    @Test
    void setPassword_userNotFound_shouldThrowException() {
        String token = "valid-token";
        String rawPassword = "password";
        UUID userId = UUID.randomUUID();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUserId(userId);

        when(tokenServiceImp.getToken(token)).thenReturn(resetToken);
        when(tokenServiceImp.validateToken(token)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> authServiceImp.setPassword(token, rawPassword)
        );

        // Verify
        verify(tokenServiceImp).getToken(token);
        verify(tokenServiceImp).validateToken(token);
        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any());
        verify(tokenServiceImp, never()).deleteToken(any());
    }

    //=== FORGET PASSWORD ===
    @Test
    void forgetPassword_success_shouldGenerateTokenAndSendEmail(){
        String email = "user@example.com";
        UUID userId = UUID.randomUUID();

        Users user = new Users();
        user.setId(userId);
        user.setEmail(email);
        user.setUsername("john123");
        user.setFullName("John Doe");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(tokenServiceImp.generateToken(userId)).thenReturn("token123");

        authServiceImp.forgetPassword(email);

        assertNotNull(user.getUpdateAt());

        verify(userRepository).findByEmail(email);
        verify(tokenServiceImp).generateToken(userId);
        verify(emailServiceImp).sendPasswordSetupEmail(
                email,
                "token123",
                "john123",
                "John Doe"
        );
    }

    @Test
    void forgetPassword_userNotFound_shouldThrowException() {
        String email = "unknown@example.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> authServiceImp.forgetPassword(email));

        verify(userRepository).findByEmail(email);
        verify(tokenServiceImp, never()).generateToken(any());
        verify(emailServiceImp, never()).sendPasswordSetupEmail(any(), any(), any(), any());
    }


}
