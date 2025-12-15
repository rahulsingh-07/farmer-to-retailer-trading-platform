package com.example.userservice.serviceImp;

import com.example.userservice.entity.PasswordResetToken;
import com.example.userservice.repository.PasswordResetTokenRepository;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.util.GenerateToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private PasswordResetTokenRepository tokenRepo;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GenerateToken generateToken;

    @InjectMocks
    private TokenServiceImp tokenService;

    @Test
    void generateToken_shouldCreateAndSaveTokenAndReturnString() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String generated = "generated-token";
        when(generateToken.generateToken()).thenReturn(generated);

        // Act
        String result = tokenService.generateToken(userId);

        // Assert
        assertEquals(generated, result);

        ArgumentCaptor<PasswordResetToken> captor =
                ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepo, times(1)).save(captor.capture());

        PasswordResetToken saved = captor.getValue();
        assertEquals(generated, saved.getToken());
        assertEquals(userId, saved.getUserId());
        assertFalse(saved.isUsed());
        assertTrue(saved.getExpiryDate().isAfter(LocalDateTime.now()));
    }

    @Test
    void validateToken_shouldReturnTrue_whenTokenExistsNotUsedAndNotExpired() {
        // Arrange
        String token = "valid-token";
        PasswordResetToken entity = new PasswordResetToken();
        entity.setToken(token);
        entity.setUsed(false);
        entity.setExpiryDate(LocalDateTime.now().plusHours(1)); // not expired

        when(tokenRepo.findByToken(token)).thenReturn(Optional.of(entity));

        // Act
        boolean result = tokenService.validateToken(token);

        // Assert
        assertTrue(result);
    }

    @Test
    void validateToken_shouldReturnFalse_whenTokenMissing() {
        // Arrange
        String token = "missing-token";
        when(tokenRepo.findByToken(token)).thenReturn(Optional.empty());

        // Act
        boolean result = tokenService.validateToken(token);

        // Assert
        assertFalse(result);
    }

    @Test
    void validateToken_shouldReturnFalse_whenTokenUsed() {
        // Arrange
        String token = "used-token";
        PasswordResetToken entity = new PasswordResetToken();
        entity.setToken(token);
        entity.setUsed(true); // used
        entity.setExpiryDate(LocalDateTime.now().plusHours(1));

        when(tokenRepo.findByToken(token)).thenReturn(Optional.of(entity));

        // Act
        boolean result = tokenService.validateToken(token);

        // Assert
        assertFalse(result);
    }

    @Test
    void validateToken_shouldReturnFalse_whenTokenExpired() {
        // Arrange
        String token = "expired-token";
        PasswordResetToken entity = new PasswordResetToken();
        entity.setToken(token);
        entity.setUsed(false);
        entity.setExpiryDate(LocalDateTime.now().minusHours(1)); // expired

        when(tokenRepo.findByToken(token)).thenReturn(Optional.of(entity));

        // Act
        boolean result = tokenService.validateToken(token);

        // Assert
        assertFalse(result);
    }

    @Test
    void deleteToken_shouldDeleteWhenTokenExists() {
        // Arrange
        String token = "to-delete";
        PasswordResetToken entity = new PasswordResetToken();
        entity.setToken(token);

        when(tokenRepo.findByToken(token)).thenReturn(Optional.of(entity));

        // Act
        tokenService.deleteToken(token);

        // Assert
        verify(tokenRepo, times(1)).delete(entity);
    }

    @Test
    void deleteToken_shouldThrowWhenTokenNotFound() {
        // Arrange
        String token = "not-found";
        when(tokenRepo.findByToken(token)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex =
                assertThrows(RuntimeException.class, () -> tokenService.deleteToken(token));
        assertEquals("token not found", ex.getMessage());
        verify(tokenRepo, never()).delete(any());
    }

    @Test
    void getToken_shouldReturnTokenWhenExists() {
        // Arrange
        String token = "existing-token";
        PasswordResetToken entity = new PasswordResetToken();
        entity.setToken(token);

        when(tokenRepo.findByToken(token)).thenReturn(Optional.of(entity));

        // Act
        PasswordResetToken result = tokenService.getToken(token);

        // Assert
        assertNotNull(result);
        assertEquals(token, result.getToken());
    }

    @Test
    void getToken_shouldThrowWhenTokenNotFound() {
        // Arrange
        String token = "missing-token";
        when(tokenRepo.findByToken(token)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex =
                assertThrows(RuntimeException.class, () -> tokenService.getToken(token));
        assertEquals("Token not found", ex.getMessage());
    }
}
