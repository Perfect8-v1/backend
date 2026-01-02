package com.perfect8.image.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when an image is not found
 * Version 1.0 - Core exception handling
 *
 * Uses Lombok for getters
 * Returns 404 NOT FOUND to client
 */
@Getter
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ImageNotFoundException extends RuntimeException {

    private final Long imageId;
    private final String identifier;
    private final String referenceType;
    private final Long referenceId;

    /**
     * Constructor for image not found by ID
     */
    public ImageNotFoundException(String message) {
        super(message);
        this.imageId = null;
        this.identifier = null;
        this.referenceType = null;
        this.referenceId = null;
    }

    /**
     * Constructor with image ID
     */
    public ImageNotFoundException(String message, Long imageId) {
        super(message + " - Image ID: " + imageId);
        this.imageId = imageId;
        this.identifier = null;
        this.referenceType = null;
        this.referenceId = null;
    }

    /**
     * Constructor with filename/identifier
     */
    public ImageNotFoundException(String message, String identifier) {
        super(message + " - Identifier: " + identifier);
        this.imageId = null;
        this.identifier = identifier;
        this.referenceType = null;
        this.referenceId = null;
    }

    /**
     * Constructor for reference-based search
     */
    public ImageNotFoundException(String message, String referenceType, Long referenceId) {
        super(message + " - Reference: " + referenceType + "/" + referenceId);
        this.imageId = null;
        this.identifier = null;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
    }

    /**
     * Constructor with cause
     */
    public ImageNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.imageId = null;
        this.identifier = null;
        this.referenceType = null;
        this.referenceId = null;
    }

    /**
     * Full constructor with all details
     */
    public ImageNotFoundException(String message, Long imageId, Throwable cause) {
        super(message + " - Image ID: " + imageId, cause);
        this.imageId = imageId;
        this.identifier = null;
        this.referenceType = null;
        this.referenceId = null;
    }

    /**
     * Static factory methods for common cases
     */
    public static ImageNotFoundException byId(Long imageId) {
        return new ImageNotFoundException("Image not found", imageId);
    }

    public static ImageNotFoundException byFilename(String filename) {
        return new ImageNotFoundException("Image not found with filename", filename);
    }

    public static ImageNotFoundException byReference(String referenceType, Long referenceId) {
        return new ImageNotFoundException("No images found for reference", referenceType, referenceId);
    }

    /**
     * Get user-friendly error message
     */
    public String getUserMessage() {
        if (imageId != null) {
            return "The requested image could not be found.";
        } else if (identifier != null) {
            return "The requested image '" + identifier + "' could not be found.";
        } else if (referenceType != null && referenceId != null) {
            return "No images found for the requested item.";
        } else {
            return "The requested image could not be found.";
        }
    }

    /**
     * Get detailed error for logging
     */
    public String getDetailedMessage() {
        StringBuilder details = new StringBuilder("Image not found - ");

        if (imageId != null) {
            details.append("ID: ").append(imageId);
        }
        if (identifier != null) {
            details.append("Identifier: ").append(identifier);
        }
        if (referenceType != null && referenceId != null) {
            details.append("Reference: ").append(referenceType)
                    .append("/").append(referenceId);
        }

        return details.toString();
    }
}