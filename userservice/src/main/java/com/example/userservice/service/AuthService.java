package com.example.userservice.service;

import com.example.userservice.dto.LoginUser;
import com.example.userservice.records.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginUser loginRequest);
    void setPassword(String token, String rawPassword);
    void forgetPassword(String email);
}
