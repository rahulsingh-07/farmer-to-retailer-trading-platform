package com.example.userservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final String ERR="error";
    private static final String MSG ="message";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");

        ApiError error = new ApiError(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Validation Error",
                message,
                req.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Map.of(
                        ERR, true,
                        MSG, ex.getMessage()
                )
        );
    }

    @ExceptionHandler(UserRejectedException.class)
    public ResponseEntity<Map<String, Object>> handleUserRejected(UserRejectedException ex) {
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(
                Map.of(
                        ERR, true,
                        MSG, ex.getMessage()
                )
        );
    }

    @ExceptionHandler(CropNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserRejected(CropNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                Map.of(
                        ERR, true,
                        MSG, ex.getMessage()
                )
        );
    }

    @ExceptionHandler(AuctionNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserRejected(AuctionNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                Map.of(
                        ERR, true,
                        MSG, ex.getMessage()
                )
        );
    }

    @ExceptionHandler(InvalidBidException.class)
    public ResponseEntity<Map<String, Object>> handleUserRejected(InvalidBidException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                Map.of(
                        ERR, true,
                        MSG, ex.getMessage()
                )
        );
    }

    @ExceptionHandler(ImageUploadException.class)
    public ResponseEntity<Map<String, Object>> handleUserRejected(ImageUploadException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(
                Map.of(
                        ERR, true,
                        MSG, ex.getMessage()
                )
        );
    }

}
