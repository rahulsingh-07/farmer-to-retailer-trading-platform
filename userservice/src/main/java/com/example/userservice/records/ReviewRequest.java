package com.example.userservice.records;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record ReviewRequest(

        @NotEmpty(message = "Rating Required")
        @Max(5)
        int rating,
        @NotBlank(message = "Feedback please")
        String comment
) {
}
