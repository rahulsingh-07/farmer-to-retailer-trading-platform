package com.example.userservice.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CropUpdateRequest {
    private Double quantity;
    private String description;
    private BigDecimal pricePerUnit;
}
