package com.example.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RetailerRegisterRequest {
    @NotBlank(message = "Name cannot be empty")
    private String fullName;
    @Email(message = "Invalid email")
    @NotBlank(message = "Email cannot be empty")
    private String email;
    @NotBlank(message = "Phone cannot be empty")
    private String phoneNumber;
    @NotBlank(message = "shopName cannot be empty")
    private String shopName;
    @NotBlank(message = "businessAddress cannot be empty")
    private String businessAddress;
    @NotBlank(message = "gstNumber cannot be empty")
    private String gstNumber;
    @NotBlank(message = "tradeLicenseUrl cannot be empty")
    private String tradeLicenseUrl;
}
