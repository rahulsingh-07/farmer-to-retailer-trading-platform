package com.example.userservice.mapper;

import com.example.userservice.dto.FarmerRegisterRequest;
import com.example.userservice.dto.RetailerRegisterRequest;
import com.example.userservice.entity.FarmerDetails;
import com.example.userservice.entity.RetailerDetails;
import com.example.userservice.entity.Users;
import com.example.userservice.enums.UserRole;
import com.example.userservice.enums.UserStatus;

import java.time.LocalDateTime;
import java.util.Locale;

public class UserMapper {

    // FARMER REGISTRATION
    public static Users toFarmerUser(FarmerRegisterRequest req, String encodedPassword, String username) {
        Users user = new Users();
        user.setFullName(req.getFullName());
        user.setEmail(req.getEmail().toLowerCase(Locale.ROOT));
        user.setPhoneNumber(req.getPhoneNumber());
        user.setUsername(username);
        user.setPassword(encodedPassword);
        user.setRole(UserRole.FARMER);
        user.setStatus(UserStatus.PENDING);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdateAt(LocalDateTime.now());

        // Child table
        FarmerDetails details = new FarmerDetails();
        details.setUser(user);
        details.setAddress(req.getAddress());
        details.setDocumentUrl(req.getDocumentUrl());

        user.setFarmerDetails(details);
        return user;
    }

    // RETAILER REGISTRATION
    public static Users toRetailerUser(RetailerRegisterRequest req, String encodedPassword, String username) {
        Users user = new Users();
        user.setFullName(req.getFullName());
        user.setEmail(req.getEmail().toLowerCase(Locale.ROOT));
        user.setPhoneNumber(req.getPhoneNumber());
        user.setUsername(username);
        user.setPassword(encodedPassword);
        user.setRole(UserRole.RETAILER);
        user.setStatus(UserStatus.PENDING);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdateAt(LocalDateTime.now());

        RetailerDetails details = new RetailerDetails();
        details.setUser(user);
        details.setShopName(req.getShopName());
        details.setBusinessAddress(req.getBusinessAddress());
        details.setGstNumber(req.getGstNumber());
        details.setTradeLicenseUrl(req.getTradeLicenseUrl());

        user.setRetailerDetails(details);
        return user;
    }
}
