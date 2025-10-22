package com.perfect8.image.model;

import com.perfect8.image.enums.ImageStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Image Entity - Version 1.0
 * Magnum Opus Compliant: No @Column(name=...) overrides
 * FIXED: Removed all @Column(name=...) - Hibernate handles camelCase → snake_case
 * FIXED: createdAt/updatedAt → createdDate/updatedDate (Magnum Opus)
 */
@Entity
@Table(name = "images")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;  // → DB: image_id

    private String originalFilename;  // → DB: original_filename

    @Column(unique = true)
    private String storedFilename;  // → DB: stored_filename

    private String mimeType;  // → DB: mime_type

    private String imageFormat;  // → DB: image_format

    private Long originalSizeBytes;  // → DB: original_size_bytes

    private Integer originalWidth;  // → DB: original_width

    private Integer originalHeight;  // → DB: original_height

    private String originalUrl;  // → DB: original_url

    private String thumbnailUrl;  // → DB: thumbnail_url

    private String smallUrl;  // → DB: small_url

    private String mediumUrl;  // → DB: medium_url

    private String largeUrl;  // → DB: large_url

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ImageStatus imageStatus = ImageStatus.PENDING;  // → DB: image_status

    private Long processingTimeMs;  // → DB: processing_time_ms

    private String errorMessage;  // → DB: error_message

    private String altText;  // → DB: alt_text

    private String title;  // → DB: title

    private String category;  // → DB: category

    private String referenceType;  // → DB: reference_type

    private Long referenceId;  // → DB: reference_id

    @Builder.Default
    private Boolean isDeleted = false;  // → DB: is_deleted

    @Column(updatable = false)
    private LocalDateTime createdDate;  // → DB: created_date (Magnum Opus)

    private LocalDateTime updatedDate;  // → DB: updated_date (Magnum Opus)

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }

    // Business logic methods

    public String getBestAvailableUrl() {
        if (largeUrl != null) return largeUrl;
        if (mediumUrl != null) return mediumUrl;
        if (smallUrl != null) return smallUrl;
        if (thumbnailUrl != null) return thumbnailUrl;
        if (originalUrl != null) return originalUrl;
        return null;
    }

    public boolean isAvailable() {
        return imageStatus == ImageStatus.ACTIVE && !isDeleted;
    }

    public boolean areAllSizesGenerated() {
        return thumbnailUrl != null &&
                smallUrl != null &&
                mediumUrl != null &&
                largeUrl != null;
    }

    public void markAsDeleted() {
        this.isDeleted = true;
        this.imageStatus = ImageStatus.DELETED;
    }

    public void setAllUrls(String baseUrl, String filename) {
        String basePath = baseUrl + "/";
        this.originalUrl = basePath + "original/" + filename;
        this.thumbnailUrl = basePath + "thumbnail/" + filename;
        this.smallUrl = basePath + "small/" + filename;
        this.mediumUrl = basePath + "medium/" + filename;
        this.largeUrl = basePath + "large/" + filename;
    }

    // Validation helpers

    public boolean hasValidDimensions() {
        return originalWidth != null &&
                originalWidth > 0 &&
                originalHeight != null &&
                originalHeight > 0;
    }

    public boolean isProcessingComplete() {
        return imageStatus == ImageStatus.ACTIVE ||
                imageStatus == ImageStatus.FAILED;
    }

    public boolean isProcessing() {
        return imageStatus == ImageStatus.PROCESSING;
    }

    public void startProcessing() {
        this.imageStatus = ImageStatus.PROCESSING;
        this.errorMessage = null;
    }

    public void completeProcessing(long processingTime) {
        this.imageStatus = ImageStatus.ACTIVE;
        this.processingTimeMs = processingTime;
        this.errorMessage = null;
    }

    public void failProcessing(String error) {
        this.imageStatus = ImageStatus.FAILED;
        this.errorMessage = error;
    }
}