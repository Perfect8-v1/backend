package com.perfect8.image.exception;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for Image Service
 * Version 1.0 - Handles all image-related exceptions
 *
 * Uses Lombok for cleaner code
 * Provides consistent error responses
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle ImageNotFoundException - 404
     */
    @ExceptionHandler(ImageNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleImageNotFoundException(ImageNotFoundException e) {
        log.error("Image not found: {}", e.getDetailedMessage());

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .error("Image Not Found")
                .message(e.getUserMessage())
                .details(e.getDetailedMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle InvalidImageException - 400
     */
    @ExceptionHandler(InvalidImageException.class)
    public ResponseEntity<ErrorResponse> handleInvalidImageException(InvalidImageException e) {
        log.error("Invalid image: {}", e.getValidationDetails());

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Invalid Image")
                .message(e.getUserMessage())
                .details(e.getValidationDetails())
                .timestamp(LocalDateTime.now())
                .build();

        // Add validation metadata if available
        if (e.getErrorType() != null) {
            error.setValidationType(e.getErrorType().name());
        }

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle ImageProcessingException - 422
     */
    @ExceptionHandler(ImageProcessingException.class)
    public ResponseEntity<ErrorResponse> handleImageProcessingException(ImageProcessingException e) {
        log.error("Image processing failed: {}", e.getTechnicalDetails());

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .error("Processing Failed")
                .message(e.getUserMessage())
                .details(e.getTechnicalDetails())
                .timestamp(LocalDateTime.now())
                .retryable(e.isRetryable())
                .originalUsable(e.isOriginalUsable())
                .build();

        if (e.getErrorType() != null) {
            error.setProcessingType(e.getErrorType().name());
        }

        return new ResponseEntity<>(error, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    /**
     * Handle StorageException - 500
     */
    @ExceptionHandler(StorageException.class)
    public ResponseEntity<ErrorResponse> handleStorageException(StorageException e) {
        log.error("Storage error: {}", e.getDetailedError());

        // Log critical errors with higher priority
        if (e.isCritical()) {
            log.error("CRITICAL STORAGE ERROR: {}", e.getDetailedError());
            // In production, this could trigger alerts
        }

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Storage Error")
                .message(e.getUserMessage())
                .details(e.getDetailedError())
                .timestamp(LocalDateTime.now())
                .retryable(e.isRecoverable())
                .critical(e.isCritical())
                .build();

        if (e.getErrorType() != null) {
            error.setStorageType(e.getErrorType().name());
        }

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle file size exceeded - 413
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e) {
        log.error("File size exceeded: {}", e.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.PAYLOAD_TOO_LARGE.value())
                .error("File Too Large")
                .message("The uploaded file exceeds the maximum allowed size of 10MB")
                .details(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(error, HttpStatus.PAYLOAD_TOO_LARGE);
    }

    /**
     * Handle validation errors - 400
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException e) {
        Map<String, String> validationErrors = new HashMap<>();

        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });

        log.error("Validation failed: {}", validationErrors);

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Request validation failed")
                .timestamp(LocalDateTime.now())
                .validationErrors(validationErrors)
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle IllegalArgumentException - 400
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("Invalid argument: {}", e.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Invalid Argument")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle IllegalStateException - 409
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException e) {
        log.error("Invalid state: {}", e.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.CONFLICT.value())
                .error("Invalid State")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    /**
     * Handle all other exceptions - 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception e) {
        log.error("Unexpected error: ", e);

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred. Please try again later.")
                .details(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Enhanced error response with Lombok
     * Provides comprehensive error information
     */
    @Data
    @Builder
    public static class ErrorResponse {
        private int status;
        private String error;
        private String message;
        private String details;
        private LocalDateTime timestamp;

        // Additional metadata fields
        private String validationType;     // For InvalidImageException
        private String processingType;     // For ImageProcessingException
        private String storageType;        // For StorageException

        // Flags for client behavior
        @Builder.Default
        private Boolean retryable = false;
        @Builder.Default
        private Boolean critical = false;
        @Builder.Default
        private Boolean originalUsable = false;

        // Validation errors map
        private Map<String, String> validationErrors;

        /**
         * Get simplified response for production
         * Hides technical details in production mode
         */
        public ErrorResponse getProductionSafe() {
            return ErrorResponse.builder()
                    .status(this.status)
                    .error(this.error)
                    .message(this.message)
                    .timestamp(this.timestamp)
                    .retryable(this.retryable)
                    .build();
        }

        /**
         * Check if this is a client error (4xx)
         */
        public boolean isClientError() {
            return status >= 400 && status < 500;
        }

        /**
         * Check if this is a server error (5xx)
         */
        public boolean isServerError() {
            return status >= 500;
        }
    }
}