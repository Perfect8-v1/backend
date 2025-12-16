package com.perfect8.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for Post entity
 * 
 * FIXED (2025-11-12):
 * - Removed excerpt (field removed from Post.java)
 * - Removed links (field removed from Post.java)
 * - Updated ImageDto to match ImageReference.java:
 *   - imageId: String → Long
 *   - Removed imageUrl
 *   - Removed altText
 *   - Added displayOrder
 * - 100% match with updated entities
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostDto {

    private Long postId;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be less than 255 characters")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    /** Optional slug; will be generated from title if null/blank */
    private String slug;

    /** Nullable to allow tri-state in updates (leave unchanged when null) */
    private Boolean published;

    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private LocalDateTime publishedDate;

    private Integer viewCount;

    /** Optional images */
    @Builder.Default
    private List<ImageDto> images = new ArrayList<>();

    /**
     * ImageDto matching ImageReference.java structure
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ImageDto {
        private Long imageId;       // FK to image-service
        private String caption;
        @Builder.Default
        private Integer displayOrder = 0;
    }
}
