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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UsernameGenerator usernameGenerator;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImp userService;

    @Test
    void createFarmerUser_shouldGenerateCredentials_MapAndSave_ReturnSuccess() {
        FarmerRegisterRequest req = new FarmerRegisterRequest();
        String username = "farmerUser";
        String rawPwd = "rawPwd";
        String encodedPwd = "encodedPwd";

        when(usernameGenerator.generate(UserRole.FARMER)).thenReturn(username);
        when(usernameGenerator.generatePassword()).thenReturn(rawPwd);
        when(passwordEncoder.encode(rawPwd)).thenReturn(encodedPwd);

        try (MockedStatic<UserMapper> mapperMock = mockStatic(UserMapper.class)) {
            Users mappedUser = new Users();
            mapperMock.when(() -> UserMapper.toFarmerUser(req, encodedPwd, username))
                    .thenReturn(mappedUser);

            String result = userService.createFarmerUser(req);

            assertEquals("Registration successful", result);
            mapperMock.verify(() -> UserMapper.toFarmerUser(req, encodedPwd, username), times(1));
            verify(userRepository, times(1)).save(mappedUser);
        }
    }

    @Test
    void createRetailerUser_shouldGenerateCredentials_MapAndSave_ReturnSuccess() {
        RetailerRegisterRequest req = new RetailerRegisterRequest();
        String username = "retailerUser";
        String rawPwd = "rawPwd";
        String encodedPwd = "encodedPwd";

        when(usernameGenerator.generate(UserRole.RETAILER)).thenReturn(username);
        when(usernameGenerator.generatePassword()).thenReturn(rawPwd);
        when(passwordEncoder.encode(rawPwd)).thenReturn(encodedPwd);

        try (MockedStatic<UserMapper> mapperMock = mockStatic(UserMapper.class)) {
            Users mappedUser = new Users();
            mapperMock.when(() -> UserMapper.toRetailerUser(req, encodedPwd, username))
                    .thenReturn(mappedUser);

            String result = userService.createRetailerUser(req);

            assertEquals("Registration successful", result);
            mapperMock.verify(() -> UserMapper.toRetailerUser(req, encodedPwd, username), times(1));
            verify(userRepository, times(1)).save(mappedUser);
        }
    }

    @Test
    void createAdminUser_shouldSetFieldsAndSaveAndReturnUser() {
        Users req = new Users();
        String username = "adminUser";
        String rawPwd = "rawPwd";
        String encodedPwd = "encodedPwd";

        when(usernameGenerator.generatePassword()).thenReturn(rawPwd);
        when(usernameGenerator.generate(UserRole.ADMIN)).thenReturn(username);
        when(passwordEncoder.encode(rawPwd)).thenReturn(encodedPwd);

        Users result = userService.createAdminUser(req);

        assertEquals(username, result.getUsername());
        assertEquals(encodedPwd, result.getPassword());
        assertEquals(UserRole.ADMIN, result.getRole());
        assertEquals(UserStatus.ACTIVE, result.getStatus());
        assertNotNull(result.getCreatedAt());
        verify(userRepository, times(1)).save(req);
    }

    @Test
    void updateUserPartially_shouldUpdateOnlyProvidedFields() {
        UUID id = UUID.randomUUID();
        Users existing = new Users();
        existing.setId(id);
        existing.setPassword("oldPwd");
        existing.setEmail("old@example.com");
        existing.setPhoneNumber("111");

        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setPassword("newPwd");
        dto.setEmail(null);      // should not change
        dto.setPhone("222");     // should change

        when(userRepository.findById(id)).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenAnswer(inv -> inv.getArgument(0));

        Users updated = userService.updateUserPartially(id, dto);

        assertEquals("newPwd", updated.getPassword());
        assertEquals("old@example.com", updated.getEmail());
        assertEquals("222", updated.getPhoneNumber());
        assertNotNull(updated.getUpdateAt());
        verify(userRepository, times(1)).save(existing);
    }

    @Test
    void updateUserPartially_shouldThrowWhenUserNotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.updateUserPartially(id, new UserUpdateDTO()));
        assertEquals("User not found", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void getAdminNumbers_shouldReturnCountsFromRepository() {
        when(userRepository.totalUsers()).thenReturn(10);
        when(userRepository.totalPending()).thenReturn(3);
        when(userRepository.totalAdmin()).thenReturn(2);

        Map<String, Integer> result = userService.getAdminNumbers();

        assertEquals(10, result.get("totalUsers"));
        assertEquals(3, result.get("totalPending"));
        assertEquals(2, result.get("totalAdmin"));
    }

    @Test
    void getPublicNumbers_shouldReturnCountsFromRepository() {
        when(userRepository.totalFarmer()).thenReturn(5);
        when(userRepository.totalRetailer()).thenReturn(7);

        Map<String, Integer> result = userService.getPublicNumbers();

        assertEquals(5, result.get("totalFarmer"));
        assertEquals(7, result.get("totalRetailer"));
    }
}
