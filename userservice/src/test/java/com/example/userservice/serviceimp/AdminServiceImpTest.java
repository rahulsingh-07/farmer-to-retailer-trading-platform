package com.example.userservice.serviceimp;

import com.example.userservice.entity.RetailerDetails;
import com.example.userservice.entity.Users;
import com.example.userservice.enums.UserRole;
import com.example.userservice.enums.UserStatus;
import com.example.userservice.exception.UserNotFoundException;
import com.example.userservice.records.AdminCreateRequest;
import com.example.userservice.records.AdminStatsDto;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.util.UsernameGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceImpTest {
    @Mock private UserRepository userRepository;
    @Mock private TokenServiceImp tokenService;
    @Mock private EmailServiceImp emailServiceImp;
    @Mock private UsernameGenerator usernameGenerator;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private CloudinaryServiceImp cloudinaryServiceImp;
    @InjectMocks
    private AdminServiceImp adminServiceImp;

    private Users user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        user = new Users();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setUsername("testuser");
        user.setFullName("Test User");
    }
    //==== USER STATUS UPDATE ====

    @Test
    void updateStatus_throwsException_whenUserNotFound(){
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(
                UserNotFoundException.class,
                ()->adminServiceImp.updateStatus(userId, UserStatus.INACTIVE,"admin")
        );
        verify(userRepository).findById(userId);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void updateStatus_to_inactive(){
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(tokenService.generateToken(userId)).thenReturn("token123");
        adminServiceImp.updateStatus(userId,UserStatus.INACTIVE,"admin");

        assertEquals(UserStatus.INACTIVE,user.getStatus());
        assertTrue(user.isUpdatePasswordRequired());
        assertEquals(0,user.getPasswordResetAttempts());
        assertEquals("admin",user.getApprovedBy());
        assertNotNull(user.getUpdateAt());

        verify(tokenService).generateToken(userId);
        verify(emailServiceImp).sendPasswordSetupEmail(user.getEmail(),
                "token123",
                user.getUsername(),
                user.getFullName());
        verify(userRepository).save(user);
    }

    @Test
    void updateStatus_to_inactive_failToEmail(){
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        doThrow(new RuntimeException("SMTP DOWN"))
                .when(emailServiceImp)
                .sendPasswordSetupEmail(any(),any(),any(),any());
        assertDoesNotThrow(()->
                adminServiceImp.updateStatus(userId,
                        UserStatus.INACTIVE,
                        "admin"));
        verify(userRepository).save(user);

    }

    @Test
    void updateStatus_user_reject(){
        RetailerDetails rd = new RetailerDetails();
        rd.setTradeLicenseCloudinaryPublicId("cloud123");
        user.setRetailerDetails(rd);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        adminServiceImp.updateStatus(userId,UserStatus.REJECTED,"admin");
        verify(emailServiceImp).sendRejectionNotice(user.getEmail(), user.getFullName());
        verify(cloudinaryServiceImp).deleteFile("cloud123");
        verify(userRepository).delete(user);
    }

    @Test
    void updateStatus_throwsException_forUnsupportedStatus() {
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        assertThrows(
                IllegalArgumentException.class,
                () -> adminServiceImp.updateStatus(
                        userId,
                        UserStatus.ACTIVE,
                        "admin"
                )
        );
    }

    @Test
    void admin_create(){
        AdminCreateRequest adminRequest = new AdminCreateRequest(
                "Admin User",
                "admin@example.com",
                "9876543210"
        );
        String approvedBy = "superAdmin";
        when(usernameGenerator.generatePassword()).thenReturn("generatedPwd");
        when(usernameGenerator.generate(UserRole.ADMIN)).thenReturn("generatedUsername");
        when(passwordEncoder.encode("generatedPwd")).thenReturn("encodedPwd");
        Users savedUser = new Users();
        savedUser.setId(UUID.randomUUID());
        when(userRepository.save(any(Users.class))).thenReturn(savedUser);
        when(tokenService.generateToken(savedUser.getId())).thenReturn("token123");
        adminServiceImp.createAdminUser(adminRequest, approvedBy);
        ArgumentCaptor<Users> userCaptor = ArgumentCaptor.forClass(Users.class);
        verify(userRepository).save(userCaptor.capture());

        Users capturedUser = userCaptor.getValue();
        System.out.println("Captured User: " + capturedUser);
        assertEquals("admin@example.com", capturedUser.getEmail());
        assertEquals("9876543210", capturedUser.getPhoneNumber());
        assertEquals("Admin User", capturedUser.getFullName());
        assertEquals("generatedUsername", capturedUser.getUsername());
        assertEquals("encodedPwd", capturedUser.getPassword());
        assertEquals(UserRole.ADMIN, capturedUser.getRole());
        assertEquals(UserStatus.INACTIVE, capturedUser.getStatus());
        assertEquals(approvedBy, capturedUser.getApprovedBy());
        assertNotNull(capturedUser.getCreatedAt());

        verify(usernameGenerator).generatePassword();
        verify(usernameGenerator).generate(UserRole.ADMIN);
        verify(passwordEncoder).encode("generatedPwd");
        verify(tokenService).generateToken(savedUser.getId());
        verify(emailServiceImp).sendPasswordSetupEmail(
                capturedUser.getEmail(),
                "token123",
                capturedUser.getUsername(),
                capturedUser.getFullName()
        );

    }

    @Test
    void admin_create_emailFails_doesNotThrow() {
        AdminCreateRequest adminRequest = new AdminCreateRequest(
                "Admin User",
                "admin@example.com",
                "9876543210"
        );
        String approvedBy = "superAdmin";
        when(usernameGenerator.generatePassword()).thenReturn("generatedPwd");
        when(usernameGenerator.generate(UserRole.ADMIN)).thenReturn("generatedUsername");
        when(passwordEncoder.encode("generatedPwd")).thenReturn("encodedPwd");
        Users savedUser = new Users();
        savedUser.setId(UUID.randomUUID());
        when(userRepository.save(any(Users.class))).thenReturn(savedUser);
        when(tokenService.generateToken(savedUser.getId())).thenReturn("token123");
        doThrow(new RuntimeException("SMTP down"))
                .when(emailServiceImp)
                .sendPasswordSetupEmail(anyString(), anyString(), anyString(), anyString());

        assertDoesNotThrow(() -> adminServiceImp.createAdminUser(adminRequest, approvedBy));

        verify(userRepository).save(any(Users.class));
    }

    @Test
    void getUserStats_returnsCorrectValues() {
        String username = "adminUser";

        when(userRepository.totalUsers()).thenReturn(100L);
        when(userRepository.totalPending()).thenReturn(10L);
        when(userRepository.totalAdmin()).thenReturn(5L);
        when(userRepository.totalApproved(username)).thenReturn(50L);

        AdminStatsDto stats = adminServiceImp.getUserStats(username);

        assertEquals(100L, stats.totalUsers());
        assertEquals(10L, stats.totalPendingUsers());
        assertEquals(5L, stats.totalAdmin());
        assertEquals(50L, stats.totalApproved());

        verify(userRepository).totalUsers();
        verify(userRepository).totalPending();
        verify(userRepository).totalAdmin();
        verify(userRepository).totalApproved(username);
    }


}
