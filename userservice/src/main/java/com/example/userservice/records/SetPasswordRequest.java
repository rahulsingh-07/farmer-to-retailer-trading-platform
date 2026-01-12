package com.example.userservice.records;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SetPasswordRequest(
        @NotBlank(message = "password required")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
                message = "password must contain at least 8 characters, one uppercase, one lowercase, one number, and one special character"
        )
        String password
) {
}
