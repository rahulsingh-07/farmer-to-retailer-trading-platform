package com.example.userservice.serviceimp;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.userservice.exception.DuplicateFieldException;
import com.example.userservice.exception.FileUploadException;
import com.example.userservice.records.ChatbotQuestionDto;
import com.example.userservice.records.PublicStats;
import com.example.userservice.records.RetailerStats;
import com.example.userservice.repository.ChatbotQuestionRepository;
import com.example.userservice.util.UsernameGenerator;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImp implements UserService {
    private final UserRepository userRepository;
    private final UsernameGenerator usernameGenerator;
    private final PasswordEncoder passwordEncoder;
    private final OrderRepository orderRepository;
    private final NotificationRepository notificationRepository;
    private final CloudinaryServiceImp cloudinaryServiceImp;
    private final ChatbotQuestionRepository chatbot;

    @Override
    public void createFarmerUser(FarmerRegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new DuplicateFieldException("Email already exists");
        }
        String generatedUsername = usernameGenerator.generate(UserRole.FARMER);
        String generatedPwd = usernameGenerator.generatePassword();
        Users user = UserMapper.toFarmerUser(
                req,
                passwordEncoder.encode(generatedPwd),
                generatedUsername
        );

        userRepository.save(user);
    }

    @Override
    public void createRetailerUser(RetailerRegisterRequest req, MultipartFile tradeLicense) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new DuplicateFieldException("Email already exists");
        }
        try {
            Map<String, String> uploadResult = cloudinaryServiceImp.uploadFile(tradeLicense, "trade-licenses");
            String licenseUrl = uploadResult.get("url");
            String publicId = uploadResult.get("publicId");

            String generatedUsername = usernameGenerator.generate(UserRole.RETAILER);
            String generatedPwd = usernameGenerator.generatePassword();

            // Map to Users entity
            Users user = UserMapper.toRetailerUser(
                    req,
                    passwordEncoder.encode(generatedPwd),
                    generatedUsername,
                    licenseUrl,
                    publicId
            );

            userRepository.save(user);

        } catch (IOException e) {
            log.error("Trade license upload failed", e);
            throw new FileUploadException("Trade license upload failed", e);
        }
    }




    public void updateUserPartially(UUID id, UserUpdateDTO patchDTO) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (patchDTO.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(patchDTO.getPassword()));
        }
        if (patchDTO.getEmail() != null) {
            user.setEmail(patchDTO.getEmail());
        }
        if (patchDTO.getPhone() != null) {
            user.setPhoneNumber(patchDTO.getPhone());
        }
        user.setUpdateAt(LocalDateTime.now());
        userRepository.save(user);
    }


    public PublicStats getPublicStats() {
        return new PublicStats(
                userRepository.totalFarmer(),
                userRepository.totalRetailer(),
                orderRepository.totalTrades()
        );
    }


    public RetailerStats dashboardValues(UUID userId) {

        return new RetailerStats(
                orderRepository.countNeedConfirmedOrderByRetailerId(userId),
                orderRepository.countConfirmedOrderByRetailerId(userId),
                orderRepository.countShippedOrderByRetailerId(userId),
                notificationRepository.countUnreadByUserId(userId)
        );
    }

    public List<ChatbotQuestionDto> getQuestions(String role, String lang) {
        return chatbot
                .findByRoleAndLanguageAndIsActiveOrderByOrderIndex(
                        role.toUpperCase(),
                        lang,
                        true
                )
                .stream()
                .map(q -> new ChatbotQuestionDto(
                        q.getId(),
                        q.getQuestion(),
                        q.getAnswer()
                ))
                .toList();
    }

}
