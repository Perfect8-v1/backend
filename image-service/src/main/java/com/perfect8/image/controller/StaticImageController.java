package com.perfect8.image.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * Static Image Controller - Serves image files from the upload directory
 *
 * Endpoints:
 * - GET /images/{size}/{filename} - Get image by size (original, thumbnail, small, medium, large)
 * - GET /images/products/{filename} - Get product image (alias for original)
 *
 * Features:
 * - Proper cache headers for CDN compatibility
 * - Content-type detection
 * - Logging for analytics
 * - Graceful 404 handling
 */
@RestController
@RequestMapping("/images")
@Slf4j
public class StaticImageController {

    @Value("${app.upload.dir:uploads/images}")
    private String uploadDirectory;

    // Cache images for 7 days (CDN-friendly)
    private static final CacheControl CACHE_CONTROL = CacheControl.maxAge(7, TimeUnit.DAYS)
            .cachePublic();

    /**
     * Serve image by size category
     * GET /images/{size}/{filename}
     *
     * @param size     Image size: original, thumbnail, small, medium, large
     * @param filename The image filename
     */
    @GetMapping("/{size}/{filename:.+}")
    public ResponseEntity<Resource> getImageBySize(
            @PathVariable String size,
            @PathVariable String filename) {

        log.debug("Image request: size={}, filename={}", size, filename);

        // Validate size parameter
        if (!isValidSize(size)) {
            log.warn("Invalid size requested: {}", size);
            return ResponseEntity.badRequest().build();
        }

        Path imagePath = Paths.get(uploadDirectory, size, filename);
        return serveImage(imagePath, filename);
    }

    /**
     * Serve product images (convenience endpoint)
     * GET /images/products/{filename}
     *
     * Maps to original size for backward compatibility
     */
    @GetMapping("/products/{filename:.+}")
    public ResponseEntity<Resource> getProductImage(@PathVariable String filename) {
        log.debug("Product image request: filename={}", filename);

        // Try products subdirectory first, then fall back to original
        Path productsPath = Paths.get(uploadDirectory, "products", filename);
        if (Files.exists(productsPath)) {
            return serveImage(productsPath, filename);
        }

        // Fallback to original directory
        Path originalPath = Paths.get(uploadDirectory, "original", filename);
        return serveImage(originalPath, filename);
    }

    /**
     * Core method to serve an image file
     */
    private ResponseEntity<Resource> serveImage(Path imagePath, String filename) {
        try {
            if (!Files.exists(imagePath)) {
                log.info("Image not found: {}", imagePath);
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(imagePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                log.warn("Image not readable: {}", imagePath);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            // Detect content type
            String contentType = detectContentType(imagePath, filename);

            log.debug("Serving image: {} ({})", imagePath, contentType);

            return ResponseEntity.ok()
                    .cacheControl(CACHE_CONTROL)
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            log.error("Malformed URL for image: {}", imagePath, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Validate size parameter
     */
    private boolean isValidSize(String size) {
        return size != null && (
                size.equals("original") ||
                size.equals("thumbnail") ||
                size.equals("small") ||
                size.equals("medium") ||
                size.equals("large") ||
                size.equals("products")
        );
    }

    /**
     * Detect content type from file extension or probe
     */
    private String detectContentType(Path path, String filename) {
        // Try to probe content type
        try {
            String probed = Files.probeContentType(path);
            if (probed != null) {
                return probed;
            }
        } catch (IOException e) {
            log.debug("Could not probe content type for: {}", path);
        }

        // Fallback to extension-based detection
        String lower = filename.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lower.endsWith(".png")) {
            return "image/png";
        } else if (lower.endsWith(".webp")) {
            return "image/webp";
        } else if (lower.endsWith(".gif")) {
            return "image/gif";
        } else if (lower.endsWith(".bmp")) {
            return "image/bmp";
        }

        return "application/octet-stream";
    }
}
