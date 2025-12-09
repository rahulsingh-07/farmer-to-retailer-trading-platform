package com.example.userservice.controller;

import com.example.userservice.dto.UserUpdateDTO;
import com.example.userservice.entity.Users;
import com.example.userservice.serviceImp.UserServiceImp;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
    private final UserServiceImp userService;

    @PatchMapping("/{id}")
    public ResponseEntity<Users> updateUserPartially(
            @PathVariable UUID id,
            @Valid @RequestBody UserUpdateDTO patchDTO) {
        Users updatedUser = userService.updateUserPartially(id, patchDTO);
        return ResponseEntity.ok(updatedUser);
    }
}
