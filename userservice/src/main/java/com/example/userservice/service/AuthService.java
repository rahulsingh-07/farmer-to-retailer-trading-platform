package com.example.userservice.service;

import com.example.userservice.dto.LoginResponse;
import com.example.userservice.dto.LoginUser;

public interface AuthService {
    LoginResponse login(LoginUser loginRequest);
}
