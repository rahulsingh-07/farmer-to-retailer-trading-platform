package com.example.userservice.serviceimp;

import com.example.userservice.entity.Users;
import com.example.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordReminderServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailServiceImp emailService;

    @InjectMocks
    private PasswordReminderService passwordReminderService;

    @Test
    void runReminderTask_shouldSendReminderAndIncrementAttempts_whenAttemptsLessThanThree() {
        // Arrange
        Users user = new Users();
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        user.setPasswordResetAttempts(1);

        when(userRepository.findByUpdatePasswordRequired(true))
                .thenReturn(List.of(user));

        // Act
        passwordReminderService.runReminderTask();

        // Assert
        verify(emailService)
                .sendReminderEmail("test@example.com", "Test User");

        assertThat(user.getPasswordResetAttempts()).isEqualTo(2);

        verify(userRepository).save(user);
        verify(userRepository, never()).delete(any());
        verify(emailService, never()).sendDeletionEmail(any(), any());
    }

    @Test
    void runReminderTask_shouldDeleteUserAndSendDeletionEmail_whenAttemptsEqualThree() {
        // Arrange
        Users user = new Users();
        user.setEmail("delete@example.com");
        user.setFullName("Delete User");
        user.setPasswordResetAttempts(3);

        when(userRepository.findByUpdatePasswordRequired(true))
                .thenReturn(List.of(user));

        // Act
        passwordReminderService.runReminderTask();

        // Assert
        verify(emailService)
                .sendDeletionEmail("delete@example.com", "Delete User");

        verify(userRepository).delete(user);

        verify(emailService, never()).sendReminderEmail(any(), any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void runReminderTask_shouldDeleteUserAndSendDeletionEmail_whenAttemptsGreaterThanThree() {
        // Arrange
        Users user = new Users();
        user.setEmail("delete2@example.com");
        user.setFullName("Delete User 2");
        user.setPasswordResetAttempts(5);

        when(userRepository.findByUpdatePasswordRequired(true))
                .thenReturn(List.of(user));

        // Act
        passwordReminderService.runReminderTask();

        // Assert
        verify(emailService)
                .sendDeletionEmail("delete2@example.com", "Delete User 2");

        verify(userRepository).delete(user);

        verify(emailService, never()).sendReminderEmail(any(), any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void runReminderTask_shouldHandleMultipleUsersCorrectly() {
        // Arrange
        Users user1 = new Users();
        user1.setEmail("u1@example.com");
        user1.setFullName("User One");
        user1.setPasswordResetAttempts(0);

        Users user2 = new Users();
        user2.setEmail("u2@example.com");
        user2.setFullName("User Two");
        user2.setPasswordResetAttempts(3);

        when(userRepository.findByUpdatePasswordRequired(true))
                .thenReturn(List.of(user1, user2));

        // Act
        passwordReminderService.runReminderTask();

        // Assert user1 (reminder)
        verify(emailService)
                .sendReminderEmail("u1@example.com", "User One");
        verify(userRepository).save(user1);
        assertThat(user1.getPasswordResetAttempts()).isEqualTo(1);

        // Assert user2 (deletion)
        verify(emailService)
                .sendDeletionEmail("u2@example.com", "User Two");
        verify(userRepository).delete(user2);
    }

    @Test
    void runReminderTask_shouldDoNothing_whenNoUsersFound() {
        // Arrange
        when(userRepository.findByUpdatePasswordRequired(true))
                .thenReturn(List.of());

        // Act
        passwordReminderService.runReminderTask();

        // Assert
        verifyNoInteractions(emailService);
        verify(userRepository, never()).save(any());
        verify(userRepository, never()).delete(any());
    }
}
