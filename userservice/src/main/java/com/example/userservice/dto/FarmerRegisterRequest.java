package com.example.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FarmerRegisterRequest {
    @NotBlank(message = "Name cannot be empty")
    private String fullName;
    @Email(message = "Invalid email")
    @NotBlank(message = "Email cannot be empty")
    private String email;
    @NotBlank(message = "Phone cannot be empty")
    private String phoneNumber;
    @NotBlank(message = "address cannot be empty")
    private String address;
    @NotBlank(message = "document cannot be empty")
    private String documentUrl;
}
