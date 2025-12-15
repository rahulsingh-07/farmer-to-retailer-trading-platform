package com.example.userservice.exception;

public class CropNotFoundException extends RuntimeException {
    public CropNotFoundException(String message) {
        super(message);
    }
}
