package com.example.userservice.util;

import com.example.userservice.enums.UserRole;
import com.example.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsernameGeneratorTest {
    @Mock private UserRepository userRepository;
    @Mock private SecureRandom random;
    @InjectMocks
    private UsernameGenerator usernameGenerator;

    @Test
    void generate_shouldCreateFarmerUsername() {
        when(random.nextInt(9000)).thenReturn(1234);

        when(userRepository.existsByUsername(anyString()))
                .thenReturn(false);

        String username = usernameGenerator.generate(UserRole.FARMER);

        assertNotNull(username);
        assertEquals("FARM-2234", username);

        verify(userRepository).existsByUsername(username);
    }

    @Test
    void generate_shouldCreateRetailerUsername() {
        when(random.nextInt(9000)).thenReturn(1234);
        when(userRepository.existsByUsername(anyString()))
                .thenReturn(false);

        String username = usernameGenerator.generate(UserRole.RETAILER);

        assertNotNull(username);
        assertEquals("RETL-2234", username);

        verify(userRepository).existsByUsername(username);
    }

    @Test
    void generate_shouldCreateAdminUsername() {
        when(random.nextInt(9000)).thenReturn(1234);
        when(userRepository.existsByUsername(anyString()))
                .thenReturn(false);

        String username = usernameGenerator.generate(UserRole.ADMIN);

        assertNotNull(username);
        assertEquals("ADM-2234", username);

        verify(userRepository).existsByUsername(username);
    }

    @Test
    void generate_shouldRetryIfUsernameAlreadyExists() {
        when(userRepository.existsByUsername(anyString()))
                .thenReturn(true)   // first try exists
                .thenReturn(false); // second try available

        String username = usernameGenerator.generate(UserRole.FARMER);

        assertNotNull(username);
        assertTrue(username.startsWith("FARM-"));

        verify(userRepository, atLeast(2)).existsByUsername(anyString());
    }

    @Test
    void generatePassword_shouldCreate10CharPassword() {
        String password = usernameGenerator.generatePassword();

        assertNotNull(password);
        assertEquals(10, password.length());
    }

    @Test
    void generatePassword_shouldContainOnlyAllowedCharacters() {
        String password = usernameGenerator.generatePassword();

        assertTrue(password.matches("[A-Za-z0-9]{10}"));
    }

    @Test
    void generatePassword_shouldGenerateDifferentPasswords() {
        when(random.nextInt(anyInt()))
                .thenReturn(
                        0,1,2,3,4,5,6,7,8,9,
                        9,8,7,6,5,4,3,2,1,0
                );
        String p1 = usernameGenerator.generatePassword();
        String p2 = usernameGenerator.generatePassword();

        assertNotEquals(p1, p2);
    }
}