package com.perfect8.image.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.nio.file.Path;

/**
 * Exception for storage-related errors in image service
 * Version 1.0 - Enhanced with Lombok and detailed error handling
 *
 * Handles file system operations, permissions, space issues
 * Returns 500 INTERNAL SERVER ERROR to client
 */
@Getter
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class StorageException extends RuntimeException {

    /**
     * Storage error types
     */
    public enum StorageError {
        DIRECTORY_CREATE_FAILED("Failed to create storage directory"),
        FILE_WRITE_FAILED("Failed to write file to storage"),
        FILE_READ_FAILED("Failed to read file from storage"),
        FILE_DELETE_FAILED("Failed to delete file from storage"),
        FILE_MOVE_FAILED("Failed to move file in storage"),
        INSUFFICIENT_SPACE("Insufficient storage space"),
        PERMISSION_DENIED("Storage permission denied"),
        FILE_NOT_FOUND("File not found in storage"),
        DIRECTORY_NOT_FOUND("Storage directory not found"),
        INVALID_PATH("Invalid storage path"),
        STORAGE_NOT_CONFIGURED("Storage is not properly configured"),
        CLEANUP_FAILED("Failed to clean up storage"),
        BACKUP_FAILED("Failed to backup file"),
        RESTORE_FAILED("Failed to restore file from backup"),
        UNKNOWN_ERROR("Unknown storage error");

        private final String defaultMessage;

        StorageError(String defaultMessage) {
            this.defaultMessage = defaultMessage;
        }

        public String getDefaultMessage() {
            return defaultMessage;
        }
    }

    private final StorageError errorType;
    private final String filename;
    private final String path;
    private final Long spaceRequired;
    private final Long spaceAvailable;
    private final String operation;

    /**
     * Simple constructor with message
     */
    public StorageException(String message) {
        super(message);
        this.errorType = StorageError.UNKNOWN_ERROR;
        this.filename = null;
        this.path = null;
        this.spaceRequired = null;
        this.spaceAvailable = null;
        this.operation = null;
    }

    /**
     * Constructor with message and cause
     */
    public StorageException(String message, Throwable cause) {
        super(message, cause);
        this.errorType = StorageError.UNKNOWN_ERROR;
        this.filename = null;
        this.path = null;
        this.spaceRequired = null;
        this.spaceAvailable = null;
        this.operation = null;
    }

    /**
     * Constructor with error type
     */
    public StorageException(StorageError errorType, String message) {
        super(message != null ? message : errorType.getDefaultMessage());
        this.errorType = errorType;
        this.filename = null;
        this.path = null;
        this.spaceRequired = null;
        this.spaceAvailable = null;
        this.operation = null;
    }

    /**
     * Constructor with error type and cause
     */
    public StorageException(StorageError errorType, String message, Throwable cause) {
        super(message != null ? message : errorType.getDefaultMessage(), cause);
        this.errorType = errorType;
        this.filename = null;
        this.path = null;
        this.spaceRequired = null;
        this.spaceAvailable = null;
        this.operation = null;
    }

    /**
     * Full constructor for detailed storage errors
     */
    private StorageException(StorageError errorType, String message, String filename,
                             String path, String operation, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
        this.filename = filename;
        this.path = path;
        this.spaceRequired = null;
        this.spaceAvailable = null;
        this.operation = operation;
    }

    /**
     * Constructor for space-related errors
     */
    private StorageException(StorageError errorType, Long spaceRequired, Long spaceAvailable) {
        super(String.format("%s. Required: %d bytes, Available: %d bytes",
                errorType.getDefaultMessage(), spaceRequired, spaceAvailable));
        this.errorType = errorType;
        this.filename = null;
        this.path = null;
        this.spaceRequired = spaceRequired;
        this.spaceAvailable = spaceAvailable;
        this.operation = null;
    }

    /**
     * Static factory methods for common storage errors
     */
    public static StorageException directoryCreateFailed(String path, Throwable cause) {
        return new StorageException(
                StorageError.DIRECTORY_CREATE_FAILED,
                String.format("Failed to create directory: %s", path),
                null, path, "CREATE_DIRECTORY", cause
        );
    }

    public static StorageException fileWriteFailed(String filename, Throwable cause) {
        return new StorageException(
                StorageError.FILE_WRITE_FAILED,
                String.format("Failed to write file: %s", filename),
                filename, null, "WRITE_FILE", cause
        );
    }

    public static StorageException fileReadFailed(String filename, Throwable cause) {
        return new StorageException(
                StorageError.FILE_READ_FAILED,
                String.format("Failed to read file: %s", filename),
                filename, null, "READ_FILE", cause
        );
    }

    public static StorageException fileDeleteFailed(String filename, Throwable cause) {
        return new StorageException(
                StorageError.FILE_DELETE_FAILED,
                String.format("Failed to delete file: %s", filename),
                filename, null, "DELETE_FILE", cause
        );
    }

    public static StorageException insufficientSpace(Long required, Long available) {
        return new StorageException(StorageError.INSUFFICIENT_SPACE, required, available);
    }

    public static StorageException permissionDenied(String path) {
        return new StorageException(
                StorageError.PERMISSION_DENIED,
                String.format("Permission denied for path: %s", path),
                null, path, "ACCESS", null
        );
    }

    public static StorageException fileNotFound(String filename) {
        return new StorageException(
                StorageError.FILE_NOT_FOUND,
                String.format("File not found in storage: %s", filename),
                filename, null, "FIND_FILE", null
        );
    }

    public static StorageException invalidPath(String path) {
        return new StorageException(
                StorageError.INVALID_PATH,
                String.format("Invalid storage path: %s", path),
                null, path, "VALIDATE_PATH", null
        );
    }

    public static StorageException storageNotConfigured() {
        return new StorageException(
                StorageError.STORAGE_NOT_CONFIGURED,
                "Storage system is not properly configured"
        );
    }

    public static StorageException cleanupFailed(String path, Throwable cause) {
        return new StorageException(
                StorageError.CLEANUP_FAILED,
                String.format("Failed to clean up storage at: %s", path),
                null, path, "CLEANUP", cause
        );
    }

    /**
     * Get user-friendly error message for API response
     */
    public String getUserMessage() {
        if (errorType == null) {
            return "A storage error occurred. Please try again later.";
        }

        switch (errorType) {
            case INSUFFICIENT_SPACE:
                return "The server has insufficient storage space. Please contact support.";
            case PERMISSION_DENIED:
                return "Storage access denied. Please contact support.";
            case FILE_NOT_FOUND:
                return "The requested file could not be found.";
            case STORAGE_NOT_CONFIGURED:
                return "Storage system is temporarily unavailable.";
            case FILE_WRITE_FAILED:
                return "Failed to save the file. Please try again.";
            case FILE_READ_FAILED:
                return "Failed to retrieve the file. Please try again.";
            case FILE_DELETE_FAILED:
                return "Failed to delete the file. Please try again.";
            default:
                return "A storage error occurred. Please try again later.";
        }
    }

    /**
     * Get detailed error information for logging
     */
    public String getDetailedError() {
        StringBuilder details = new StringBuilder("Storage Error: ");

        if (errorType != null) {
            details.append("[").append(errorType.name()).append("] ");
        }

        details.append(getMessage());

        if (filename != null) {
            details.append(" | File: ").append(filename);
        }
        if (path != null) {
            details.append(" | Path: ").append(path);
        }
        if (operation != null) {
            details.append(" | Operation: ").append(operation);
        }
        if (spaceRequired != null && spaceAvailable != null) {
            details.append(" | Space Required: ").append(spaceRequired)
                    .append(" bytes, Available: ").append(spaceAvailable).append(" bytes");
        }
        if (getCause() != null) {
            details.append(" | Cause: ").append(getCause().getMessage());
        }

        return details.toString();
    }

    /**
     * Check if error is recoverable (for retry logic)
     */
    public boolean isRecoverable() {
        if (errorType == null) {
            return false;
        }

        switch (errorType) {
            case FILE_WRITE_FAILED:
            case FILE_READ_FAILED:
            case FILE_DELETE_FAILED:
            case FILE_MOVE_FAILED:
            case CLEANUP_FAILED:
                return true;
            case INSUFFICIENT_SPACE:
            case PERMISSION_DENIED:
            case STORAGE_NOT_CONFIGURED:
            case INVALID_PATH:
                return false;
            default:
                return false;
        }
    }

    /**
     * Check if this is a critical error requiring immediate attention
     */
    public boolean isCritical() {
        if (errorType == null) {
            return false;
        }

        switch (errorType) {
            case INSUFFICIENT_SPACE:
            case PERMISSION_DENIED:
            case STORAGE_NOT_CONFIGURED:
            case DIRECTORY_NOT_FOUND:
                return true;
            default:
                return false;
        }
    }
}