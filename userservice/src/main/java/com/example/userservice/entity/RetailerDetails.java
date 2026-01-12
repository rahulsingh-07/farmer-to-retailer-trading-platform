package com.example.userservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class RetailerDetails {
    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne
    @JoinColumn(name = "user_id",nullable = false)
    private Users user;
    private String businessAddress;
    private String tradeLicenseUrl;
    private String tradeLicenseCloudinaryPublicId;
}
