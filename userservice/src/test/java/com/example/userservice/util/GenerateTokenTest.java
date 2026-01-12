package com.example.userservice.util;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GenerateTokenTest {

    private final GenerateToken generateToken = new GenerateToken();

    @Test
    void generateToken_shouldReturnNonEmptyUrlSafeToken() {
        String token = generateToken.generateToken();

        assertNotNull(token);
        assertFalse(token.isBlank());

        // URL-safe Base64: A–Z a–z 0–9 - _
        assertTrue(token.matches("^[A-Za-z0-9_-]+$"));
    }

    @Test
    void generateToken_shouldHaveConsistentLength() {
        String token1 = generateToken.generateToken();
        String token2 = generateToken.generateToken();

        assertEquals(token1.length(), token2.length());

        // 32 bytes → Base64 URL-safe without padding → 43 chars
        assertEquals(43, token1.length());
    }

    @Test
    void generateToken_shouldGenerateUniqueTokens() {
        Set<String> tokens = new HashSet<>();

        for (int i = 0; i < 100; i++) {
            tokens.add(generateToken.generateToken());
        }

        assertEquals(100, tokens.size());
    }
}
