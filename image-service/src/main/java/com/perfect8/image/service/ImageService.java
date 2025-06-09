// image-service/src/main/java/com/perfect8/image/service/ImageService.java
//

        package com.perfect8.image.service;

import com.perfect8.image.config.StorageConfig;
import com.perfect8.image.dto.ImageDto;
import com.perfect8.image.exception.StorageException;
import com.perfect8.image.model.Image;
import com.perfect8.image.repository.ImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Arrays;

@Service
@Transactional
public class ImageService {

    private final ImageRepository imageRepository;
    private final StorageConfig storageConfig;

    public ImageService(ImageRepository imageRepository, StorageConfig storageConfig) {
        this.imageRepository = imageRepository;
        this.storageConfig = storageConfig;
    }

    public ImageDto uploadImage(MultipartFile file) {
        validateFile(file);

        try {
            Image image = new Image();
            image.setFilename(file.getOriginalFilename());
            image.setContentType(file.getContentType());
            image.setSize(file.getSize());
            image.setData(file.getBytes());

            Image savedImage = imageRepository.save(image);
            return convertToDto(savedImage);
        } catch (IOException e) {
            throw new StorageException("Failed to store file", e);
        }
    }

    public Image getImage(String id) {
        return imageRepository.findById(id)
                .orElseThrow(() -> new StorageException("Image not found"));
    }

    public ImageDto getImageDto(String id) {
        return convertToDto(getImage(id));
    }

    public void deleteImage(String id) {
        imageRepository.deleteById(id);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new StorageException("Failed to store empty file");
        }

        if (file.getSize() > storageConfig.getMaxFileSize()) {
            throw new StorageException("File size exceeds maximum allowed size");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || filename.isEmpty()) {
            throw new StorageException("Invalid filename");
        }

        String extension = getFileExtension(filename);
        if (!isAllowedExtension(extension)) {
            throw new StorageException("File type not allowed");
        }
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return filename.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }

    private boolean isAllowedExtension(String extension) {
        return Arrays.asList(storageConfig.getAllowedExtensions()).contains(extension);
    }

    private ImageDto convertToDto(Image image) {
        ImageDto dto = new ImageDto();
        dto.setId(image.getId());
        dto.setFilename(image.getFilename());
        dto.setContentType(image.getContentType());
        dto.setSize(image.getSize());
        dto.setUrl("/api/images/view/" + image.getId());
        dto.setCreatedAt(image.getCreatedAt());
        return dto;
    }
}