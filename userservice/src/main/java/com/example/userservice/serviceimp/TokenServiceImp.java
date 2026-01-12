package com.example.userservice.serviceimp;

import com.example.userservice.entity.PasswordResetToken;
import com.example.userservice.repository.PasswordResetTokenRepository;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.TokenService;
import com.example.userservice.util.GenerateToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenServiceImp implements TokenService {
    private final PasswordResetTokenRepository tokenRepo;
    private final GenerateToken generateToken;

    @Override
    public String generateToken(UUID userId) {
        PasswordResetToken resetToken = new PasswordResetToken();
        String token=generateToken.generateToken();
        resetToken.setToken(token);
        resetToken.setUserId(userId);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        resetToken.setUsed(false);

        tokenRepo.save(resetToken);
        return token;
    }

    @Override
    public boolean validateToken(String token) {
        log.info("validate token call {}",token);
        return tokenRepo.findByToken(token)
                .filter(t -> !t.isUsed() && t.getExpiryDate().isAfter(LocalDateTime.now()))
                .isPresent();
    }

    @Override
    public void deleteToken(String token){
        PasswordResetToken resetToken = tokenRepo.findByToken(token)
                .orElseThrow(() -> new RuntimeException("token not found"));
        tokenRepo.delete(resetToken);
    }

    @Override
    public PasswordResetToken getToken(String token) {
        return tokenRepo.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token not found"));
    }

}
