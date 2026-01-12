package com.example.userservice.service;

import com.example.userservice.entity.PasswordResetToken;

import java.util.UUID;

public interface TokenService {
    String generateToken(UUID userId);
    boolean validateToken(String token);
    void deleteToken(String token);
    PasswordResetToken getToken(String token);


}
