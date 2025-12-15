package com.example.userservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");

        ApiError error = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Error",
                message,
                req.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> handleNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Map.of(
                        "error", true,
                        "message", ex.getMessage()
                )
        );
    }

    @ExceptionHandler(UserRejectedException.class)
    public ResponseEntity<?> handleUserRejected(UserRejectedException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                Map.of(
                        "error", true,
                        "message", ex.getMessage()
                )
        );
    }

    @ExceptionHandler(CropNotFoundException.class)
    public ResponseEntity<?> handleUserRejected(CropNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                Map.of(
                        "error", true,
                        "message", ex.getMessage()
                )
        );
    }

    @ExceptionHandler(AuctionNotFoundException.class)
    public ResponseEntity<?> handleUserRejected(AuctionNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                Map.of(
                        "error", true,
                        "message", ex.getMessage()
                )
        );
    }

    @ExceptionHandler(InvalidBidException.class)
    public ResponseEntity<?> handleUserRejected(InvalidBidException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                Map.of(
                        "error", true,
                        "message", ex.getMessage()
                )
        );
    }

}
