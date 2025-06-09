// image-service/src/main/java/com/perfect8/image/controller/ImageController.java
//

        package com.perfect8.image.controller;

import com.perfect8.image.dto.ImageDto;
import com.perfect8.image.model.Image;
import com.perfect8.image.service.ImageService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<ImageDto> uploadImage(@RequestParam("file") MultipartFile file) {
        ImageDto imageDto = imageService.uploadImage(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(imageDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ImageDto> getImageInfo(@PathVariable String id) {
        return ResponseEntity.ok(imageService.getImageDto(id));
    }

    @GetMapping("/view/{id}")
    public ResponseEntity<byte[]> viewImage(@PathVariable String id) {
        Image image = imageService.getImage(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(image.getContentType()));
        headers.setContentLength(image.getSize());

        return ResponseEntity.ok()
                .headers(headers)
                .body(image.getData());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImage(@PathVariable String id) {
        imageService.deleteImage(id);
        return ResponseEntity.noContent().build();
    }
}