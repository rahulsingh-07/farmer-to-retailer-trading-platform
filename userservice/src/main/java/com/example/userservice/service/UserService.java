package com.example.userservice.service;

import com.example.userservice.dto.FarmerRegisterRequest;
import com.example.userservice.dto.RetailerRegisterRequest;
import com.example.userservice.entity.Users;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    String createFarmerUser(FarmerRegisterRequest req);

    String createRetailerUser(RetailerRegisterRequest req);
}
