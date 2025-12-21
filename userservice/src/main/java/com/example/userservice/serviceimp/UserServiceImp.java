package com.example.userservice.serviceimp;

import com.example.userservice.config.UsernameGenerator;
import com.example.userservice.dto.FarmerRegisterRequest;
import com.example.userservice.dto.RetailerRegisterRequest;
import com.example.userservice.dto.UserUpdateDTO;
import com.example.userservice.entity.Users;
import com.example.userservice.enums.UserRole;
import com.example.userservice.mapper.UserMapper;
import com.example.userservice.repository.NotificationRepository;
import com.example.userservice.repository.OrderRepository;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
    private final OrderRepository orderRepository;
    private final NotificationRepository notificationRepository;

    @Override
    public String createFarmerUser(FarmerRegisterRequest req) {
        try {
            String generatedUsername = usernameGenerator.generate(UserRole.FARMER);
            String generatedPwd = usernameGenerator.generatePassword();
            Users user = UserMapper.toFarmerUser(req, passwordEncoder.encode(generatedPwd), generatedUsername);
            userRepository.save(user);
            return "Registration successful";
        } catch (IllegalArgumentException  | NullPointerException e) {
            return "Registration failed: " + e.getMessage();  // "Email cannot be null"
        } catch (Exception e) {
            return "Registration failed: Unexpected error occurred";
        }
    }
    @Override
    public String createRetailerUser(RetailerRegisterRequest req) {
        try {
            String generatedUsername = usernameGenerator.generate(UserRole.RETAILER);
            String generatedPwd = usernameGenerator.generatePassword();
            Users user = UserMapper.toRetailerUser(req, passwordEncoder.encode(generatedPwd), generatedUsername);
            userRepository.save(user);
            return "Registration successful";
        }catch (IllegalArgumentException | NullPointerException e) {
            return "Registration failed: " + e.getMessage();
        }catch (Exception e) {
            return "Registration failed: Unexpected error occurred";
        }
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
    public Map<String, Integer> getPublicNumbers() {
        Map<String, Integer> map = new HashMap<>();
        map.put("totalFarmer", userRepository.totalFarmer());
        map.put("totalRetailer", userRepository.totalRetailer());
        return map;
    }


    public Map<String, Long> dashboardValues(UUID userId) {
        Map<String,Long> map=new HashMap<>();
        map.put("needConfirmation",orderRepository.countNeedConfirmedOrderByRetailerId(userId));
        map.put("confirmed",orderRepository.countConfirmedOrderByRetailerId(userId));
        map.put("shipped",orderRepository.countShippedOrderByRetailerId(userId));
        map.put("notifications",notificationRepository.countUnreadByUserId(userId));
        return map;
    }
}
