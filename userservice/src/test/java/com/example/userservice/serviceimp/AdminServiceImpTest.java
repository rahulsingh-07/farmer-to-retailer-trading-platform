package com.example.userservice.serviceimp;

import com.example.userservice.config.UsernameGenerator;
import com.example.userservice.dto.AdminDTO;
import com.example.userservice.entity.Users;
import com.example.userservice.enums.UserRole;
import com.example.userservice.enums.UserStatus;
import com.example.userservice.exception.UserRejectedException;
import com.example.userservice.notification.EmailService;
import com.example.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceImpTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenServiceImp tokenService;

    @Mock
    private EmailService emailService;

    @Mock
    private UsernameGenerator usernameGenerator;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminServiceImp adminService;

    @Test
    void updateStatus_toActive_setsFieldsAndSendsEmail() {
        // Arrange
        UUID userId = UUID.randomUUID();
        Users user = new Users();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        user.setUsername("testuser");
        user.setStatus(UserStatus.PENDING);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(tokenService.generateToken(userId)).thenReturn("mock-token");

        // Act
        Users result = adminService.updateStatus(userId, UserStatus.ACTIVE);

        // Assert
        assertEquals(UserStatus.ACTIVE, result.getStatus());
        assertNotNull(result.getUpdateAt());
        assertTrue(result.isUpdatePasswordRequired());
        assertEquals(0, result.getPasswordResetAttempts());

        verify(emailService, times(1)).sendPasswordSetupEmail(
                eq("test@example.com"), eq("mock-token"), eq("testuser"), eq("Test User"));
        verify(userRepository, never()).delete(any());
    }

    @Test
    void updateStatus_toInactive_sendsRejectionAndDeletes() {
        // Arrange
        UUID userId = UUID.randomUUID();
        Users user = new Users();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setFullName("Test User");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act & Assert
        UserRejectedException exception = assertThrows(UserRejectedException.class,
                () -> adminService.updateStatus(userId, UserStatus.INACTIVE));

        assertEquals("User rejected and removed from system", exception.getMessage());
        verify(emailService, times(1)).sendRejectionNotice(eq("test@example.com"), eq("Test User"));
        verify(userRepository, times(1)).delete(user);
    }

    @Test
    void updateStatus_userNotFound_throwsRuntimeException() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> adminService.updateStatus(userId, UserStatus.ACTIVE));

        assertEquals("User not found", exception.getMessage());
        verifyNoInteractions(emailService, tokenService);
    }

    @Test
    void getPending_returnsPendingUsersPage() {
        // Arrange
        Pageable pageable = mock(Pageable.class);
        Users pendingUser = new Users();
        pendingUser.setStatus(UserStatus.PENDING);
        Page<Users> page = new PageImpl<>(List.of(pendingUser), pageable, 1);

        when(userRepository.findByStatus(UserStatus.PENDING, pageable)).thenReturn(page);

        // Act
        Page<Users> result = adminService.getPending(pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals(UserStatus.PENDING, result.getContent().get(0).getStatus());
        verify(userRepository, times(1)).findByStatus(UserStatus.PENDING, pageable);
    }

    @Test
    void createAdminUser_createsAndSavesAdmin() {
        // Arrange
        AdminDTO adminDTO = new AdminDTO();
        adminDTO.setEmail("admin@example.com");
        adminDTO.setPhoneNumber("1234567890");
        adminDTO.setFullName("Admin User");

        String generatedPwd = "encoded_pwd";
        String generatedUsername = "ADMIN-123";
        Users savedUser = new Users();
        savedUser.setId(UUID.randomUUID());
        savedUser.setEmail("admin@example.com");
        savedUser.setUsername(generatedUsername);

        when(usernameGenerator.generatePassword()).thenReturn(generatedPwd);
        when(usernameGenerator.generate(UserRole.ADMIN)).thenReturn(generatedUsername);
        when(passwordEncoder.encode(generatedPwd)).thenReturn("encoded_pwd");
        when(userRepository.save(any(Users.class))).thenReturn(savedUser);
        when(tokenService.generateToken(savedUser.getId())).thenReturn("admin-token");

        // Act
        String result = adminService.createAdminUser(adminDTO);

        // Assert
        assertEquals("Admin create successfully", result);
        verify(usernameGenerator, times(1)).generatePassword();
        verify(usernameGenerator, times(1)).generate(UserRole.ADMIN);
        verify(passwordEncoder, times(1)).encode(generatedPwd);
        verify(userRepository, times(1)).save(argThat(user ->
                UserRole.ADMIN == user.getRole() &&
                        UserStatus.ACTIVE == user.getStatus() &&
                        "admin@example.com".equals(user.getEmail())));
        verify(emailService, times(1)).sendPasswordSetupEmail(
                eq("admin@example.com"), eq("admin-token"), eq(generatedUsername), eq("Admin User"));
    }
}
