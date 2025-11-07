// blog-service/src/main/java/com/perfect8/blog/dto/PostDto.java

        package com.perfect8.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

public class PostDto {

    private Long postDtoId;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be less than 255 characters")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    private String slug;
    private String excerpt;
    private boolean published;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private LocalDateTime publishedDate;
    private String authorName;
    private List<ImageReferenceDto> images;
    private List<String> links;

    // Getters and setters
    public Long getPostDtoId() { return postDtoId; }
    public void setPostDtoId(Long postDtoId) { this.postDtoId = postDtoId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getExcerpt() { return excerpt; }
    public void setExcerpt(String excerpt) { this.excerpt = excerpt; }

    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }

    public LocalDateTime getPublishedDate() { return publishedDate; }
    public void setPublishedDate(LocalDateTime publishedDate) { this.publishedDate = publishedDate; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public List<ImageReferenceDto> getImages() { return images; }
    public void setImages(List<ImageReferenceDto> images) { this.images = images; }

    public List<String> getLinks() { return links; }
    public void setLinks(List<String> links) { this.links = links; }

    public static class ImageReferenceDto {
        private String imageId;
        private String imageUrl;
        private String altText;
        private String caption;

        // Getters and setters
        public String getImageId() { return imageId; }
        public void setImageId(String imageId) { this.imageId = imageId; }

        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

        public String getAltText() { return altText; }
        public void setAltText(String altText) { this.altText = altText; }

        public String getCaption() { return caption; }
        public void setCaption(String caption) { this.caption = caption; }
    }
}