package com.example.userservice.serviceimp;

import com.example.userservice.config.UsernameGenerator;
import com.example.userservice.dto.FarmerRegisterRequest;
import com.example.userservice.dto.RetailerRegisterRequest;
import com.example.userservice.dto.UserUpdateDTO;
import com.example.userservice.entity.Users;
import com.example.userservice.enums.UserRole;
import com.example.userservice.repository.NotificationRepository;
import com.example.userservice.repository.OrderRepository;
import com.example.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImpTest {

    @Mock private UserRepository userRepository;
    @Mock private UsernameGenerator usernameGenerator;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private OrderRepository orderRepository;
    @Mock private NotificationRepository notificationRepository;
    @InjectMocks private UserServiceImp userService;

    private UUID testUserId;
    private Users testUser;
    private FarmerRegisterRequest farmerRequest;
    private RetailerRegisterRequest retailerRequest;
    private UserUpdateDTO updateDTO;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = new Users();
        testUser.setId(testUserId);
        testUser.setEmail("test@example.com");
        testUser.setFullName("Test User");

        farmerRequest = new FarmerRegisterRequest();
        farmerRequest.setFullName("John Farmer");
        farmerRequest.setEmail("farmer@example.com");
        farmerRequest.setPhoneNumber("1234567890");

        retailerRequest = new RetailerRegisterRequest();
        retailerRequest.setFullName("Jane Retailer");
        retailerRequest.setEmail("retailer@example.com");
        retailerRequest.setPhoneNumber("0987654321");

        updateDTO = new UserUpdateDTO();
    }

    // === FARMER REGISTRATION TESTS ===

    @Test
    void createFarmerUser_happyPath_shouldReturnSuccess() {
        // Arrange
        String username = "farmer_abc123";
        String pwd = "pwd_xyz789";
        String encodedPwd = "$2a$10$encodedHash";

        when(usernameGenerator.generate(UserRole.FARMER)).thenReturn(username);
        when(usernameGenerator.generatePassword()).thenReturn(pwd);
        when(passwordEncoder.encode(pwd)).thenReturn(encodedPwd);
        when(userRepository.save(any(Users.class))).thenReturn(testUser);

        // Act
        String result = userService.createFarmerUser(farmerRequest);

        // Assert
        assertThat(result).isEqualTo("Registration successful");
        verify(usernameGenerator).generate(UserRole.FARMER);
        verify(usernameGenerator).generatePassword();
        verify(passwordEncoder).encode(pwd);
        verify(userRepository).save(any(Users.class));
    }

    @Test
    void createFarmerUser_shouldReturnValidationError_whenEmailIsNull() {
        farmerRequest.setEmail(null);
        when(usernameGenerator.generate(UserRole.FARMER)).thenReturn("farmer_test");

        String result = userService.createFarmerUser(farmerRequest);
        assertThat(result).isEqualTo("Registration failed: Email cannot be null");  // ✅
    }


    @Test
    void createFarmerUser_shouldReturnGenericError_whenPasswordEncodingFails() {
        // Arrange
        String pwd = "pwd_test";
        when(usernameGenerator.generate(UserRole.FARMER)).thenReturn("farmer_test");
        when(usernameGenerator.generatePassword()).thenReturn(pwd);
        when(passwordEncoder.encode(pwd)).thenThrow(new RuntimeException("Encoding failed"));

        // Act
        String result = userService.createFarmerUser(farmerRequest);

        // Assert - Generic error for unexpected exceptions
        assertThat(result).startsWith("Registration failed: Unexpected error occurred");
        verifyNoInteractions(userRepository);
    }

    @Test
    void createFarmerUser_shouldReturnGenericError_whenSaveFails() {
        // Arrange
        String pwd = "pwd_test";
        String encodedPwd = "encoded_pwd";
        when(usernameGenerator.generate(UserRole.FARMER)).thenReturn("farmer_test");
        when(usernameGenerator.generatePassword()).thenReturn(pwd);
        when(passwordEncoder.encode(pwd)).thenReturn(encodedPwd);
        when(userRepository.save(any())).thenThrow(new RuntimeException("DB error"));

        // Act
        String result = userService.createFarmerUser(farmerRequest);

        // Assert
        assertThat(result).startsWith("Registration failed: Unexpected error occurred");
    }

    // === RETAILER REGISTRATION TESTS ===

    @Test
    void createRetailerUser_happyPath_shouldReturnSuccess() {
        // Arrange
        String username = "retailer_def456";
        String pwd = "pwd_uvw012";
        String encodedPwd = "$2a$10$encodedHash";

        when(usernameGenerator.generate(UserRole.RETAILER)).thenReturn(username);
        when(usernameGenerator.generatePassword()).thenReturn(pwd);
        when(passwordEncoder.encode(pwd)).thenReturn(encodedPwd);
        when(userRepository.save(any(Users.class))).thenReturn(testUser);

        // Act
        String result = userService.createRetailerUser(retailerRequest);

        // Assert
        assertThat(result).isEqualTo("Registration successful");
        verify(usernameGenerator).generate(UserRole.RETAILER);
        verify(userRepository).save(any());
    }

    @Test
    void createRetailerUser_shouldReturnValidationError_whenEmailIsNull() {
        retailerRequest.setEmail(null);
        when(usernameGenerator.generate(UserRole.RETAILER)).thenReturn("retailer_test");

        String result = userService.createRetailerUser(retailerRequest);
        assertThat(result).isEqualTo("Registration failed: Email cannot be null");
    }

    // === USER UPDATE TESTS ===

    @Test
    void updateUserPartially_shouldUpdateMultipleFields() {
        // Arrange
        updateDTO.setPassword("newPass");
        updateDTO.setEmail("new@example.com");
        updateDTO.setPhone("9999999999");
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(testUser)).thenReturn(testUser);

        // Act
        Users result = userService.updateUserPartially(testUserId, updateDTO);

        // Assert
        assertThat(result).isSameAs(testUser);
        assertThat(testUser.getPassword()).isEqualTo("newPass");
        assertThat(testUser.getEmail()).isEqualTo("new@example.com");
        assertThat(testUser.getPhoneNumber()).isEqualTo("9999999999");
        assertThat(testUser.getUpdateAt()).isNotNull();
    }

    @Test
    void updateUserPartially_shouldHandlePartialUpdate() {
        // Arrange - Only email
        updateDTO.setEmail("partial@example.com");
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(testUser)).thenReturn(testUser);

        // Act
        userService.updateUserPartially(testUserId, updateDTO);

        // Assert - Only email changed
        assertThat(testUser.getEmail()).isEqualTo("partial@example.com");
        assertThat(testUser.getPassword()).isNull();
        verify(userRepository).save(testUser);
    }

    @Test
    void updateUserPartially_shouldThrow_whenUserNotFound() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.updateUserPartially(testUserId, updateDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");

        verify(userRepository).findById(testUserId);
        verify(userRepository, never()).save(any());
    }

    // === DASHBOARD STATS TESTS ===

    @Test
    void getAdminNumbers_shouldReturnAdminStats() {
        // Arrange
        when(userRepository.totalUsers()).thenReturn(100);
        when(userRepository.totalPending()).thenReturn(10);
        when(userRepository.totalAdmin()).thenReturn(5);

        // Act
        Map<String, Integer> result = userService.getAdminNumbers();

        // Assert
        assertThat(result).containsEntry("totalUsers", 100)
                .containsEntry("totalPending", 10)
                .containsEntry("totalAdmin", 5);
    }

    @Test
    void getPublicNumbers_shouldReturnPublicStats() {
        // Arrange
        when(userRepository.totalFarmer()).thenReturn(60);
        when(userRepository.totalRetailer()).thenReturn(40);

        // Act
        Map<String, Integer> result = userService.getPublicNumbers();

        // Assert
        assertThat(result).containsEntry("totalFarmer", 60)
                .containsEntry("totalRetailer", 40);
    }

    @Test
    void dashboardValues_shouldReturnUserMetrics() {
        // Arrange
        when(orderRepository.countNeedConfirmedOrderByRetailerId(testUserId)).thenReturn(5L);
        when(orderRepository.countConfirmedOrderByRetailerId(testUserId)).thenReturn(12L);
        when(orderRepository.countShippedOrderByRetailerId(testUserId)).thenReturn(8L);
        when(notificationRepository.countUnreadByUserId(testUserId)).thenReturn(3L);

        // Act
        Map<String, Long> result = userService.dashboardValues(testUserId);

        // Assert
        assertThat(result).containsEntry("needConfirmation", 5L)
                .containsEntry("confirmed", 12L)
                .containsEntry("shipped", 8L)
                .containsEntry("notifications", 3L);
    }

    @Test
    void dashboardValues_shouldHandleZeroMetrics() {
        // Arrange - All zeros
        when(orderRepository.countNeedConfirmedOrderByRetailerId(testUserId)).thenReturn(0L);
        when(orderRepository.countConfirmedOrderByRetailerId(testUserId)).thenReturn(0L);
        when(orderRepository.countShippedOrderByRetailerId(testUserId)).thenReturn(0L);
        when(notificationRepository.countUnreadByUserId(testUserId)).thenReturn(0L);

        // Act
        Map<String, Long> result = userService.dashboardValues(testUserId);

        // Assert
        assertThat(result.values()).allSatisfy(value -> assertThat(value).isZero());
    }
}
