// blog-service/src/main/java/com/perfect8/blog/service/PostService.java

package com.perfect8.blog.service;

import com.perfect8.blog.dto.PostDto;
import com.perfect8.blog.exception.ResourceNotFoundException;
import com.perfect8.blog.model.Post;
import com.perfect8.blog.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PostService {

    private final PostRepository postRepository;

    // ==================== Public Methods (Read) ====================

    /**
     * Get all published posts (paginated)
     */
    public Page<Post> getPublishedPosts(Pageable pageable) {
        return postRepository.findByIsPublishedTrue(pageable);
    }

    /**
     * Get post by ID
     */
    public Post getPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));
    }

    /**
     * Get post by slug
     */
    public Post getPostBySlug(String slug) {
        return postRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with slug: " + slug));
    }

    /**
     * Get published post by slug (increments view count)
     */
    public Post getPublishedPostBySlug(String slug) {
        Post post = postRepository.findBySlugAndIsPublishedTrue(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Published post not found with slug: " + slug));

        // Increment view count
        post.incrementViewCount();
        postRepository.save(post);

        return post;
    }

    /**
     * Search posts by keyword
     */
    public Page<Post> searchPosts(String keyword, Pageable pageable) {
        return postRepository.searchByTitleOrContent(keyword, pageable);
    }

    // ==================== Admin Methods (CRUD) ====================

    /**
     * Get all posts (including drafts) - Admin only
     */
    public Page<Post> getAllPosts(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    /**
     * Get all drafts - Admin only
     */
    public Page<Post> getDrafts(Pageable pageable) {
        return postRepository.findByIsPublishedFalse(pageable);
    }

    /**
     * Create new post
     */
    public Post createPost(String title, String content) {
        log.info("Creating new post: {}", title);

        // Generate unique slug
        String slug = generateUniqueSlug(title);

        Post post = Post.builder()
                .title(title)
                .content(content)
                .slug(slug)
                .isPublished(false)
                .viewCount(0)
                .build();

        Post savedPost = postRepository.save(post);
        log.info("Post created with id: {}", savedPost.getPostId());

        return savedPost;
    }

    /**
     * Create post from DTO
     */
    public Post createPost(PostDto postDto) {
        return createPost(postDto.getTitle(), postDto.getContent());
    }

    /**
     * Update post
     */
    public Post updatePost(Long postId, String title, String content) {
        Post post = getPostById(postId);

        if (title != null && !title.equals(post.getTitle())) {
            post.setTitle(title);
            // Update slug if title changed
            post.setSlug(generateUniqueSlug(title));
        }

        if (content != null) {
            post.setContent(content);
        }

        Post updatedPost = postRepository.save(post);
        log.info("Post updated: {}", postId);

        return updatedPost;
    }

    /**
     * Update post from DTO
     */
    public Post updatePost(Long postId, PostDto postDto) {
        return updatePost(postId, postDto.getTitle(), postDto.getContent());
    }

    /**
     * Delete post
     */
    public void deletePost(Long postId) {
        Post post = getPostById(postId);
        postRepository.delete(post);
        log.info("Post deleted: {}", postId);
    }

    /**
     * Publish post
     */
    public Post publishPost(Long postId) {
        Post post = getPostById(postId);
        post.publish();
        Post publishedPost = postRepository.save(post);
        log.info("Post published: {}", postId);
        return publishedPost;
    }

    /**
     * Unpublish post
     */
    public Post unpublishPost(Long postId) {
        Post post = getPostById(postId);
        post.unpublish();
        Post unpublishedPost = postRepository.save(post);
        log.info("Post unpublished: {}", postId);
        return unpublishedPost;
    }

    // ==================== Helper Methods ====================

    /**
     * Generate unique slug from title
     */
    private String generateUniqueSlug(String title) {
        String baseSlug = title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();

        String slug = baseSlug;
        int counter = 1;

        while (postRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }

        return slug;
    }

    /**
     * Count all published posts
     */
    public long countPublishedPosts() {
        return postRepository.countByIsPublishedTrue();
    }
}