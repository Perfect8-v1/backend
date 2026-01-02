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
    List<Image> findByCategoryAndImageStatusAndIsDeletedFalse(String category, ImageStatus imageStatus);

    // Find all active images
    List<Image> findByImageStatusAndIsDeletedFalse(ImageStatus imageStatus);

    // Find by stored filename
    Optional<Image> findByStoredFilename(String storedFilename);

    // Find active images by reference
    @Query("SELECT i FROM Image i WHERE i.referenceType = :referenceType AND i.referenceId = :referenceId " +
            "AND i.imageStatus = :imageStatus AND i.isDeleted = false")
    List<Image> findActiveImagesByReference(
            @Param("referenceType") String referenceType,
            @Param("referenceId") Long referenceId,
            @Param("imageStatus") ImageStatus imageStatus
    );

    // Count images by category
    Long countByCategoryAndIsDeletedFalse(String category);

    // Find images needing processing
    List<Image> findByImageStatus(ImageStatus imageStatus);

    // Find orphaned images (no reference)
    @Query("SELECT i FROM Image i WHERE i.referenceType IS NULL AND i.referenceId IS NULL " +
            "AND i.createdDate < :createdBefore AND i.isDeleted = false")
    List<Image> findOrphanedImages(@Param("createdBefore") LocalDateTime createdBefore);

    // Find by multiple categories
    List<Image> findByCategoryInAndImageStatusAndIsDeletedFalse(
            List<String> categories,
            ImageStatus imageStatus
    );

    // Check if image exists for reference
    boolean existsByReferenceTypeAndReferenceIdAndIsDeletedFalse(
            String referenceType,
            Long referenceId
    );
}