package com.example.userservice.serviceImp;

import com.example.userservice.dto.LoginResponse;
import com.example.userservice.dto.LoginUser;
import com.example.userservice.entity.PasswordResetToken;
import com.example.userservice.entity.Users;
import com.example.userservice.exception.UserNotFoundException;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.AuthService;
import com.example.userservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImp implements AuthService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final TokenServiceImp tokenService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(LoginUser loginRequest) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        if(!auth.isAuthenticated()) {
            throw new BadCredentialsException("Authentication failed");
        }

        Users user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());

        return new LoginResponse(token, "Login successful");
    }

    @Transactional
    public void setPassword(String token, String rawPassword) {
        PasswordResetToken resetToken = tokenService.getToken(token);

        // Validate token
        if (!tokenService.validateToken(token)) {
            throw new RuntimeException("Invalid or expired token");
        }

        // Set password for user
        UUID userId = resetToken.getUserId();
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(rawPassword));
        userRepository.save(user);
        tokenService.deleteToken(token);
    }

}
