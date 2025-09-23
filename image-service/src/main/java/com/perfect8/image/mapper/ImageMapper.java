package com.perfect8.image.mapper;

import com.perfect8.image.dto.ImageDto;
import com.perfect8.image.enums.ImageSize;
import com.perfect8.image.enums.ImageStatus;
import com.perfect8.image.model.Image;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mapper for converting between Image entity and ImageDto
 * Version 1.0 - Simplified for core functionality
 *
 * Only maps fields that exist in both entity and DTO
 * Follows "Less Strings, More Objects" principle
 */
@Component
public class ImageMapper {

    /**
     * Convert Image entity to ImageDto
     * Only maps fields that exist in the entity
     */
    public ImageDto toDto(Image image) {
        if (image == null) {
            return null;
        }

        ImageDto dto = ImageDto.builder()
                // Identification
                .imageId(image.getImageId())
                .originalFilename(image.getOriginalFilename())
                .storedFilename(image.getStoredFilename())

                // Build URLs map
                .urls(buildUrlsMap(image))

                // Individual URL fields for backward compatibility
                .originalUrl(image.getOriginalUrl())
                .thumbnailUrl(image.getThumbnailUrl())
                .smallUrl(image.getSmallUrl())
                .mediumUrl(image.getMediumUrl())
                .largeUrl(image.getLargeUrl())

                // Best display URL
                .displayUrl(image.getBestAvailableUrl())

                // Image metadata
                .imageFormat(image.getImageFormat())
                .mimeType(image.getMimeType())
                .originalWidth(image.getOriginalWidth())
                .originalHeight(image.getOriginalHeight())
                .originalSizeBytes(image.getOriginalSizeBytes())

                // Status - using enum directly
                .imageStatus(image.getImageStatus())
                .isAvailable(image.isAvailable())
                .allSizesGenerated(image.areAllSizesGenerated())

                // Reference info
                .referenceType(image.getReferenceType())
                .referenceId(image.getReferenceId())

                // SEO and accessibility
                .altText(image.getAltText())
                .title(image.getTitle())

                // Processing info
                .processingTimeMs(image.getProcessingTimeMs())
                .errorMessage(image.getErrorMessage())

                // Audit fields
                .createdAt(image.getCreatedAt())
                .updatedAt(image.getUpdatedAt())
                .build();

        // Set readable size (computed field)
        dto.setReadableSize(dto.getReadableSize());

        return dto;
    }

    /**
     * Convert list of Image entities to list of ImageDto
     */
    public List<ImageDto> toDtoList(List<Image> images) {
        if (images == null) {
            return new ArrayList<>();
        }

        List<ImageDto> dtos = new ArrayList<>();
        for (Image image : images) {
            ImageDto dto = toDto(image);
            if (dto != null) {
                dtos.add(dto);
            }
        }
        return dtos;
    }

    /**
     * Convert ImageDto to Image entity (for create/update operations)
     * Only sets fields that exist in the entity
     */
    public Image toEntity(ImageDto dto) {
        if (dto == null) {
            return null;
        }

        Image image = Image.builder()
                .imageId(dto.getImageId())
                .originalFilename(dto.getOriginalFilename())
                .storedFilename(dto.getStoredFilename())

                // URLs from individual fields
                .originalUrl(dto.getOriginalUrl())
                .thumbnailUrl(dto.getThumbnailUrl())
                .smallUrl(dto.getSmallUrl())
                .mediumUrl(dto.getMediumUrl())
                .largeUrl(dto.getLargeUrl())

                // Metadata
                .imageFormat(dto.getImageFormat())
                .mimeType(dto.getMimeType())
                .originalWidth(dto.getOriginalWidth())
                .originalHeight(dto.getOriginalHeight())
                .originalSizeBytes(dto.getOriginalSizeBytes())

                // Status
                .imageStatus(dto.getImageStatus())

                // Reference
                .referenceType(dto.getReferenceType())
                .referenceId(dto.getReferenceId())

                // SEO
                .altText(dto.getAltText())
                .title(dto.getTitle())

                // Processing
                .processingTimeMs(dto.getProcessingTimeMs())
                .errorMessage(dto.getErrorMessage())

                // Audit
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();

        return image;
    }

    /**
     * Create a simplified DTO for list views
     */
    public ImageDto toSimpleDto(Image image) {
        if (image == null) {
            return null;
        }

        return ImageDto.createSimple(
                image.getImageId(),
                image.getOriginalFilename(),
                image.getBestAvailableUrl(),
                image.getImageStatus()
        );
    }

    /**
     * Create thumbnail-only DTO
     */
    public ImageDto toThumbnailDto(Image image) {
        if (image == null) {
            return null;
        }

        return ImageDto.createThumbnail(
                image.getImageId(),
                image.getThumbnailUrl(),
                image.getAltText()
        );
    }

    /**
     * Build URLs map from Image entity
     */
    private Map<ImageSize, String> buildUrlsMap(Image image) {
        Map<ImageSize, String> urls = new HashMap<>();

        // Add non-null URLs to map
        if (image.getThumbnailUrl() != null) {
            urls.put(ImageSize.THUMBNAIL, image.getThumbnailUrl());
        }
        if (image.getSmallUrl() != null) {
            urls.put(ImageSize.SMALL, image.getSmallUrl());
        }
        if (image.getMediumUrl() != null) {
            urls.put(ImageSize.MEDIUM, image.getMediumUrl());
        }
        if (image.getLargeUrl() != null) {
            urls.put(ImageSize.LARGE, image.getLargeUrl());
        }

        return urls.isEmpty() ? null : urls;
    }

    /**
     * Update existing entity with DTO values
     * Used for partial updates - only updates fields that exist in entity
     */
    public void updateEntityFromDto(Image existingImage, ImageDto dto) {
        if (existingImage == null || dto == null) {
            return;
        }

        // Update only non-null values from DTO
        if (dto.getAltText() != null) {
            existingImage.setAltText(dto.getAltText());
        }
        if (dto.getTitle() != null) {
            existingImage.setTitle(dto.getTitle());
        }
        if (dto.getReferenceType() != null) {
            existingImage.setReferenceType(dto.getReferenceType());
        }
        if (dto.getReferenceId() != null) {
            existingImage.setReferenceId(dto.getReferenceId());
        }
        if (dto.getImageStatus() != null) {
            existingImage.setImageStatus(dto.getImageStatus());
        }
    }

    /**
     * Create DTO for error response
     */
    public ImageDto createErrorDto(String errorMessage, String filename) {
        return ImageDto.builder()
                .originalFilename(filename)
                .imageStatus(ImageStatus.FAILED)
                .errorMessage(errorMessage)
                .isAvailable(false)
                .build();
    }

    /**
     * Create DTO with processing status
     * Used during upload/processing
     */
    public ImageDto createProcessingDto(Long imageId, String filename) {
        return ImageDto.builder()
                .imageId(imageId)
                .originalFilename(filename)
                .imageStatus(ImageStatus.PROCESSING)
                .isAvailable(false)
                .allSizesGenerated(false)
                .build();
    }

    /**
     * Create DTO for successful upload
     * Simple version without computed fields
     */
    public ImageDto createSuccessDto(Image image) {
        // Just use the standard toDto method
        return toDto(image);
    }
}