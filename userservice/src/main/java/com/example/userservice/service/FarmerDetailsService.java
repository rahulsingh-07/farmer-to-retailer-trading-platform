package com.example.userservice.service;

import com.example.userservice.dto.FarmerRegisterRequest;
import com.example.userservice.entity.FarmerDetails;
import com.example.userservice.entity.Users;

public interface FarmerDetailsService {
    FarmerDetails saveDetails(Users user, FarmerRegisterRequest req);

}
