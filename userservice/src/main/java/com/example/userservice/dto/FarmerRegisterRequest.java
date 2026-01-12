package com.example.userservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class FarmerRegisterRequest {
    @NotBlank(message = "Name cannot be empty")
    private String fullName;
    @Email(message = "Invalid email")
    @NotBlank(message = "Email cannot be empty")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$",message = "invalid email pattern")
    private String email;
    @NotBlank(message = "PhoneNumber cannot be empty")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "invalid phone number")
    private String phoneNumber;
    @NotBlank(message = "address cannot be empty")
    private String address;
    @NotBlank(message = " PM-KISAN-ID cannot be empty")
    @Size(min = 12, max = 12, message = "PM-KISAN ID must be exactly 12 digits")
    @Pattern(regexp = "\\d{12}", message = "PM-KISAN ID must contain only numbers")
    private String pmKisanId;
}
