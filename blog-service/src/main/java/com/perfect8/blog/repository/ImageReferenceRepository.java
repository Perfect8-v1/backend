package com.perfect8.blog.repository;

import com.perfect8.blog.model.ImageReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository for ImageReference entity
 * 
 * FIXED (2025-11-12):
 * - findByPostId â†' findByPost_PostId (explicit path to Post.postId)
 * - deleteByImageId â†' takes Long (imageId is Long, not String)
 */
@Repository
public interface ImageReferenceRepository extends JpaRepository<ImageReference, Long> {
    
    /**
     * Find all image references for a specific post
     * Uses explicit property path: ImageReference.post.postId
     */
    List<ImageReference> findByPost_PostId(Long postId);
    
    /**
     * Delete image references by imageId
     * imageId is Long (FK to image-service Image.imageId)
     */
    void deleteByImageId(Long imageId);
}
