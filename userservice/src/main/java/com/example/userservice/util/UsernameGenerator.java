package com.example.userservice.util;

import com.example.userservice.enums.UserRole;
import com.example.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
@RequiredArgsConstructor
public class UsernameGenerator {
    private final UserRepository userRepository;
    private final SecureRandom random;


    public String generate(UserRole role) {
        String prefix = switch (role) {
            case FARMER -> "FARM";
            case RETAILER -> "RETL";
            case ADMIN -> "ADM";
        };

        String username;
        do {
            int num = random.nextInt(9000) + 1000;
            username = prefix + "-" + num;
        } while (userRepository.existsByUsername(username));

        return username;
    }

    // ---- PASSWORD GENERATOR ----
    public String generatePassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder(10);

        for (int i = 0; i < 10; i++) {
            int index = random.nextInt(chars.length());
            password.append(chars.charAt(index));
        }

        return password.toString();
    }
}
