package com.example.userservice.serviceImp;

import com.example.userservice.config.UsernameGenerator;
import com.example.userservice.dto.FarmerRegisterRequest;
import com.example.userservice.dto.RetailerRegisterRequest;
import com.example.userservice.dto.UserUpdateDTO;
import com.example.userservice.entity.Users;
import com.example.userservice.enums.UserRole;
import com.example.userservice.enums.UserStatus;
import com.example.userservice.mapper.UserMapper;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImp implements UserService {
    private final UserRepository userRepository;
    private final UsernameGenerator usernameGenerator;
    private final PasswordEncoder passwordEncoder;

    @Override
    public String createFarmerUser(FarmerRegisterRequest req) {
        String generatedUsername = usernameGenerator.generate(UserRole.FARMER);
        String generatedPwd=usernameGenerator.generatePassword();
        Users user = UserMapper.toFarmerUser(req, passwordEncoder.encode(generatedPwd), generatedUsername);
        userRepository.save(user);
        return "Registration successful";
    }
    @Override
    public String createRetailerUser(RetailerRegisterRequest req) {
        String generatedUsername = usernameGenerator.generate(UserRole.RETAILER);
        String generatedPwd=usernameGenerator.generatePassword();
        Users user = UserMapper.toRetailerUser(req, passwordEncoder.encode(generatedPwd), generatedUsername);
        userRepository.save(user);
        return "Registration successful";
    }

    @Override
    public Users createAdminUser(Users req) {
        String generatedPwd=usernameGenerator.generatePassword();
        System.out.println("password: "+generatedPwd);
        String generatedUsername = usernameGenerator.generate(UserRole.ADMIN);
        System.out.println("Username: "+generatedUsername);
        req.setUsername(generatedUsername);
        req.setPassword(passwordEncoder.encode(generatedPwd));
        req.setRole(UserRole.ADMIN);
        req.setStatus(UserStatus.ACTIVE);
        req.setCreatedAt(LocalDateTime.now());
        userRepository.save(req);
        return req;
    }

    public Users updateUserPartially(UUID id, UserUpdateDTO patchDTO) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (patchDTO.getPassword() != null) {
            user.setPassword(patchDTO.getPassword());
        }
        if (patchDTO.getEmail() != null) {
            user.setEmail(patchDTO.getEmail());
        }
        if (patchDTO.getPhone() != null) {
            user.setPhoneNumber(patchDTO.getPhone());
        }
        user.setUpdateAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    public Map<String, Integer> getAdminNumbers() {

        Map<String, Integer> map = new HashMap<>();
        map.put("totalUsers", userRepository.totalUsers());
        map.put("totalPending", userRepository.totalPending());
        map.put("totalAdmin", userRepository.totalAdmin());

        return map;
    }
    @GetMapping("/public-numbers")
    public Map<String, Integer> getPublicNumbers() {
        Map<String, Integer> map = new HashMap<>();
        map.put("totalFarmer", userRepository.totalFarmer());
        map.put("totalRetailer", userRepository.totalRetailer());
        return map;
    }


}
