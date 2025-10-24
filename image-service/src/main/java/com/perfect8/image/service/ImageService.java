package com.perfect8.image.service;

import com.perfect8.image.dto.ImageDTO;
import com.perfect8.image.enums.ImageStatus;
import com.perfect8.image.exception.ImageNotFoundException;
import com.perfect8.image.exception.ImageProcessingException;
import com.perfect8.image.mapper.ImageMapper;
import com.perfect8.image.model.Image;
import com.perfect8.image.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {

    private final ImageRepository imageRepository;
    private final ImageProcessingService imageProcessingService;
    private final ImageMapper imageMapper;

    @Value("${app.upload.dir:uploads/images}")
    private String uploadDirectory;

    @Value("${app.base-url:http://localhost:8084}")
    private String baseUrl;

    // Main upload method
    @Transactional
    public ImageDTO saveImage(MultipartFile file, String altText, String category) {
        try {
            log.info("Starting image save process for file: {}", file.getOriginalFilename());

            // Create upload directories if they don't exist
            ensureDirectoriesExist();

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String storedFilename = generateUniqueFilename(originalFilename);

            // Detect image format
            String mimeType = file.getContentType();
            String imageFormat = determineImageFormat(originalFilename, mimeType);

            // Read image dimensions
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(file.getBytes()));
            if (bufferedImage == null) {
                throw new ImageProcessingException("Invalid image file");
            }

            // Create and save entity
            Image image = Image.builder()
                    .originalFilename(originalFilename)
                    .storedFilename(storedFilename)
                    .mimeType(mimeType)
                    .imageFormat(imageFormat)
                    .originalSizeBytes(file.getSize())
                    .altText(altText)
                    .category(category)
                    .imageStatus(ImageStatus.PENDING)
                    .build();

            // Save original file
            Path originalPath = Paths.get(uploadDirectory, "original", storedFilename);
            Files.createDirectories(originalPath.getParent());
            Files.write(originalPath, file.getBytes());
            image.setOriginalUrl(baseUrl + "/images/original/" + storedFilename);

            // Set dimensions
            image.setOriginalWidth(bufferedImage.getWidth());
            image.setOriginalHeight(bufferedImage.getHeight());

            // Save to database
            image = imageRepository.save(image);

            // Process different sizes asynchronously
            long startTime = System.currentTimeMillis();
            imageProcessingService.processImageSizes(image, file.getBytes());
            image.setImageStatus(ImageStatus.ACTIVE);
            image.setProcessingTimeMs(System.currentTimeMillis() - startTime);

            log.info("Successfully saved image with ID: {}", image.getImageId());
            image = imageRepository.save(image);

            return imageMapper.toDto(image);

        } catch (IOException e) {
            log.error("Failed to save image: {}", e.getMessage());
            throw new ImageProcessingException("Failed to save image: " + e.getMessage());
        }
    }

    // Get single image
    @Transactional(readOnly = true)
    public ImageDTO getImage(Long imageId) {
        log.info("Fetching image with ID: {}", imageId);
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ImageNotFoundException("Image not found with ID: " + imageId));

        return imageMapper.toDto(image);
    }

    // Get images by category
    @Transactional(readOnly = true)
    public List<ImageDTO> getImagesByCategory(String category) {
        log.info("Fetching images for category: {}", category);
        List<Image> images = imageRepository.findByCategoryAndImageStatusAndIsDeletedFalse(
                category, ImageStatus.ACTIVE
        );

        return images.stream()
                .map(imageMapper::toDto)
                .collect(Collectors.toList());
    }

    // Get by reference
    @Transactional(readOnly = true)
    public List<ImageDTO> getImagesByReference(String referenceType, Long referenceId) {
        log.info("Fetching images for {} with ID: {}", referenceType, referenceId);
        List<Image> images = imageRepository.findByReferenceTypeAndReferenceId(referenceType, referenceId);

        return images.stream()
                .map(imageMapper::toDto)
                .collect(Collectors.toList());
    }

    // Update with status parameter
    @Transactional
    public ImageDTO updateImageMetadata(Long imageId, String altText, String category, String status) {
        log.info("Updating image {} metadata", imageId);

        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ImageNotFoundException("Image not found with ID: " + imageId));

        if (altText != null) {
            image.setAltText(altText);
        }
        if (category != null) {
            image.setCategory(category);
        }
        if (status != null) {
            try {
                ImageStatus newStatus = ImageStatus.valueOf(status.toUpperCase());
                image.setImageStatus(newStatus);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status value: {}", status);
            }
        }

        Image updatedImage = imageRepository.save(image);
        return imageMapper.toDto(updatedImage);
    }

    // Attach to reference
    @Transactional
    public void attachImageToReference(Long imageId, String referenceType, Long referenceId) {
        log.info("Attaching image {} to {} with ID {}", imageId, referenceType, referenceId);

        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ImageNotFoundException("Image not found with ID: " + imageId));

        image.setReferenceType(referenceType);
        image.setReferenceId(referenceId);
        imageRepository.save(image);
    }

    // Detach from reference
    @Transactional
    public void detachImageFromReference(Long imageId, String referenceType, Long referenceId) {
        log.info("Detaching image {} from {} with ID {}", imageId, referenceType, referenceId);

        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ImageNotFoundException("Image not found with ID: " + imageId));

        if (referenceType.equals(image.getReferenceType()) &&
                referenceId.equals(image.getReferenceId())) {
            image.setReferenceType(null);
            image.setReferenceId(null);
            imageRepository.save(image);
        }
    }

    // Delete image
    @Transactional
    public void deleteImage(Long imageId) {
        log.info("Deleting image with ID: {}", imageId);

        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ImageNotFoundException("Image not found with ID: " + imageId));

        image.markAsDeleted();
        imageRepository.save(image);

        // Optionally delete physical files
        deletePhysicalFiles(image);
    }

    // Helper methods

    private void ensureDirectoriesExist() throws IOException {
        Path baseDir = Paths.get(uploadDirectory);
        Files.createDirectories(baseDir.resolve("original"));
        Files.createDirectories(baseDir.resolve("thumbnail"));
        Files.createDirectories(baseDir.resolve("small"));
        Files.createDirectories(baseDir.resolve("medium"));
        Files.createDirectories(baseDir.resolve("large"));
    }

    private String generateUniqueFilename(String originalFilename) {
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }
        return UUID.randomUUID().toString() + extension;
    }

    private String determineImageFormat(String filename, String mimeType) {
        if (filename != null) {
            String lower = filename.toLowerCase();
            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "JPEG";
            if (lower.endsWith(".png")) return "PNG";
            if (lower.endsWith(".webp")) return "WEBP";
            if (lower.endsWith(".bmp")) return "BMP";
        }
        if (mimeType != null) {
            if (mimeType.contains("jpeg")) return "JPEG";
            if (mimeType.contains("png")) return "PNG";
            if (mimeType.contains("webp")) return "WEBP";
            if (mimeType.contains("bmp")) return "BMP";
        }
        return "UNKNOWN";
    }

    private void deletePhysicalFiles(Image image) {
        try {
            String filename = image.getStoredFilename();
            if (filename != null) {
                Files.deleteIfExists(Paths.get(uploadDirectory, "original", filename));
                Files.deleteIfExists(Paths.get(uploadDirectory, "thumbnail", filename));
                Files.deleteIfExists(Paths.get(uploadDirectory, "small", filename));
                Files.deleteIfExists(Paths.get(uploadDirectory, "medium", filename));
                Files.deleteIfExists(Paths.get(uploadDirectory, "large", filename));
            }
        } catch (IOException e) {
            log.error("Failed to delete physical files for image {}: {}",
                    image.getImageId(), e.getMessage());
        }
    }
}