package com.example.userservice.exception;

import com.example.userservice.records.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

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

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body( new ApiError(
                        LocalDateTime.now(),
                        HttpStatus.BAD_REQUEST.value(),
                        "Validation Error",
                        message,
                        req.getRequestURI()
                        )

                );
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(UserNotFoundException ex,
                                                   HttpServletRequest req) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(
                        new ApiError(
                            LocalDateTime.now(),
                            HttpStatus.NOT_FOUND.value(),
                            "User Not Found",
                            ex.getMessage(),
                            req.getRequestURI()
                        )
                );
    }

    @ExceptionHandler(UserRejectedException.class)
    public ResponseEntity<ApiError> handleUserRejected(UserRejectedException ex,
                                                       HttpServletRequest req) {
        return ResponseEntity
                .status(HttpStatus.NOT_ACCEPTABLE)
                .body(
                        new ApiError(
                                LocalDateTime.now(),
                                HttpStatus.NOT_ACCEPTABLE.value(),
                                "Data is Not Valid",
                                ex.getMessage(),
                                req.getRequestURI()
                        )
                );
    }

    @ExceptionHandler(CropNotFoundException.class)
    public ResponseEntity<ApiError> handleCropNotFound(CropNotFoundException ex,
                                                       HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(
                        new ApiError(
                                LocalDateTime.now(),
                                HttpStatus.NOT_FOUND.value(),
                                "Crop Not Found",
                                ex.getMessage(),
                                req.getRequestURI()
                        )
                );
    }

    @ExceptionHandler(AuctionNotFoundException.class)
    public ResponseEntity<ApiError> handleAuctionNotFound(AuctionNotFoundException ex,
                                                          HttpServletRequest req) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(
                        new ApiError(
                                LocalDateTime.now(),
                                HttpStatus.NOT_FOUND.value(),
                                "Auction Not Found",
                                ex.getMessage(),
                                req.getRequestURI()
                        )
                );
    }

    @ExceptionHandler(InvalidBidException.class)
    public ResponseEntity<ApiError> handleInvalidBid(InvalidBidException ex,
                                                     HttpServletRequest req) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        new ApiError(
                                LocalDateTime.now(),
                                HttpStatus.BAD_REQUEST.value(),
                                "Invalid Bid",
                                ex.getMessage(),
                                req.getRequestURI()
                        )
                );
    }

    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<ApiError> handleFileUpload(FileUploadException ex,
                                                     HttpServletRequest req) {
        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(
                        new ApiError(
                                LocalDateTime.now(),
                                HttpStatus.BAD_GATEWAY.value(),
                                "Fail to Upload",
                                ex.getMessage(),
                                req.getRequestURI()
                        )
                );
    }

    @ExceptionHandler(DuplicateFieldException.class)
    public ResponseEntity<ApiError> handleDuplicateEmail(DuplicateFieldException ex,
                                                         HttpServletRequest req) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(
                        new ApiError(
                                LocalDateTime.now(),
                                HttpStatus.CONFLICT.value(),
                                "Duplicate resource",
                                ex.getMessage(),
                                req.getRequestURI()
                        )
                );
    }

    @ExceptionHandler(PaymentGatewayException.class)
    public ResponseEntity<ApiError> handlePaymentGatewayException(PaymentGatewayException ex,
                                                                  HttpServletRequest req){
        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(
                        new ApiError(
                                LocalDateTime.now(),
                                HttpStatus.BAD_GATEWAY.value(),
                                "Payment Fail",
                                ex.getMessage(),
                                req.getRequestURI()
                        )
                );
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ApiError> handleOrderNotFound(OrderNotFoundException ex,
                                                        HttpServletRequest req) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(
                        new ApiError(
                                LocalDateTime.now(),
                                HttpStatus.NOT_FOUND.value(),
                                "Order Not Found",
                                ex.getMessage(),
                                req.getRequestURI()
                        )
                );
    }
}
