package com.example.userservice.exception;

public class UserRejectedException extends RuntimeException {
    public UserRejectedException(String message) {
        super(message);
    }
}
