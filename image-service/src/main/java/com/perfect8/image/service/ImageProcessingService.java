package com.perfect8.image.service;

import com.perfect8.image.enums.ImageSize;
import com.perfect8.image.enums.ImageStatus;
import com.perfect8.image.exception.ImageProcessingException;
import com.perfect8.image.model.Image;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageProcessingService {

    @Value("${app.upload.dir:uploads/images}")
    private String uploadDirectory;

    @Value("${app.base-url: https://p8.rantila.com}")
    private String baseUrl;

    @Value("${image.output.quality:0.90}")
    private Double outputQuality;

    /**
     * Process image and generate all sizes
     */
    public void processImageSizes(Image image, byte[] imageData) {
        log.info("Starting image processing for: {}", image.getOriginalFilename());

        try {
            // Ensure directories exist
            ensureDirectoriesExist();

            // Save original file
            Path originalPath = Paths.get(uploadDirectory, "original", image.getStoredFilename());
            Files.write(originalPath, imageData);

            // Read as BufferedImage for processing
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageData));

            // Generate each size
            for (ImageSize size : ImageSize.values()) {
                generateSize(image, bufferedImage, size);
            }

            log.info("Successfully processed all sizes for image: {}", image.getImageId());

        } catch (IOException e) {
            log.error("Failed to process image sizes: {}", e.getMessage());
            throw new ImageProcessingException("Failed to process image: " + e.getMessage());
        }
    }

    /**
     * Generate a specific size
     */
    private void generateSize(Image image, BufferedImage originalImage, ImageSize size) throws IOException {
        String outputDir = size.getDirectory();
        Path outputPath = Paths.get(uploadDirectory, outputDir, image.getStoredFilename());
        Files.createDirectories(outputPath.getParent());

        log.debug("Generating {} size: {}x{}", size.name(), size.getWidth(), size.getHeight());

        // Use Thumbnailator for resize
        if (size == ImageSize.THUMBNAIL) {
            // Center crop for thumbnails
            Thumbnails.of(originalImage)
                    .size(size.getWidth(), size.getHeight())
                    .crop(Positions.CENTER)
                    .outputQuality(outputQuality)
                    .toFile(outputPath.toFile());
        } else {
            // Keep aspect ratio for other sizes
            Thumbnails.of(originalImage)
                    .size(size.getWidth(), size.getHeight())
                    .keepAspectRatio(true)
                    .outputQuality(outputQuality)
                    .toFile(outputPath.toFile());
        }

        // Update image URLs
        String url = baseUrl + "/images/" + outputDir + "/" + image.getStoredFilename();
        switch (size) {
            case THUMBNAIL:
                image.setThumbnailUrl(url);
                break;
            case SMALL:
                image.setSmallUrl(url);
                break;
            case MEDIUM:
                image.setMediumUrl(url);
                break;
            case LARGE:
                image.setLargeUrl(url);
                break;
        }

        log.debug("Generated {} size successfully", size.name());
    }

    /**
     * Ensure all required directories exist
     */
    private void ensureDirectoriesExist() throws IOException {
        String[] dirs = {"products", "original", "thumbnail", "small", "medium", "large"};
        for (String dir : dirs) {
            Path path = Paths.get(uploadDirectory, dir);
            Files.createDirectories(path);
        }
    }

    /**
     * Validate image file
     */
    public boolean validateImage(byte[] imageData) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));
            return image != null && image.getWidth() > 0 && image.getHeight() > 0;
        } catch (IOException e) {
            log.error("Image validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get image dimensions
     */
    public int[] getImageDimensions(byte[] imageData) throws IOException {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));
        if (image != null) {
            return new int[]{image.getWidth(), image.getHeight()};
        }
        throw new IOException("Could not read image dimensions");
    }
}