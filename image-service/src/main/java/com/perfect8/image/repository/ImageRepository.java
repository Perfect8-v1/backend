package com.perfect8.image.repository;

import com.perfect8.image.enums.ImageStatus;
import com.perfect8.image.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    // Find by reference
    List<Image> findByReferenceTypeAndReferenceId(String referenceType, Long referenceId);

    // Find by category
    List<Image> findByCategoryAndImageStatusAndIsDeletedFalse(String category, ImageStatus status);

    // Find all active images
    List<Image> findByImageStatusAndIsDeletedFalse(ImageStatus status);

    // Find by stored filename
    Optional<Image> findByStoredFilename(String storedFilename);

    // Find active images by reference
    @Query("SELECT i FROM Image i WHERE i.referenceType = :type AND i.referenceId = :customerEmailDTOId " +
            "AND i.imageStatus = :status AND i.isDeleted = false")
    List<Image> findActiveImagesByReference(
            @Param("type") String referenceType,
            @Param("id") Long referenceId,
            @Param("status") ImageStatus status
    );

    // Count images by category
    Long countByCategoryAndIsDeletedFalse(String category);

    // Find images needing processing
    List<Image> findByImageStatus(ImageStatus status);

    // Find orphaned images (no reference)
    @Query("SELECT i FROM Image i WHERE i.referenceType IS NULL AND i.referenceId IS NULL " +
            "AND i.createdDate < :before AND i.isDeleted = false")
    List<Image> findOrphanedImages(@Param("before") LocalDateTime before);

    // Find by multiple categories
    List<Image> findByCategoryInAndImageStatusAndIsDeletedFalse(
            List<String> categories,
            ImageStatus status
    );

    // Check if image exists for reference
    boolean existsByReferenceTypeAndReferenceIdAndIsDeletedFalse(
            String referenceType,
            Long referenceId
    );
}