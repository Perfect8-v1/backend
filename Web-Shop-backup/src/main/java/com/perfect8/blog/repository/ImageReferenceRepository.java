// blog-service/src/main/java/com/perfect8/blog/repository/ImageReferenceRepository.java

        package com.perfect8.blog.repository;

import com.perfect8.blog.model.ImageReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ImageReferenceRepository extends JpaRepository<ImageReference, Long> {
    List<ImageReference> findByPostId(Long postId);
    void deleteByImageId(String imageId);
}