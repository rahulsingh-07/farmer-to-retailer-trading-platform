package com.example.userservice.serviceimp;

import com.example.userservice.entity.PasswordResetToken;
import com.example.userservice.repository.PasswordResetTokenRepository;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.util.GenerateToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceImpTest {

    @Mock
    private PasswordResetTokenRepository tokenRepo;

    @Mock
    private UserRepository userRepository; // currently unused, but kept for constructor

    @Mock
    private GenerateToken generateToken;

    @InjectMocks
    private TokenServiceImp tokenService;

    // ===================== generateToken =====================

    @Test
    void generateToken_shouldCreateAndSaveResetToken() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String generatedToken = "reset-token-123";

        when(generateToken.generateToken()).thenReturn(generatedToken);

        // Act
        String result = tokenService.generateToken(userId);

        // Assert
        assertThat(result).isEqualTo(generatedToken);

        ArgumentCaptor<PasswordResetToken> captor =
                ArgumentCaptor.forClass(PasswordResetToken.class);

        verify(tokenRepo).save(captor.capture());

        PasswordResetToken savedToken = captor.getValue();

        assertThat(savedToken.getToken()).isEqualTo(generatedToken);
        assertThat(savedToken.getUserId()).isEqualTo(userId);
        assertThat(savedToken.isUsed()).isFalse();
        assertThat(savedToken.getExpiryDate()).isAfter(LocalDateTime.now());
        assertThat(savedToken.getExpiryDate()).isBefore(LocalDateTime.now().plusHours(25));
    }

    // ===================== validateToken =====================

    @Test
    void validateToken_shouldReturnTrue_whenTokenIsValid() {
        // Arrange
        PasswordResetToken token = new PasswordResetToken();
        token.setToken("valid");
        token.setUsed(false);
        token.setExpiryDate(LocalDateTime.now().plusMinutes(10));

        when(tokenRepo.findByToken("valid"))
                .thenReturn(Optional.of(token));

        // Act & Assert
        assertThat(tokenService.validateToken("valid")).isTrue();
    }

    @Test
    void validateToken_shouldReturnFalse_whenTokenNotFound() {
        when(tokenRepo.findByToken("missing"))
                .thenReturn(Optional.empty());

        assertThat(tokenService.validateToken("missing")).isFalse();
    }

    @Test
    void validateToken_shouldReturnFalse_whenTokenIsUsed() {
        PasswordResetToken token = new PasswordResetToken();
        token.setUsed(true);
        token.setExpiryDate(LocalDateTime.now().plusMinutes(10));

        when(tokenRepo.findByToken("used"))
                .thenReturn(Optional.of(token));

        assertThat(tokenService.validateToken("used")).isFalse();
    }

    @Test
    void validateToken_shouldReturnFalse_whenTokenIsExpired() {
        PasswordResetToken token = new PasswordResetToken();
        token.setUsed(false);
        token.setExpiryDate(LocalDateTime.now().minusMinutes(1));

        when(tokenRepo.findByToken("expired"))
                .thenReturn(Optional.of(token));

        assertThat(tokenService.validateToken("expired")).isFalse();
    }

    @Test
    void validateToken_shouldReturnFalse_whenExpiryIsExactlyNow() {
        PasswordResetToken token = new PasswordResetToken();
        token.setUsed(false);
        token.setExpiryDate(LocalDateTime.now());

        when(tokenRepo.findByToken("boundary"))
                .thenReturn(Optional.of(token));

        assertThat(tokenService.validateToken("boundary")).isFalse();
    }

    // ===================== deleteToken =====================

    @Test
    void deleteToken_shouldDeleteToken_whenExists() {
        PasswordResetToken token = new PasswordResetToken();

        when(tokenRepo.findByToken("delete"))
                .thenReturn(Optional.of(token));

        tokenService.deleteToken("delete");

        verify(tokenRepo).delete(token);
    }

    @Test
    void deleteToken_shouldThrowException_whenTokenNotFound() {
        when(tokenRepo.findByToken("missing"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> tokenService.deleteToken("missing"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("token not found");

        verify(tokenRepo, never()).delete(any());
    }

    // ===================== getToken =====================

    @Test
    void getToken_shouldReturnToken_whenExists() {
        PasswordResetToken token = new PasswordResetToken();

        when(tokenRepo.findByToken("get"))
                .thenReturn(Optional.of(token));

        PasswordResetToken result = tokenService.getToken("get");

        assertThat(result).isSameAs(token);
    }

    @Test
    void getToken_shouldThrowException_whenTokenNotFound() {
        when(tokenRepo.findByToken("missing"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> tokenService.getToken("missing"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Token not found");
    }
}
