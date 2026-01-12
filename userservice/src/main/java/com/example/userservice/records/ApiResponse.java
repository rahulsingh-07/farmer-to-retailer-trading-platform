package com.example.userservice.records;

public record ApiResponse<T>(
        boolean success,
        String message,
        T data
) {
    public ApiResponse(String message, T data) {
        this(true, message, data);
    }
}

