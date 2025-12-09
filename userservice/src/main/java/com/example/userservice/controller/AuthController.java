package com.example.userservice.controller;

import com.example.userservice.dto.FarmerRegisterRequest;
import com.example.userservice.dto.LoginResponse;
import com.example.userservice.dto.LoginUser;
import com.example.userservice.dto.RetailerRegisterRequest;
import com.example.userservice.exception.UserNotFoundException;
import com.example.userservice.serviceImp.AuthServiceImp;
import com.example.userservice.serviceImp.TokenServiceImp;
import com.example.userservice.serviceImp.UserServiceImp;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final UserServiceImp userService;
    private final AuthServiceImp authService;
    private final TokenServiceImp tokenService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginUser loginRequest) {
        try {
            LoginResponse response = authService.login(loginRequest);
            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", e.getMessage()));

        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/register/farmer")
    public ResponseEntity<?> farmer(@Valid @RequestBody FarmerRegisterRequest user){
        return ResponseEntity.ok(Map.of("message",userService.createFarmerUser(user)));

    }

    @PostMapping("/register/retailer")
    public ResponseEntity<?> retailer(@Valid @RequestBody RetailerRegisterRequest user){
        return ResponseEntity.ok(Map.of("message",userService.createRetailerUser(user)));

    }


    @GetMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestParam String token) {
        try {
            boolean isValid = tokenService.validateToken(token);
            return ResponseEntity.ok(Map.of("valid", isValid));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid token"));
        }
    }

    @PostMapping("/set-password")
    public ResponseEntity<?> setPassword(@RequestParam String token, @RequestBody Map<String, String> request) {
        try {
            String password = request.get("password");
            authService.setPassword(token, password);

            return ResponseEntity.ok(Map.of("message", "Password set successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/totalUsers")
    public ResponseEntity<Map<String,Integer>> getNumberUsers(){
        return ResponseEntity.ok(userService.getPublicNumbers());
    }


}
