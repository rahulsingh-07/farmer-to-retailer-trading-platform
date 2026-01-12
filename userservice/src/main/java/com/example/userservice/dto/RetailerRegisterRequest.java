package com.example.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RetailerRegisterRequest {
    @NotBlank(message = "Name cannot be empty")
    private String fullName;
    @Email(message = "Invalid email")
    @NotBlank(message = "Email cannot be empty")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$",message = "invalid email pattern")
    private String email;
    @NotBlank(message = "Phone cannot be empty")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "invalid phone number")
    private String phoneNumber;
    @NotBlank(message = "businessAddress cannot be empty")
    private String businessAddress;
}
