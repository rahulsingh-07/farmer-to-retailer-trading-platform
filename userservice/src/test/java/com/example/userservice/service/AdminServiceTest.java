package com.example.userservice.serviceImp;

import com.example.userservice.entity.Users;
import com.example.userservice.enums.UserStatus;
import com.example.userservice.exception.UserRejectedException;
import com.example.userservice.notification.EmailService;
import com.example.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenServiceImp tokenService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AdminServiceImp adminService;

    @Test
    void updateStatus_shouldActivateUserAndSendPasswordSetupEmail() {
        // Arrange
        UUID id = UUID.randomUUID();
        Users user = new Users();
        user.setId(id);
        user.setEmail("test@example.com");
        user.setUsername("testuser");
        user.setFullName("Test User");
        user.setStatus(UserStatus.PENDING);
        user.setUpdatePasswordRequired(false);
        user.setPasswordResetAttempts(5);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(tokenService.generateToken(id)).thenReturn("dummy-token");

        // Act
        Users result = adminService.updateStatus(id, UserStatus.ACTIVE);

        // Assert
        assertEquals(UserStatus.ACTIVE, result.getStatus());
        assertTrue(result.isUpdatePasswordRequired());
        assertEquals(0, result.getPasswordResetAttempts());
        assertNotNull(result.getUpdateAt());

        verify(tokenService, times(1)).generateToken(id);
        verify(emailService, times(1))
                .sendPasswordSetupEmail("test@example.com", "dummy-token", "testuser", "Test User");
        verify(userRepository, never()).delete(any());
    }

    @Test
    void updateStatus_shouldThrowWhenUserNotFound() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> adminService.updateStatus(id, UserStatus.ACTIVE));

        assertEquals("User not found", ex.getMessage());
        verify(tokenService, never()).generateToken(any());
        verify(emailService, never()).sendPasswordSetupEmail(any(), any(), any(), any());
        verify(emailService, never()).sendRejectionNotice(any(), any());
        verify(userRepository, never()).delete(any());
    }

    @Test
    void updateStatus_shouldRejectUserAndDeleteAndThrowException() {
        // Arrange
        UUID id = UUID.randomUUID();
        Users user = new Users();
        user.setId(id);
        user.setEmail("reject@example.com");
        user.setFullName("Reject User");
        user.setStatus(UserStatus.PENDING);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        // Act & Assert
        UserRejectedException ex = assertThrows(UserRejectedException.class,
                () -> adminService.updateStatus(id, UserStatus.INACTIVE));

        assertEquals("User rejected and removed from system", ex.getMessage());
        verify(emailService, times(1))
                .sendRejectionNotice("reject@example.com", "Reject User");
        verify(userRepository, times(1)).delete(user);
        verify(tokenService, never()).generateToken(any());
        verify(emailService, never()).sendPasswordSetupEmail(any(), any(), any(), any());
    }

    @Test
    void getPending_shouldReturnPendingUsersPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Users pendingUser = new Users();
        pendingUser.setId(UUID.randomUUID());
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
}
