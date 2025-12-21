package com.example.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminDTO {
    @NotBlank(message = "name required")
    private String fullName;
    @NotBlank(message = "email required")
    @Email(message = "valid email required")
    private String email;
    @NotBlank(message = "phone number required")
    private String phoneNumber;
}
