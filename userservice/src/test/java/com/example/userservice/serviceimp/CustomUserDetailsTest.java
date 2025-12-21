package com.example.userservice.serviceimp;

import com.example.userservice.entity.Users;
import com.example.userservice.enums.UserStatus;
import com.example.userservice.enums.UserRole;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CustomUserDetailsTest {

    @Test
    void constructor_shouldMapAllFieldsCorrectly() {
        // Arrange
        UUID userId = UUID.randomUUID();

        Users user = new Users();
        user.setId(userId);
        user.setUsername("veer");
        user.setPassword("secret");
        user.setRole(UserRole.ADMIN);
        user.setStatus(UserStatus.ACTIVE);

        // Act
        CustomUserDetails userDetails = new CustomUserDetails(user);

        // Assert
        assertThat(userDetails.getUserId()).isEqualTo(userId);
        assertThat(userDetails.getUsername()).isEqualTo("veer");
        assertThat(userDetails.getPassword()).isEqualTo("secret");
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities().iterator().next().getAuthority())
                .isEqualTo("ROLE_ADMIN");
        assertThat(userDetails.getUserStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void isEnabled_shouldReturnTrue_whenUserIsActive() {
        Users user = new Users();
        user.setRole(UserRole.ADMIN);
        user.setStatus(UserStatus.ACTIVE);

        CustomUserDetails details = new CustomUserDetails(user);

        assertThat(details.isEnabled()).isTrue();
    }

    @Test
    void isEnabled_shouldReturnFalse_whenUserIsNotActive() {
        Users user = new Users();
        user.setRole(UserRole.ADMIN);
        user.setStatus(UserStatus.INACTIVE);

        CustomUserDetails details = new CustomUserDetails(user);

        assertThat(details.isEnabled()).isFalse();
    }

    @Test
    void accountFlags_shouldAlwaysReturnTrue() {
        Users user = new Users();
        user.setRole(UserRole.ADMIN);
        user.setStatus(UserStatus.ACTIVE);

        CustomUserDetails details = new CustomUserDetails(user);

        assertThat(details.isAccountNonExpired()).isTrue();
        assertThat(details.isAccountNonLocked()).isTrue();
        assertThat(details.isCredentialsNonExpired()).isTrue();
    }

    @Test
    void getAuthorities_shouldHaveRolePrefix() {
        Users user = new Users();
        user.setRole(UserRole.FARMER);
        user.setStatus(UserStatus.ACTIVE);

        CustomUserDetails details = new CustomUserDetails(user);

        assertThat(details.getAuthorities())
                .extracting(a -> a.getAuthority())
                .containsExactly("ROLE_FARMER");
    }
}
