package com.example.userservice.dto;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CropRequest {

        @NotBlank(message = "Crop name is required")
        @Size(min = 2, max = 100, message = "Crop name must be between 2 and 100 characters")
        private String cropName;

        @NotBlank(message = "Category is required")
        @Size(max = 50, message = "Category must not exceed 50 characters")
        private String category;

        @NotBlank(message = "Variety is required")
        @Size(max = 100, message = "Variety must not exceed 100 characters")
        private String variety;

        @NotNull(message = "Quantity is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Quantity must be greater than 0")
        @DecimalMax(value = "1000000.0", message = "Quantity cannot exceed 1,000,000")
        private Double quantity;

        @NotBlank(message = "Unit is required")
        @Size(min = 1, max = 20, message = "Unit must be between 1 and 20 characters")
        private String unit;

        @NotNull(message = "Price per unit is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        @DecimalMax(value = "1000000.0", message = "Price cannot exceed 1,000,000")
        private BigDecimal pricePerUnit;

        @NotBlank(message = "Location is required")
        @Size(max = 200, message = "Location must not exceed 200 characters")
        private String location;

        @NotNull(message = "Harvest date is required")
        @PastOrPresent(message = "Harvest date cannot be in the future")
        private LocalDate harvestDate;

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        @Column(columnDefinition = "TEXT")
        private String description;

}
