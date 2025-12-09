package com.example.userservice.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UserUpdateDTO {
    @Email(message = "Email should be valid")
    private String email;

    private String phone;

    private String password;
}
