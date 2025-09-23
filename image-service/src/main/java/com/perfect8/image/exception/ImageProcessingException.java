package com.perfect8.image.exception;

import com.perfect8.image.enums.ImageSize;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception for image processing errors
 * Version 1.0 - Handles resize, format conversion, optimization errors
 *
 * Uses Lombok for clean code
 * Returns 422 UNPROCESSABLE ENTITY to client
 */
@Getter
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class ImageProcessingException extends RuntimeException {

    /**
     * Processing error types
     */
    public enum ProcessingError {
        RESIZE_FAILED("Failed to resize image"),
        FORMAT_CONVERSION_FAILED("Failed to convert image format"),
        THUMBNAIL_GENERATION_FAILED("Failed to generate thumbnail"),
        QUALITY_ADJUSTMENT_FAILED("Failed to adjust image quality"),
        ROTATION_FAILED("Failed to rotate image"),
        CROP_FAILED("Failed to crop image"),
        WATERMARK_FAILED("Failed to apply watermark"),
        METADATA_EXTRACTION_FAILED("Failed to extract image metadata"),
        COLOR_PROFILE_FAILED("Failed to process color profile"),
        COMPRESSION_FAILED("Failed to compress image"),
        OPTIMIZATION_FAILED("Failed to optimize image"),
        MEMORY_INSUFFICIENT("Insufficient memory to process image"),
        TIMEOUT("Image processing timed out"),
        INVALID_DIMENSIONS("Invalid dimensions for processing"),
        UNSUPPORTED_OPERATION("Unsupported image operation"),
        CORRUPTED_DURING_PROCESSING("Image corrupted during processing"),
        MULTIPLE_SIZES_FAILED("Failed to generate multiple sizes"),
        UNKNOWN_PROCESSING_ERROR("Unknown processing error");

        private final String defaultMessage;

        ProcessingError(String defaultMessage) {
            this.defaultMessage = defaultMessage;
        }

        public String getDefaultMessage() {
            return defaultMessage;
        }
    }

    private final ProcessingError errorType;
    private final Long imageId;
    private final String filename;
    private final String operation;
    private final ImageSize targetSize;
    private final Integer sourceWidth;
    private final Integer sourceHeight;
    private final Integer targetWidth;
    private final Integer targetHeight;
    private final String sourceFormat;
    private final String targetFormat;
    private final Long processingTimeMs;
    private final Integer attemptNumber;

    /**
     * Simple constructor with message
     */
    public ImageProcessingException(String message) {
        super(message);
        this.errorType = ProcessingError.UNKNOWN_PROCESSING_ERROR;
        this.imageId = null;
        this.filename = null;
        this.operation = null;
        this.targetSize = null;
        this.sourceWidth = null;
        this.sourceHeight = null;
        this.targetWidth = null;
        this.targetHeight = null;
        this.sourceFormat = null;
        this.targetFormat = null;
        this.processingTimeMs = null;
        this.attemptNumber = null;
    }

    /**
     * Constructor with message and cause
     */
    public ImageProcessingException(String message, Throwable cause) {
        super(message, cause);
        this.errorType = ProcessingError.UNKNOWN_PROCESSING_ERROR;
        this.imageId = null;
        this.filename = null;
        this.operation = null;
        this.targetSize = null;
        this.sourceWidth = null;
        this.sourceHeight = null;
        this.targetWidth = null;
        this.targetHeight = null;
        this.sourceFormat = null;
        this.targetFormat = null;
        this.processingTimeMs = null;
        this.attemptNumber = null;
    }

    /**
     * Constructor with error type and basic info
     */
    private ImageProcessingException(ProcessingError errorType, Long imageId,
                                     String filename, String operation, Throwable cause) {
        super(buildMessage(errorType, imageId, filename, operation), cause);
        this.errorType = errorType;
        this.imageId = imageId;
        this.filename = filename;
        this.operation = operation;
        this.targetSize = null;
        this.sourceWidth = null;
        this.sourceHeight = null;
        this.targetWidth = null;
        this.targetHeight = null;
        this.sourceFormat = null;
        this.targetFormat = null;
        this.processingTimeMs = null;
        this.attemptNumber = null;
    }

    /**
     * Constructor for resize errors
     */
    private ImageProcessingException(ProcessingError errorType, Long imageId, String filename,
                                     Integer sourceWidth, Integer sourceHeight,
                                     Integer targetWidth, Integer targetHeight,
                                     Throwable cause) {
        super(buildResizeMessage(errorType, filename, sourceWidth, sourceHeight,
                targetWidth, targetHeight), cause);
        this.errorType = errorType;
        this.imageId = imageId;
        this.filename = filename;
        this.operation = "RESIZE";
        this.targetSize = null;
        this.sourceWidth = sourceWidth;
        this.sourceHeight = sourceHeight;
        this.targetWidth = targetWidth;
        this.targetHeight = targetHeight;
        this.sourceFormat = null;
        this.targetFormat = null;
        this.processingTimeMs = null;
        this.attemptNumber = null;
    }

    /**
     * Constructor for format conversion errors
     */
    private ImageProcessingException(ProcessingError errorType, Long imageId, String filename,
                                     String sourceFormat, String targetFormat, Throwable cause) {
        super(buildFormatMessage(errorType, filename, sourceFormat, targetFormat), cause);
        this.errorType = errorType;
        this.imageId = imageId;
        this.filename = filename;
        this.operation = "FORMAT_CONVERSION";
        this.targetSize = null;
        this.sourceWidth = null;
        this.sourceHeight = null;
        this.targetWidth = null;
        this.targetHeight = null;
        this.sourceFormat = sourceFormat;
        this.targetFormat = targetFormat;
        this.processingTimeMs = null;
        this.attemptNumber = null;
    }

    /**
     * Static factory methods for common processing errors
     */
    public static ImageProcessingException resizeFailed(Long imageId, String filename,
                                                        Integer sourceWidth, Integer sourceHeight,
                                                        Integer targetWidth, Integer targetHeight,
                                                        Throwable cause) {
        return new ImageProcessingException(ProcessingError.RESIZE_FAILED, imageId, filename,
                sourceWidth, sourceHeight, targetWidth, targetHeight, cause);
    }

    public static ImageProcessingException thumbnailFailed(Long imageId, String filename, Throwable cause) {
        return new ImageProcessingException(ProcessingError.THUMBNAIL_GENERATION_FAILED,
                imageId, filename, "THUMBNAIL", cause);
    }

    public static ImageProcessingException formatConversionFailed(Long imageId, String filename,
                                                                  String sourceFormat, String targetFormat,
                                                                  Throwable cause) {
        return new ImageProcessingException(ProcessingError.FORMAT_CONVERSION_FAILED,
                imageId, filename, sourceFormat, targetFormat, cause);
    }

    public static ImageProcessingException multipleSizesFailed(Long imageId, String filename) {
        return new ImageProcessingException(ProcessingError.MULTIPLE_SIZES_FAILED,
                imageId, filename, "GENERATE_SIZES", null);
    }

    public static ImageProcessingException compressionFailed(Long imageId, String filename, Throwable cause) {
        return new ImageProcessingException(ProcessingError.COMPRESSION_FAILED,
                imageId, filename, "COMPRESS", cause);
    }

    public static ImageProcessingException metadataExtractionFailed(Long imageId, String filename, Throwable cause) {
        return new ImageProcessingException(ProcessingError.METADATA_EXTRACTION_FAILED,
                imageId, filename, "EXTRACT_METADATA", cause);
    }

    public static ImageProcessingException memoryInsufficient(Long imageId, String filename) {
        return new ImageProcessingException(ProcessingError.MEMORY_INSUFFICIENT,
                imageId, filename, "PROCESS", null);
    }

    public static ImageProcessingException timeout(Long imageId, String filename, Long processingTimeMs) {
        ImageProcessingException e = new ImageProcessingException(ProcessingError.TIMEOUT,
                imageId, filename, "PROCESS", null);
        return e;
    }

    public static ImageProcessingException invalidDimensions(String filename,
                                                             Integer width, Integer height) {
        return new ImageProcessingException(
                String.format("Invalid dimensions for %s: %dx%d", filename, width, height)
        );
    }

    public static ImageProcessingException corruptedDuringProcessing(Long imageId, String filename) {
        return new ImageProcessingException(ProcessingError.CORRUPTED_DURING_PROCESSING,
                imageId, filename, "PROCESS", null);
    }

    /**
     * Helper method to build error message
     */
    private static String buildMessage(ProcessingError errorType, Long imageId,
                                       String filename, String operation) {
        return String.format("%s - Image: %s (ID: %s), Operation: %s",
                errorType.getDefaultMessage(),
                filename != null ? filename : "unknown",
                imageId != null ? imageId : "N/A",
                operation != null ? operation : "unknown");
    }

    /**
     * Helper method for resize error message
     */
    private static String buildResizeMessage(ProcessingError errorType, String filename,
                                             Integer sourceWidth, Integer sourceHeight,
                                             Integer targetWidth, Integer targetHeight) {
        return String.format("%s - File: %s, From: %dx%d, To: %dx%d",
                errorType.getDefaultMessage(), filename,
                sourceWidth, sourceHeight, targetWidth, targetHeight);
    }

    /**
     * Helper method for format conversion message
     */
    private static String buildFormatMessage(ProcessingError errorType, String filename,
                                             String sourceFormat, String targetFormat) {
        return String.format("%s - File: %s, From: %s, To: %s",
                errorType.getDefaultMessage(), filename,
                sourceFormat, targetFormat);
    }

    /**
     * Get user-friendly error message
     */
    public String getUserMessage() {
        if (errorType == null) {
            return "Failed to process the image. Please try again.";
        }

        switch (errorType) {
            case RESIZE_FAILED:
                return "Failed to resize the image. Please try a different image.";
            case THUMBNAIL_GENERATION_FAILED:
                return "Failed to generate image preview. The original image has been saved.";
            case FORMAT_CONVERSION_FAILED:
                return "Failed to convert image format. Please try a different format.";
            case MEMORY_INSUFFICIENT:
                return "The image is too large to process. Please upload a smaller image.";
            case TIMEOUT:
                return "Image processing took too long. Please try a smaller image.";
            case INVALID_DIMENSIONS:
                return "The image dimensions are not supported. Please check the image requirements.";
            case CORRUPTED_DURING_PROCESSING:
                return "The image became corrupted during processing. Please upload it again.";
            case MULTIPLE_SIZES_FAILED:
                return "Failed to generate all image sizes. Some sizes may be unavailable.";
            case COMPRESSION_FAILED:
                return "Failed to optimize the image. The original has been saved.";
            default:
                return "Failed to process the image. Please try again.";
        }
    }

    /**
     * Get technical details for logging
     */
    public String getTechnicalDetails() {
        StringBuilder details = new StringBuilder("Processing Error: ");

        if (errorType != null) {
            details.append("[").append(errorType.name()).append("] ");
        }

        if (imageId != null) {
            details.append("ImageID: ").append(imageId).append(", ");
        }
        if (filename != null) {
            details.append("File: ").append(filename).append(", ");
        }
        if (operation != null) {
            details.append("Operation: ").append(operation).append(", ");
        }
        if (sourceWidth != null && sourceHeight != null) {
            details.append("Source: ").append(sourceWidth).append("x").append(sourceHeight).append(", ");
        }
        if (targetWidth != null && targetHeight != null) {
            details.append("Target: ").append(targetWidth).append("x").append(targetHeight).append(", ");
        }
        if (sourceFormat != null && targetFormat != null) {
            details.append("Format: ").append(sourceFormat).append("→").append(targetFormat).append(", ");
        }
        if (processingTimeMs != null) {
            details.append("Time: ").append(processingTimeMs).append("ms, ");
        }
        if (attemptNumber != null) {
            details.append("Attempt: ").append(attemptNumber).append(", ");
        }
        if (getCause() != null) {
            details.append("Cause: ").append(getCause().getMessage());
        }

        return details.toString().replaceAll(", $", "");
    }

    /**
     * Check if error is retryable
     */
    public boolean isRetryable() {
        if (errorType == null) {
            return false;
        }

        switch (errorType) {
            case RESIZE_FAILED:
            case THUMBNAIL_GENERATION_FAILED:
            case COMPRESSION_FAILED:
            case OPTIMIZATION_FAILED:
            case TIMEOUT:
                return true;
            case INVALID_DIMENSIONS:
            case UNSUPPORTED_OPERATION:
            case MEMORY_INSUFFICIENT:
            case CORRUPTED_DURING_PROCESSING:
                return false;
            default:
                return false;
        }
    }

    /**
     * Check if original image is still usable
     */
    public boolean isOriginalUsable() {
        if (errorType == null) {
            return true;
        }

        switch (errorType) {
            case THUMBNAIL_GENERATION_FAILED:
            case COMPRESSION_FAILED:
            case OPTIMIZATION_FAILED:
            case MULTIPLE_SIZES_FAILED:
                return true;
            case CORRUPTED_DURING_PROCESSING:
                return false;
            default:
                return true;
        }
    }
}