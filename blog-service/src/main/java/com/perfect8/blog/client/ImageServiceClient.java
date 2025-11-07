// blog-service/src/main/java/com/perfect8/blog/client/ImageServiceClient.java

        package com.perfect8.blog.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "image-service", url = "${image.service.url}")
public interface ImageServiceClient {

    @PostMapping(value = "/api/images/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ImageResponse uploadImage(@RequestPart("file") MultipartFile file);

    @GetMapping("/api/images/{customerEmailDTOId}")
    ImageResponse getImage(@PathVariable String id);

    @DeleteMapping("/api/images/{customerEmailDTOId}")
    void deleteImage(@PathVariable String id);

    class ImageResponse {
        private String id;
        private String url;
        private String contentType;
        private Long size;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }

        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }

        public Long getSize() { return size; }
        public void setSize(Long size) { this.size = size; }
    }
}