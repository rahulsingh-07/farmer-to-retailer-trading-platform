package com.example.userservice.service;

import com.example.userservice.dto.RetailerRegisterRequest;
import com.example.userservice.entity.RetailerDetails;
import com.example.userservice.entity.Users;

public interface RetailerDetailsService {
    RetailerDetails saveDetails(Users user, RetailerRegisterRequest req);
}
