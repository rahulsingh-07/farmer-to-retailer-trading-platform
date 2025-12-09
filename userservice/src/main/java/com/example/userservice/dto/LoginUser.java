package com.example.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginUser {
    @NotBlank(message = "enter username")
    private String username;
    @NotBlank(message = "enter the password")
    private String password;
}
