package com.example.userservice.serviceImp;

import com.example.userservice.entity.PasswordResetToken;
import com.example.userservice.repository.PasswordResetTokenRepository;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.util.GenerateToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenServiceImp {
    private final PasswordResetTokenRepository tokenRepo;
    private final UserRepository userRepository;
    private final GenerateToken generateToken;
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

    public boolean validateToken(String token) {
        return tokenRepo.findByToken(token)
                .filter(t -> !t.isUsed() && t.getExpiryDate().isAfter(LocalDateTime.now()))
                .isPresent();
    }

    public void deleteToken(String token){
        PasswordResetToken resetToken = tokenRepo.findByToken(token)
                .orElseThrow(() -> new RuntimeException("token not found"));
        tokenRepo.delete(resetToken);
    }



    public PasswordResetToken getToken(String token) {
        return tokenRepo.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token not found"));
    }

}
