package com.example.userservice.records;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AdminCreateRequest(
        @NotBlank(message = "name required")
        String fullName,
        @NotBlank(message = "email required")
        @Email(message = "valid email required")
        @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$",message = "invalid email pattern")
        String email,

        @NotBlank(message = "phone number required")
        @Pattern(regexp = "^[6-9]\\d{9}$", message = "invalid phone number")
        String phoneNumber
) {}
