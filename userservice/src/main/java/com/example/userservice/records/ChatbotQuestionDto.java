package com.example.userservice.records;

public record ChatbotQuestionDto(
        Long id,
        String question,
        String answer
) {
}
