package com.perfect8.blog.service;

import com.perfect8.blog.dto.PostDto;
import com.perfect8.blog.exception.ResourceNotFoundException;
import com.perfect8.blog.model.ImageReference;
import com.perfect8.blog.model.Post;
import com.perfect8.blog.repository.PostRepository;
import com.perfect8.blog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service-layer for Post entities. Written for clarity.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    /**
     * Create a Post from a DTO. If userId is given, attach the User.
     */
    @Transactional
    public Post create(PostDto dto, Long userId) {
        // Resolve slug (use from DTO if provided and not blank; otherwise generate from title)
        String slug;
        if (dto.getSlug() == null || dto.getSlug().isBlank()) {
            slug = generateSlug(dto.getTitle());
        } else {
            slug = dto.getSlug();
        }

        Post post = Post.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .slug(slug)
                .excerpt(dto.getExcerpt())
                .published(Boolean.TRUE.equals(dto.getPublished()))
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .images(new ArrayList<>())
                .links(dto.getLinks() == null ? new ArrayList<>() : new ArrayList<>(dto.getLinks()))
                .build();

        if (userId != null) {
            var user = userRepository.findById(userId).orElseThrow(
                    () -> new ResourceNotFoundException("User not found: id=" + userId)
            );
            post.setUser(user);
        }

        if (dto.getImages() != null) {
            List<ImageReference> imgs = dto.getImages().stream()
                    .map(imgDto -> ImageReference.builder()
                            .url(imgDto.getImageUrl())
                            .alt(imgDto.getAltText())
                            .caption(imgDto.getCaption())
                            .post(post)
                            .build())
                    .collect(Collectors.toList());
            post.setImages(imgs);
        }

        var saved = postRepository.save(post);
        log.debug("Created Post id={} slug={}", saved.getPostId(), saved.getSlug());
        return saved;
    }

    @Transactional(readOnly = true)
    public Page<Post> listPublished(Pageable pageable) {
        return postRepository.findByPublishedTrue(pageable);
    }

    @Transactional(readOnly = true)
    public Post getBySlug(String slug) {
        return postRepository.findBySlug(slug).orElseThrow(
                () -> new ResourceNotFoundException("Post not found for slug: " + slug)
        );
    }

    /**
     * Update the Post with fields present in the DTO.
     */
    @Transactional
    public Post update(Long id, PostDto dto) {
        Post post = postRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Post not found for id: " + id)
        );

        // Title + slug handling
        if (dto.getTitle() != null && !dto.getTitle().equals(post.getTitle())) {
            post.setTitle(dto.getTitle());
            String newSlug;
            if (dto.getSlug() == null || dto.getSlug().isBlank()) {
                newSlug = generateSlug(dto.getTitle());
            } else {
                newSlug = dto.getSlug();
            }
            post.setSlug(newSlug);
        }

        if (dto.getContent() != null) {
            post.setContent(dto.getContent());
        }
        if (dto.getExcerpt() != null) {
            post.setExcerpt(dto.getExcerpt());
        }
        if (dto.getPublished() != null) {
            boolean nowPublished = dto.getPublished();
            post.setPublished(nowPublished);
            if (nowPublished && post.getPublishedDate() == null) {
                post.setPublishedDate(LocalDateTime.now());
            }
        }

        post.setUpdatedDate(LocalDateTime.now());

        // Replace images if provided
        if (dto.getImages() != null) {
            post.getImages().clear();
            List<ImageReference> imgs = dto.getImages().stream()
                    .map(imgDto -> ImageReference.builder()
                            .url(imgDto.getImageUrl())
                            .alt(imgDto.getAltText())
                            .caption(imgDto.getCaption())
                            .post(post)
                            .build())
                    .collect(Collectors.toList());
            post.getImages().addAll(imgs);
        }

        // Replace links if provided
        if (dto.getLinks() != null) {
            post.getLinks().clear();
            post.getLinks().addAll(dto.getLinks());
        }

        return postRepository.save(post);
    }

    @Transactional
    public void delete(Long id) {
        if (!postRepository.existsById(id)) {
            throw new ResourceNotFoundException("Post not found: id=" + id);
        }
        postRepository.deleteById(id);
    }

    /**
     * Create a URL-friendly slug from a title and ensure uniqueness.
     */
    @Transactional(readOnly = true)
    public String generateSlug(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        String slug = title.toLowerCase()
                .replaceAll("[^a-z0-9\s-]", "")
                .replaceAll("\s+", "-")
                .replaceAll("-+", "-")
                .trim();

        String base = slug;
        int counter = 1;
        while (postRepository.existsBySlug(slug)) {
            slug = base + "-" + counter++;
        }
        return slug;
    }
}
