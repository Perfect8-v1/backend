package com.perfect8.image.model;

import com.perfect8.image.enums.ImageStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "images")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long imageId;

    @Column(name = "original_filename")
    private String originalFilename;

    @Column(name = "stored_filename", unique = true)
    private String storedFilename;

    @Column(name = "mime_type")
    private String mimeType;

    @Column(name = "image_format")
    private String imageFormat;

    @Column(name = "original_size_bytes")
    private Long originalSizeBytes;

    @Column(name = "original_width")
    private Integer originalWidth;

    @Column(name = "original_height")
    private Integer originalHeight;

    @Column(name = "original_url")
    private String originalUrl;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "small_url")
    private String smallUrl;

    @Column(name = "medium_url")
    private String mediumUrl;

    @Column(name = "large_url")
    private String largeUrl;

    @Column(name = "image_status")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ImageStatus imageStatus = ImageStatus.PENDING;

    @Column(name = "processing_time_ms")
    private Long processingTimeMs;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "alt_text")
    private String altText;

    @Column(name = "title")
    private String title;

    @Column(name = "category")
    private String category;

    @Column(name = "reference_type")
    private String referenceType;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

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
