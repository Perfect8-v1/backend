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
 * Lombok-clean DTO for Post.
 * If some fields don't exist in your original DTO, feel free to remove them;
 * this version is designed to match the current service methods.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostDto {

    private Long postDtoId;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be less than 255 characters")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    /** Optional slug; will be generated from title if null/blank */
    private String slug;

    private String excerpt;

    /** Nullable to allow tri-state in updates (leave unchanged when null) */
    private Boolean published;

    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private LocalDateTime publishedDate;

    /** Optional simple link list */
    @Builder.Default
    private List<String> links = new ArrayList<>();

    /** Optional images */
    @Builder.Default
    private List<ImageDto> images = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ImageDto {
        private String imageId;   // client-side id if any
        private String imageUrl;  // actual URL used to render
        private String altText;
        private String caption;
    }
}
