package com.example.userservice.service;
import com.example.userservice.entity.Users;
import com.example.userservice.notification.EmailService;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.serviceimp.PasswordReminderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PasswordReminderServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private PasswordReminderService passwordReminderService;

    @Test
    void runReminderTask_shouldSendReminderAndIncrementAttempts_whenAttemptsLessThanThree() {
        // Arrange
        Users user = new Users();
        user.setEmail("user1@example.com");
        user.setFullName("User One");
        user.setPasswordResetAttempts(1); // < 3

        when(userRepository.findByUpdatePasswordRequired(true))
                .thenReturn(List.of(user));

        // Act
        passwordReminderService.runReminderTask();

        // Assert
        verify(emailService, times(1))
                .sendReminderEmail("user1@example.com", "User One");
        verify(emailService, never())
                .sendDeletionEmail(anyString(), anyString());

        verify(userRepository, times(1)).save(user);

    }

    @Test
    void runReminderTask_shouldDeleteUser_whenAttemptsGreaterOrEqualThree() {
        // Arrange
        Users user = new Users();
        user.setEmail("user2@example.com");
        user.setFullName("User Two");
        user.setPasswordResetAttempts(3); // >= 3

        when(userRepository.findByUpdatePasswordRequired(true))
                .thenReturn(List.of(user));

        // Act
        passwordReminderService.runReminderTask();

        // Assert
        verify(emailService, times(1))
                .sendDeletionEmail("user2@example.com", "User Two");
        verify(userRepository, times(1)).delete(user);

        verify(emailService, never())
                .sendReminderEmail(anyString(), anyString());
        verify(userRepository, never()).save(user);
    }

    @Test
    void runReminderTask_shouldHandleMixedUsers() {
        // Arrange
        Users userToRemind = new Users();
        userToRemind.setEmail("remind@example.com");
        userToRemind.setFullName("Remind User");
        userToRemind.setPasswordResetAttempts(2); // < 3

        Users userToDelete = new Users();
        userToDelete.setEmail("delete@example.com");
        userToDelete.setFullName("Delete User");
        userToDelete.setPasswordResetAttempts(4); // >= 3

        when(userRepository.findByUpdatePasswordRequired(true))
                .thenReturn(List.of(userToRemind, userToDelete));

        // Act
        passwordReminderService.runReminderTask();

        // Assert: reminder path
        verify(emailService, times(1))
                .sendReminderEmail("remind@example.com", "Remind User");
        verify(userRepository, times(1)).save(userToRemind);

        // Assert: deletion path
        verify(emailService, times(1))
                .sendDeletionEmail("delete@example.com", "Delete User");
        verify(userRepository, times(1)).delete(userToDelete);
    }
}
