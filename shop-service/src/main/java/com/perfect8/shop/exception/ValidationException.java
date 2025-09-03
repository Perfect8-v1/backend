package com.perfect8.shop.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class ValidationException extends RuntimeException {

    private String field;
    private Object rejectedValue;
    private List<String> errors;
    private Map<String, List<String>> fieldErrors;

    // Default constructor
    public ValidationException() {
        super();
        this.errors = new ArrayList<>();
        this.fieldErrors = new HashMap<>();
    }

    // Constructor with message
    public ValidationException(String message) {
        super(message);
        this.errors = new ArrayList<>();
        this.fieldErrors = new HashMap<>();
    }

    // Constructor with message and cause
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
        this.errors = new ArrayList<>();
        this.fieldErrors = new HashMap<>();
    }

    // Constructor with field validation error
    public ValidationException(String field, Object rejectedValue, String message) {
        super(message);
        this.field = field;
        this.rejectedValue = rejectedValue;
        this.errors = new ArrayList<>();
        this.fieldErrors = new HashMap<>();
        addFieldError(field, message);
    }

    // Constructor with multiple errors
    public ValidationException(String message, List<String> errors) {
        super(message);
        this.errors = errors != null ? new ArrayList<>(errors) : new ArrayList<>();
        this.fieldErrors = new HashMap<>();
    }

    // Constructor with field errors map
    public ValidationException(String message, Map<String, List<String>> fieldErrors) {
        super(message);
        this.errors = new ArrayList<>();
        this.fieldErrors = fieldErrors != null ? new HashMap<>(fieldErrors) : new HashMap<>();
    }

    // Static factory methods
    public static ValidationException forField(String field, Object value, String message) {
        return new ValidationException(field, value, message);
    }

    public static ValidationException forMultipleErrors(List<String> errors) {
        return new ValidationException("Validation failed", errors);
    }

    public static ValidationException forFieldErrors(Map<String, List<String>> fieldErrors) {
        return new ValidationException("Field validation failed", fieldErrors);
    }

    public static ValidationException required(String field) {
        return new ValidationException(field, null, field + " is required");
    }

    public static ValidationException invalid(String field, Object value) {
        return new ValidationException(field, value, "Invalid value for " + field);
    }

    public static ValidationException tooLong(String field, Object value, int maxLength) {
        return new ValidationException(
                field,
                value,
                field + " cannot be longer than " + maxLength + " characters"
        );
    }

    public static ValidationException tooShort(String field, Object value, int minLength) {
        return new ValidationException(
                field,
                value,
                field + " must be at least " + minLength + " characters"
        );
    }

    public static ValidationException invalidFormat(String field, Object value, String expectedFormat) {
        return new ValidationException(
                field,
                value,
                field + " must be in format: " + expectedFormat
        );
    }

    public static ValidationException outOfRange(String field, Object value, String range) {
        return new ValidationException(
                field,
                value,
                field + " must be " + range
        );
    }

    public static ValidationException duplicateValue(String field, Object value) {
        return new ValidationException(
                field,
                value,
                field + " already exists"
        );
    }

    // Getters and Setters
    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public Object getRejectedValue() {
        return rejectedValue;
    }

    public void setRejectedValue(Object rejectedValue) {
        this.rejectedValue = rejectedValue;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors != null ? new ArrayList<>(errors) : new ArrayList<>();
    }

    public Map<String, List<String>> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(Map<String, List<String>> fieldErrors) {
        this.fieldErrors = fieldErrors != null ? new HashMap<>(fieldErrors) : new HashMap<>();
    }

    // Utility methods to add errors
    public ValidationException addError(String error) {
        if (error != null && !error.trim().isEmpty()) {
            this.errors.add(error);
        }
        return this;
    }

    public ValidationException addFieldError(String field, String error) {
        if (field != null && error != null && !error.trim().isEmpty()) {
            this.fieldErrors.computeIfAbsent(field, k -> new ArrayList<>()).add(error);
        }
        return this;
    }

    public ValidationException addFieldErrors(String field, List<String> errors) {
        if (field != null && errors != null) {
            this.fieldErrors.computeIfAbsent(field, k -> new ArrayList<>()).addAll(errors);
        }
        return this;
    }

    // Query methods
    public boolean hasErrors() {
        return !errors.isEmpty() || !fieldErrors.isEmpty();
    }

    public boolean hasGlobalErrors() {
        return !errors.isEmpty();
    }

    public boolean hasFieldErrors() {
        return !fieldErrors.isEmpty();
    }

    public boolean hasFieldError(String field) {
        return fieldErrors.containsKey(field) && !fieldErrors.get(field).isEmpty();
    }

    public int getErrorCount() {
        int count = errors.size();
        for (List<String> fieldErrorList : fieldErrors.values()) {
            count += fieldErrorList.size();
        }
        return count;
    }

    public List<String> getFieldErrors(String field) {
        return fieldErrors.getOrDefault(field, new ArrayList<>());
    }

    public List<String> getAllErrors() {
        List<String> allErrors = new ArrayList<>(errors);
        for (Map.Entry<String, List<String>> entry : fieldErrors.entrySet()) {
            String field = entry.getKey();
            for (String error : entry.getValue()) {
                allErrors.add(field + ": " + error);
            }
        }
        return allErrors;
    }

    // Formatting methods
    public String getFormattedErrors() {
        StringBuilder sb = new StringBuilder();

        // Add global errors
        for (String error : errors) {
            sb.append("- ").append(error).append("\n");
        }

        // Add field errors
        for (Map.Entry<String, List<String>> entry : fieldErrors.entrySet()) {
            String field = entry.getKey();
            for (String error : entry.getValue()) {
                sb.append("- ").append(field).append(": ").append(error).append("\n");
            }
        }

        return sb.toString().trim();
    }

    public String getUserFriendlyMessage() {
        if (!hasErrors()) {
            return "Validation failed";
        }

        if (getErrorCount() == 1) {
            List<String> allErrors = getAllErrors();
            return allErrors.get(0);
        } else {
            return String.format("Validation failed with %d errors", getErrorCount());
        }
    }

    public Map<String, Object> getErrorDetails() {
        Map<String, Object> details = new HashMap<>();
        details.put("message", getMessage());
        details.put("errorCount", getErrorCount());

        if (hasGlobalErrors()) {
            details.put("globalErrors", errors);
        }

        if (hasFieldErrors()) {
            details.put("fieldErrors", fieldErrors);
        }

        if (field != null) {
            details.put("field", field);
        }

        if (rejectedValue != null) {
            details.put("rejectedValue", rejectedValue);
        }

        return details;
    }

    // Validation helper methods
    public static void throwIfEmpty(String field, String value) {
        if (value == null || value.trim().isEmpty()) {
            throw ValidationException.required(field);
        }
    }

    public static void throwIfNull(String field, Object value) {
        if (value == null) {
            throw ValidationException.required(field);
        }
    }

    public static void throwIfTooLong(String field, String value, int maxLength) {
        if (value != null && value.length() > maxLength) {
            throw ValidationException.tooLong(field, value, maxLength);
        }
    }

    public static void throwIfTooShort(String field, String value, int minLength) {
        if (value != null && value.length() < minLength) {
            throw ValidationException.tooShort(field, value, minLength);
        }
    }

    public static void throwIfOutOfRange(String field, int value, int min, int max) {
        if (value < min || value > max) {
            throw ValidationException.outOfRange(field, value, "between " + min + " and " + max);
        }
    }

    @Override
    public String toString() {
        return "ValidationException{" +
                "field='" + field + '\'' +
                ", rejectedValue=" + rejectedValue +
                ", errorCount=" + getErrorCount() +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}