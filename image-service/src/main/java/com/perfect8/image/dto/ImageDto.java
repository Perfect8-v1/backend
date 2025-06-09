// image-service/src/main/java/com/perfect8/image/dto/ImageDto.java
//

        package com.perfect8.image.dto;

import java.time.LocalDateTime;

public class ImageDto {
    private String id;
    private String filename;
    private String contentType;
    private Long size;
    private String url;
    private LocalDateTime createdAt;

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public Long getSize() { return size; }
    public void setSize(Long size) { this.size = size; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}