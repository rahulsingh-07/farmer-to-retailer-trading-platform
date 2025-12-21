package com.example.userservice.filter;

import com.example.userservice.common.CustomUserDetailsService;
import com.example.userservice.common.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // Extract the Authorization header from the request
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        // Check if the Authorization header is present and starts with "Bearer "
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // Remove "Bearer " prefix to get the actual token
            token = authHeader.substring(7);
            // Extract username from the JWT token
            username = jwtUtil.extractUsername(token);
        }

        // If username is obtained and there's no authentication already set in the context
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Load user details using custom UserDetailsService
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
            // Validate the token with the user details
            if (jwtUtil.isTokenValid(token, userDetails.getUsername())) {
                // Extract userId for future use
                UUID userId = jwtUtil.extractUserId(token);

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                // Set additional details (e.g., IP, session ID) from the current request
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // ✅ Store userId in authentication details for controller access
                Map<String, Object> details = new HashMap<>();
                details.put("userId", userId);
                details.put("userAgent", request.getHeader("User-Agent"));
                authToken.setDetails(details);
                // Set the authentication token in the SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authToken);

                log.debug("Authenticated user: {} (ID: {})", username, userId);
            }

        }


        // Continue with the next filter in the chain
        filterChain.doFilter(request, response);
    }
}
