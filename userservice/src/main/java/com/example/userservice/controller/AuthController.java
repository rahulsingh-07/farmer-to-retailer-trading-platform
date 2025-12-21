package com.example.userservice.controller;

import com.example.userservice.dto.*;
import com.example.userservice.exception.UserNotFoundException;
import com.example.userservice.serviceimp.AuthServiceImp;
import com.example.userservice.serviceimp.TokenServiceImp;
import com.example.userservice.serviceimp.UserServiceImp;
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
    private static final String MSG = "message";

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginUser loginRequest) {
        try {
            LoginResponse response = authService.login(loginRequest);
            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(MSG, e.getMessage()));

        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(MSG, e.getMessage()));
        }
    }

    @PostMapping("/register/farmer")
    public ResponseEntity<Map<String,String>> farmer(@Valid @RequestBody FarmerRegisterRequest user){
        return ResponseEntity.ok(Map.of(MSG,userService.createFarmerUser(user)));

    }

    @PostMapping("/register/retailer")
    public ResponseEntity<Map<String,String>> retailer(@Valid @RequestBody RetailerRegisterRequest user){
        return ResponseEntity.ok(Map.of(MSG,userService.createRetailerUser(user)));

    }


    @GetMapping("/validate-token")
    public ResponseEntity<Map<String,Object>> validateToken(@RequestParam String token) {
        try {
            boolean isValid = tokenService.validateToken(token);
            return ResponseEntity.ok(Map.of("valid", isValid));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(MSG, "Invalid token"));
        }
    }

    @PostMapping("/set-password")
    public ResponseEntity<Map<String,String>> setPassword(@RequestParam String token, @RequestBody Map<String, String> request) {
        try {
            String password = request.get("password");
            authService.setPassword(token, password);

            return ResponseEntity.ok(Map.of(MSG, "Password set successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(MSG, e.getMessage()));
        }
    }

    @GetMapping("/totalUsers")
    public ResponseEntity<Map<String,Integer>> getNumberUsers(){
        return ResponseEntity.ok(userService.getPublicNumbers());
    }

    @PostMapping("/forgetPassword")
    public ResponseEntity<Map<String,String>> forgetPassword(@Valid @RequestBody ForgetPasswordRequest email){
        return ResponseEntity.ok(Map.of(MSG,authService.forgetPassword(email.getEmail())));
    }


}
