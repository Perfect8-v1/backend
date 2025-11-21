package com.perfect8.blog.repository;

import com.perfect8.blog.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * PostRepository - Blog post data access
 * 
 * Uses authorId (Long) to reference users in admin-service
 * No direct JPA relation to User entity
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    
    /**
     * Find all published posts (paginated)
     */
    Page<Post> findByIsPublishedTrue(Pageable pageable);

    /**
     * Find all published posts (list)
     */
    List<Post> findByIsPublishedTrue();

    /**
     * Find published posts by author
     */
    Page<Post> findByAuthorIdAndIsPublishedTrue(Long authorId, Pageable pageable);

    /**
     * Find all posts by author (including drafts)
     */
    Page<Post> findByAuthorId(Long authorId, Pageable pageable);

    /**
     * Find all posts by author (list)
     */
    List<Post> findByAuthorId(Long authorId);

    /**
     * Find post by slug (for SEO-friendly URLs)
     */
    Optional<Post> findBySlug(String slug);

    /**
     * Find published post by slug
     */
    Optional<Post> findBySlugAndIsPublishedTrue(String slug);
    
    /**
     * Check if slug already exists
     */
    boolean existsBySlug(String slug);

    /**
     * Find all drafts (unpublished posts)
     */
    Page<Post> findByIsPublishedFalse(Pageable pageable);

    /**
     * Find drafts by author
     */
    List<Post> findByAuthorIdAndIsPublishedFalse(Long authorId);

    /**
     * Count posts by author
     */
    long countByAuthorId(Long authorId);

    /**
     * Count published posts
     */
    long countByIsPublishedTrue();

    /**
     * Search posts by title (case-insensitive)
     */
    @Query("SELECT p FROM Post p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) AND p.isPublished = true")
    Page<Post> searchByTitle(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Search posts by title or content
     */
    @Query("SELECT p FROM Post p WHERE (LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND p.isPublished = true")
    Page<Post> searchByTitleOrContent(@Param("keyword") String keyword, Pageable pageable);
}
