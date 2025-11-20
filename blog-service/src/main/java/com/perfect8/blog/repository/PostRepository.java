package com.perfect8.blog.repository;

import com.perfect8.blog.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * PostRepository - Blog post data access
 * 
 * FIXED: Cleaner method names with explanation
 * - findByPublishedTrue() - all published posts
 * - findByUserAndPublishedTrue() - published posts by specific user (cleaner than findByUserUserIdAndPublishedTrue)
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    
    /**
     * Find all published posts
     */
    Page<Post> findByPublishedTrue(Pageable pageable);

    /**
     * Find published posts by user
     * CLEANER: Pass User entity directly (Spring Data handles the join)
     */
    Page<Post> findByUserAndPublishedTrue(User user, Pageable pageable);

    /**
     * Alternative: Find posts by userId directly
     * This navigates: Post.user.userId (where User PK field = userId)
     */
    Page<Post> findByUserUserIdAndPublishedTrue(Long userId, Pageable pageable);

    /**
     * Find post by slug (for SEO-friendly URLs)
     */
    Optional<Post> findBySlug(String slug);
    
    /**
     * Check if slug already exists
     */
    boolean existsBySlug(String slug);
}
