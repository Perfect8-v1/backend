package com.perfect8.shop.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    private String resourceName;
    private String fieldName;
    private Object fieldValue;

    // Default constructor
    public ResourceNotFoundException() {
        super();
    }

    // Constructor with message
    public ResourceNotFoundException(String message) {
        super(message);
    }

    // Constructor with message and cause
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    // Constructor with resource details
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    // Constructor with resource details and custom message
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue, String customMessage) {
        super(customMessage);
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    // Static factory methods for common cases
    public static ResourceNotFoundException forId(String resourceName, Object id) {
        return new ResourceNotFoundException(resourceName, "id", id);
    }

    public static ResourceNotFoundException forField(String resourceName, String fieldName, Object fieldValue) {
        return new ResourceNotFoundException(resourceName, fieldName, fieldValue);
    }

    public static ResourceNotFoundException withMessage(String message) {
        return new ResourceNotFoundException(message);
    }

    // Getters
    public String getResourceName() {
        return resourceName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getFieldValue() {
        return fieldValue;
    }

    // Utility methods
    public boolean hasResourceDetails() {
        return resourceName != null && fieldName != null && fieldValue != null;
    }

    public String getFormattedMessage() {
        if (hasResourceDetails()) {
            return String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue);
        }
        return getMessage();
    }

    @Override
    public String toString() {
        return "ResourceNotFoundException{" +
                "resourceName='" + resourceName + '\'' +
                ", fieldName='" + fieldName + '\'' +
                ", fieldValue=" + fieldValue +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}