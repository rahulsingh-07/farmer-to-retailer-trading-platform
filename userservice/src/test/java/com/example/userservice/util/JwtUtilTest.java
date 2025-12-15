package com.example.userservice.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() throws Exception {
        jwtUtil = new JwtUtil();

        // set apiKey via reflection since it's @Value-injected in real app
        Field apiKeyField = JwtUtil.class.getDeclaredField("apiKey");
        apiKeyField.setAccessible(true);
        // must be long enough for HS256 (at least 32 bytes)
        apiKeyField.set(jwtUtil, "this_is_a_test_api_key_32_bytes_min!!");

        // call @PostConstruct manually
        jwtUtil.init();
    }

    @Test
    void generateToken_and_extractClaims_shouldWork() {
        String username = "testuser";
        String role = "ROLE_USER";
        UUID userId = UUID.randomUUID();

        String token = jwtUtil.generateToken(username, role, userId);
        assertNotNull(token);
        assertFalse(token.isBlank());

        String extractedUsername = jwtUtil.extractUsername(token);
        assertEquals(username, extractedUsername);

        UUID extractedUserId = jwtUtil.extractUserId(token);
        assertEquals(userId, extractedUserId);

        Date expiration = jwtUtil.extractExpiration(token);
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date())); // not expired yet
    }

    @Test
    void isTokenValid_shouldReturnTrue_forValidTokenAndUsername() {
        String username = "validuser";
        String role = "ROLE_ADMIN";
        UUID userId = UUID.randomUUID();

        String token = jwtUtil.generateToken(username, role, userId);

        boolean valid = jwtUtil.isTokenValid(token, username);

        assertTrue(valid);
    }

    @Test
    void isTokenValid_shouldReturnFalse_forDifferentUsername() {
        String username = "user1";
        String role = "ROLE_USER";
        UUID userId = UUID.randomUUID();

        String token = jwtUtil.generateToken(username, role, userId);

        boolean valid = jwtUtil.isTokenValid(token, "otherUser");

        assertFalse(valid);
    }

    @Test
    void extractUserId_shouldThrowForInvalidToken() {
        String invalidToken = "invalid.token.value";

        assertThrows(IllegalArgumentException.class,
                () -> jwtUtil.extractUserId(invalidToken));
    }
}
