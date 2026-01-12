package com.example.userservice.service;

import com.example.userservice.dto.FarmerRegisterRequest;
import com.example.userservice.dto.RetailerRegisterRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface UserService {
    void createFarmerUser(FarmerRegisterRequest req);
    void createRetailerUser(RetailerRegisterRequest req, MultipartFile file);
}
