package com.example.userservice.controller;

import com.example.userservice.dto.*;
import com.example.userservice.records.ApiResponse;
import com.example.userservice.records.LoginResponse;
import com.example.userservice.records.PublicStats;
import com.example.userservice.records.SetPasswordRequest;
import com.example.userservice.serviceimp.AuthServiceImp;
import com.example.userservice.serviceimp.TokenServiceImp;
import com.example.userservice.serviceimp.UserServiceImp;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final UserServiceImp userServiceImp;
    private final AuthServiceImp authServiceImp;
    private final TokenServiceImp tokenServiceImp;
    private static final String MSG = "message";

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @RequestBody LoginUser loginRequest) {

        LoginResponse response = authServiceImp.login(loginRequest);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Login successful",
                        response)
        );
    }

    // Farmer Registration
    @PostMapping("/register/farmer")
    public ResponseEntity<ApiResponse<Void>> farmer(@Valid @RequestBody FarmerRegisterRequest user){
        userServiceImp.createFarmerUser(user);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        "Farmer registered successfully",
                        null
                ));
    }

    // Retailer registration
    @PostMapping(
            value = "/register/retailer",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<Void>> retailer(@Valid @ModelAttribute RetailerRegisterRequest user,
                                                      @RequestPart("tradeLicense") MultipartFile tradeLicense){
        userServiceImp.createRetailerUser(user,tradeLicense);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        "Retailer registered successfully",
                        null
                ));
    }

    // To Check is token valid or not
    @GetMapping("/validate-token")
    public ResponseEntity<ApiResponse<Boolean>> validateToken(
            @RequestParam String token) {
        boolean isValid = tokenServiceImp.validateToken(token);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Token validation result",
                        isValid)
        );
    }


    // set password
    @PostMapping("/set-password")
    public ResponseEntity<ApiResponse<Void>> setPassword(@RequestParam String token,
                                                         @Valid @RequestBody SetPasswordRequest request) {
            authServiceImp.setPassword(token, request.password());
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(
                            "Password set successfully",
                            null
                    ));
    }

    // landing page stats
    @GetMapping("/landingPageStats")
    public ResponseEntity<ApiResponse<PublicStats>> getLandingPageStatus(){
        return ResponseEntity
                .ok(new ApiResponse<>(
                        "Public stats",
                        userServiceImp.getPublicStats()
                ));
    }

    // forget password
    @PostMapping("/forgetPassword")
    public ResponseEntity<ApiResponse<Void>> forgetPassword(@Valid @RequestBody ForgetPasswordRequest email){
        return ResponseEntity
                .ok(new ApiResponse<>(
                        "Link send to your email update your password",
                        null
                ));
    }


}
