package com.perfect8.image.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when image validation fails
 * Version 1.0 - Handles format, size, dimension validation errors
 *
 * Uses Lombok for cleaner code
 * Returns 400 BAD REQUEST to client
 */
@Getter
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidImageException extends RuntimeException {

    /**
     * Validation error types
     */
    public enum ValidationError {
        EMPTY_FILE("File is empty"),
        FILE_TOO_LARGE("File size exceeds maximum allowed size"),
        INVALID_FORMAT("File format is not supported"),
        NOT_AN_IMAGE("File is not a valid image"),
        DIMENSIONS_TOO_SMALL("Image dimensions are too small"),
        DIMENSIONS_TOO_LARGE("Image dimensions are too large"),
        CORRUPT_FILE("Image file is corrupted"),
        MISSING_FILENAME("Filename is missing"),
        INVALID_MIME_TYPE("MIME type is not supported"),
        PROCESSING_ERROR("Failed to process image");

        private final String defaultMessage;

        ValidationError(String defaultMessage) {
            this.defaultMessage = defaultMessage;
        }

        public String getDefaultMessage() {
            return defaultMessage;
        }
    }

    private final ValidationError errorType;
    private final String filename;
    private final Long fileSize;
    private final String mimeType;
    private final Integer width;
    private final Integer height;
    private final String allowedFormats;
    private final Long maxSize;

    /**
     * Simple constructor with message only
     */
    public InvalidImageException(String message) {
        super(message);
        this.errorType = null;
        this.filename = null;
        this.fileSize = null;
        this.mimeType = null;
        this.width = null;
        this.height = null;
        this.allowedFormats = null;
        this.maxSize = null;
    }

    /**
     * Constructor with error type
     */
    public InvalidImageException(ValidationError errorType) {
        super(errorType.getDefaultMessage());
        this.errorType = errorType;
        this.filename = null;
        this.fileSize = null;
        this.mimeType = null;
        this.width = null;
        this.height = null;
        this.allowedFormats = null;
        this.maxSize = null;
    }

    /**
     * Constructor for file size errors
     */
    public InvalidImageException(ValidationError errorType, Long fileSize, Long maxSize) {
        super(String.format("%s. File size: %d bytes, Max allowed: %d bytes",
                errorType.getDefaultMessage(), fileSize, maxSize));
        this.errorType = errorType;
        this.filename = null;
        this.fileSize = fileSize;
        this.mimeType = null;
        this.width = null;
        this.height = null;
        this.allowedFormats = null;
        this.maxSize = maxSize;
    }

    /**
     * Constructor for format errors
     */
    public InvalidImageException(ValidationError errorType, String filename, String mimeType, String allowedFormats) {
        super(String.format("%s. File: %s, Type: %s, Allowed: %s",
                errorType.getDefaultMessage(), filename, mimeType, allowedFormats));
        this.errorType = errorType;
        this.filename = filename;
        this.fileSize = null;
        this.mimeType = mimeType;
        this.width = null;
        this.height = null;
        this.allowedFormats = allowedFormats;
        this.maxSize = null;
    }

    /**
     * Constructor for dimension errors
     */
    public InvalidImageException(ValidationError errorType, Integer width, Integer height) {
        super(String.format("%s. Dimensions: %dx%d", errorType.getDefaultMessage(), width, height));
        this.errorType = errorType;
        this.filename = null;
        this.fileSize = null;
        this.mimeType = null;
        this.width = width;
        this.height = height;
        this.allowedFormats = null;
        this.maxSize = null;
    }

    /**
     * Constructor with cause
     */
    public InvalidImageException(String message, Throwable cause) {
        super(message, cause);
        this.errorType = ValidationError.PROCESSING_ERROR;
        this.filename = null;
        this.fileSize = null;
        this.mimeType = null;
        this.width = null;
        this.height = null;
        this.allowedFormats = null;
        this.maxSize = null;
    }

    /**
     * Static factory methods for common validation errors
     */
    public static InvalidImageException emptyFile() {
        return new InvalidImageException(ValidationError.EMPTY_FILE);
    }

    public static InvalidImageException fileTooLarge(Long fileSize, Long maxSize) {
        return new InvalidImageException(ValidationError.FILE_TOO_LARGE, fileSize, maxSize);
    }

    public static InvalidImageException invalidFormat(String filename, String mimeType, String allowedFormats) {
        return new InvalidImageException(ValidationError.INVALID_FORMAT, filename, mimeType, allowedFormats);
    }

    public static InvalidImageException notAnImage(String filename) {
        InvalidImageException e = new InvalidImageException(ValidationError.NOT_AN_IMAGE);
        return new InvalidImageException(
                String.format("File '%s' is not a valid image", filename)
        );
    }

    public static InvalidImageException dimensionsTooSmall(Integer width, Integer height) {
        return new InvalidImageException(ValidationError.DIMENSIONS_TOO_SMALL, width, height);
    }

    public static InvalidImageException dimensionsTooLarge(Integer width, Integer height) {
        return new InvalidImageException(ValidationError.DIMENSIONS_TOO_LARGE, width, height);
    }

    public static InvalidImageException corruptFile(String filename) {
        return new InvalidImageException(
                String.format("File '%s' appears to be corrupted", filename)
        );
    }

    /**
     * Get user-friendly error message for API response
     */
    public String getUserMessage() {
        if (errorType == null) {
            return getMessage();
        }

        switch (errorType) {
            case FILE_TOO_LARGE:
                return String.format("The image is too large. Maximum size is %d MB.",
                        maxSize != null ? maxSize / (1024 * 1024) : 10);
            case INVALID_FORMAT:
                return String.format("Invalid image format. Supported formats: %s",
                        allowedFormats != null ? allowedFormats : "JPG, PNG, WEBP");
            case NOT_AN_IMAGE:
                return "The uploaded file is not a valid image.";
            case DIMENSIONS_TOO_SMALL:
                return "Image resolution is too low. Please upload a higher resolution image.";
            case DIMENSIONS_TOO_LARGE:
                return "Image resolution is too high. Please upload a smaller image.";
            case EMPTY_FILE:
                return "Please select an image to upload.";
            case CORRUPT_FILE:
                return "The image file appears to be damaged. Please try a different file.";
            default:
                return errorType.getDefaultMessage();
        }
    }

    /**
     * Build validation details for logging/debugging
     */
    public String getValidationDetails() {
        StringBuilder details = new StringBuilder("Image validation failed: ");

        if (errorType != null) {
            details.append(errorType.name()).append(" - ");
        }
        if (filename != null) {
            details.append("File: ").append(filename).append(", ");
        }
        if (fileSize != null) {
            details.append("Size: ").append(fileSize).append(" bytes, ");
        }
        if (mimeType != null) {
            details.append("Type: ").append(mimeType).append(", ");
        }
        if (width != null && height != null) {
            details.append("Dimensions: ").append(width).append("x").append(height).append(", ");
        }

        return details.toString().replaceAll(", $", "");
    }
}