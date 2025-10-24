package com.perfect8.image.controller;

import com.perfect8.image.dto.ImageDTO;
import com.perfect8.image.service.ImageService;
import com.perfect8.image.exception.InvalidImageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ImageController {

    private final ImageService imageService;
    private final Tika tika = new Tika();

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/bmp"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageDTO> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "altText", required = false) String altText,
            @RequestParam(value = "category", required = false) String category) {

        log.info("Received image upload request - filename: {}, size: {}",
                file.getOriginalFilename(), file.getSize());

        try {
            // Validate file size
            if (file.getSize() > MAX_FILE_SIZE) {
                throw new InvalidImageException("File size exceeds maximum allowed size of 10MB");
            }

            // Detect actual mime type using Apache Tika
            String detectedMimeType = tika.detect(file.getBytes());
            log.info("Detected MIME type: {} for file: {}", detectedMimeType, file.getOriginalFilename());

            // Validate mime type
            if (!ALLOWED_MIME_TYPES.contains(detectedMimeType)) {
                throw new InvalidImageException("Invalid file type. Allowed types: JPEG, PNG, WEBP, BMP. Detected: " + detectedMimeType);
            }

            ImageDTO savedImage = imageService.saveImage(file, altText, category);
            log.info("Successfully uploaded image with ID: {}", savedImage.getImageId());

            return ResponseEntity.status(HttpStatus.CREATED).body(savedImage);

        } catch (IOException e) {
            log.error("IO error during image upload: {}", e.getMessage());
            throw new InvalidImageException("Failed to process image: " + e.getMessage());
        }
    }

    @PostMapping(value = "/upload/multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<ImageDTO>> uploadMultipleImages(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "category", required = false) String category) {

        log.info("Received request to upload {} images", files.length);

        if (files.length > 10) {
            throw new InvalidImageException("Maximum 10 images can be uploaded at once");
        }

        List<ImageDTO> uploadedImages = Arrays.stream(files)
                .map(file -> {
                    try {
                        // Validate each file
                        if (file.getSize() > MAX_FILE_SIZE) {
                            throw new InvalidImageException("File " + file.getOriginalFilename() + " exceeds maximum size");
                        }

                        String detectedMimeType = tika.detect(file.getBytes());
                        if (!ALLOWED_MIME_TYPES.contains(detectedMimeType)) {
                            throw new InvalidImageException("Invalid file type for " + file.getOriginalFilename());
                        }

                        return imageService.saveImage(file, null, category);

                    } catch (IOException e) {
                        log.error("Failed to upload file {}: {}", file.getOriginalFilename(), e.getMessage());
                        throw new InvalidImageException("Failed to upload " + file.getOriginalFilename());
                    }
                })
                .toList();

        log.info("Successfully uploaded {} images", uploadedImages.size());
        return ResponseEntity.status(HttpStatus.CREATED).body(uploadedImages);
    }

    @GetMapping("/{imageId}")
    public ResponseEntity<ImageDTO> getImage(@PathVariable Long imageId) {
        log.info("Fetching image with ID: {}", imageId);
        ImageDTO image = imageService.getImage(imageId);
        return ResponseEntity.ok(image);
    }

    @GetMapping("/reference/{referenceType}/{referenceId}")
    public ResponseEntity<List<ImageDTO>> getImagesByReference(
            @PathVariable String referenceType,
            @PathVariable Long referenceId) {

        log.info("Fetching images for reference type: {} with ID: {}", referenceType, referenceId);
        List<ImageDTO> images = imageService.getImagesByReference(referenceType, referenceId);

        if (images.isEmpty()) {
            log.info("No images found for reference");
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(images);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<ImageDTO>> getImagesByCategory(@PathVariable String category) {
        log.info("Fetching images for category: {}", category);
        List<ImageDTO> images = imageService.getImagesByCategory(category);

        if (images.isEmpty()) {
            log.info("No images found in category: {}", category);
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(images);
    }

    @PutMapping("/{imageId}")
    public ResponseEntity<ImageDTO> updateImage(
            @PathVariable Long imageId,
            @RequestParam(value = "altText", required = false) String altText,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "status", required = false) String status) {

        log.info("Updating image {} - altText: {}, category: {}, status: {}",
                imageId, altText, category, status);

        ImageDTO updatedImage = imageService.updateImageMetadata(imageId, altText, category, status);
        return ResponseEntity.ok(updatedImage);
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> deleteImage(@PathVariable Long imageId) {
        log.info("Deleting image with ID: {}", imageId);
        imageService.deleteImage(imageId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{imageId}/attach")
    public ResponseEntity<Void> attachImageToReference(
            @PathVariable Long imageId,
            @RequestParam String referenceType,
            @RequestParam Long referenceId) {

        log.info("Attaching image {} to {} with ID {}", imageId, referenceType, referenceId);
        imageService.attachImageToReference(imageId, referenceType, referenceId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{imageId}/detach")
    public ResponseEntity<Void> detachImageFromReference(
            @PathVariable Long imageId,
            @RequestParam String referenceType,
            @RequestParam Long referenceId) {

        log.info("Detaching image {} from {} with ID {}", imageId, referenceType, referenceId);
        imageService.detachImageFromReference(imageId, referenceType, referenceId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/validate")
    public ResponseEntity<Boolean> validateImage(@RequestParam("file") MultipartFile file) {
        try {
            String detectedMimeType = tika.detect(file.getBytes());
            boolean isValid = ALLOWED_MIME_TYPES.contains(detectedMimeType) &&
                    file.getSize() <= MAX_FILE_SIZE;

            log.info("Validation result for {}: {} (type: {}, size: {})",
                    file.getOriginalFilename(), isValid, detectedMimeType, file.getSize());

            return ResponseEntity.ok(isValid);
        } catch (IOException e) {
            log.error("Error validating file: {}", e.getMessage());
            return ResponseEntity.ok(false);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Image service is running");
    }
}