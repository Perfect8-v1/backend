package com.perfect8.image.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.perfect8.image.enums.ImageSize;
import com.perfect8.image.enums.ImageStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Data Transfer Object for Image API responses
 * Version 1.0 - Core image information
 *
 * Uses Lombok for clean code
 * Follows "Less Strings, More Objects" principle
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ImageDto {

    /**
     * Image identification
     */
    private Long imageId;
    private String originalFilename;
    private String storedFilename;

    /**
     * Image URLs for different sizes
     * Using Map for flexibility and clean JSON
     */
    private Map<ImageSize, String> urls;

    /**
     * Fallback individual URL fields for backward compatibility
     */
    private String originalUrl;
    private String thumbnailUrl;
    private String smallUrl;
    private String mediumUrl;
    private String largeUrl;

    /**
     * Best available URL (computed field)
     */
    private String displayUrl;

    /**
     * Image metadata
     */
    private String imageFormat;
    private String mimeType;
    private Integer originalWidth;
    private Integer originalHeight;
    private Long originalSizeBytes;
    private String readableSize; // "1.5 MB"

    /**
     * Image status
     */
    private ImageStatus imageStatus;
    private Boolean isAvailable;
    private Boolean allSizesGenerated;

    /**
     * Reference information
     */
    private String referenceType;
    private Long referenceId;

    /**
     * SEO and accessibility
     */
    private String altText;
    private String title;
    private String caption;

    /**
     * Processing information
     */
    private Long processingTimeMs;
    private String errorMessage;

    /**
     * Audit fields
     */
    private Long uploadedBy;
    private String uploadedByUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Usage statistics (for v2.0, but structure ready)
     */
    private Long viewCount;
    private LocalDateTime lastViewedAt;

    /**
     * Nested DTO for image dimensions
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DimensionDto {
        private Integer width;
        private Integer height;
        private Double aspectRatio;
        private String orientation; // "landscape", "portrait", "square"

        /**
         * Create from width and height
         */
        public static DimensionDto from(Integer width, Integer height) {
            if (width == null || height == null) {
                return null;
            }

            String orientation;
            if (width > height) {
                orientation = "landscape";
            } else if (height > width) {
                orientation = "portrait";
            } else {
                orientation = "square";
            }

            return DimensionDto.builder()
                    .width(width)
                    .height(height)
                    .aspectRatio((double) width / height)
                    .orientation(orientation)
                    .build();
        }
    }

    /**
     * Nested DTO for processing status
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProcessingStatusDto {
        private ImageStatus status;
        private Boolean isProcessing;
        private Boolean isComplete;
        private Boolean hasFailed;
        private String failureReason;
        private Long processingTimeMs;

        /**
         * Create from ImageStatus
         */
        public static ProcessingStatusDto from(ImageStatus status, String errorMessage, Long processingTime) {
            return ProcessingStatusDto.builder()
                    .status(status)
                    .isProcessing(status == ImageStatus.PROCESSING)
                    .isComplete(status == ImageStatus.ACTIVE)
                    .hasFailed(status == ImageStatus.FAILED)
                    .failureReason(errorMessage)
                    .processingTimeMs(processingTime)
                    .build();
        }
    }

    /**
     * Create simplified DTO for list views
     */
    public static ImageDto createSimple(Long imageId, String filename, String displayUrl, ImageStatus status) {
        return ImageDto.builder()
                .imageId(imageId)
                .originalFilename(filename)
                .displayUrl(displayUrl)
                .imageStatus(status)
                .isAvailable(status == ImageStatus.ACTIVE)
                .build();
    }

    /**
     * Create thumbnail-only DTO
     */
    public static ImageDto createThumbnail(Long imageId, String thumbnailUrl, String altText) {
        return ImageDto.builder()
                .imageId(imageId)
                .thumbnailUrl(thumbnailUrl)
                .altText(altText)
                .build();
    }

    /**
     * Helper to convert bytes to readable format
     */
    public String getReadableSize() {
        if (originalSizeBytes == null) {
            return null;
        }

        if (originalSizeBytes < 1024) {
            return originalSizeBytes + " B";
        } else if (originalSizeBytes < 1024 * 1024) {
            return String.format("%.1f KB", originalSizeBytes / 1024.0);
        } else if (originalSizeBytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", originalSizeBytes / (1024.0 * 1024));
        } else {
            return String.format("%.1f GB", originalSizeBytes / (1024.0 * 1024 * 1024));
        }
    }

    /**
     * Get dimension info as nested object
     */
    public DimensionDto getDimensions() {
        return DimensionDto.from(originalWidth, originalHeight);
    }

    /**
     * Get processing status as nested object
     */
    public ProcessingStatusDto getProcessingStatus() {
        return ProcessingStatusDto.from(imageStatus, errorMessage, processingTimeMs);
    }

    /**
     * Check if image is ready for use
     */
    public boolean isReady() {
        return imageStatus == ImageStatus.ACTIVE && displayUrl != null;
    }

    /**
     * Check if image needs alt text (accessibility)
     */
    public boolean needsAltText() {
        return altText == null || altText.trim().isEmpty();
    }

    /**
     * Get the best URL for display
     */
    public String getBestDisplayUrl() {
        if (displayUrl != null) return displayUrl;
        if (mediumUrl != null) return mediumUrl;
        if (smallUrl != null) return smallUrl;
        if (largeUrl != null) return largeUrl;
        if (thumbnailUrl != null) return thumbnailUrl;
        return originalUrl;
    }
}