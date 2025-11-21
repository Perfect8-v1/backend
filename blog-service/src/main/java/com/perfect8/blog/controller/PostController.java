package com.perfect8.blog.controller;

import com.perfect8.blog.dto.PostDto;
import com.perfect8.blog.model.ImageReference;
import com.perfect8.blog.model.Post;
import com.perfect8.blog.security.JwtTokenProvider;
import com.perfect8.blog.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

/**
 * REST Controller for blog posts
 * 
 * UPDATED (2025-11-20):
 * - Removed UserRepository dependency
 * - Gets userId from JWT token via JwtTokenProvider
 * - Central auth handled by admin-service
 * - Fixed method names to match PostService
 */
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping
    public Page<PostDto> listPublished(Pageable pageable) {
        return postService.getPublishedPosts(pageable).map(this::toDto);
    }

    @GetMapping("/{slug}")
    public ResponseEntity<PostDto> getBySlug(@PathVariable String slug) {
        Post post = postService.getPublishedPostBySlug(slug);
        return ResponseEntity.ok(toDto(post));
    }

    @PostMapping
    public ResponseEntity<PostDto> createPost(@RequestBody PostDto postDto,
                                              HttpServletRequest request) {
        Long userId = extractUserIdFromToken(request);
        Post created = postService.createPost(postDto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostDto> updatePost(@PathVariable Long id,
                                              @RequestBody PostDto postDto) {
        Post updated = postService.updatePost(id, postDto);
        return ResponseEntity.ok(toDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    // --- Helper methods ---

    private Long extractUserIdFromToken(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            return jwtTokenProvider.getUserIdFromToken(token);
        }
        return null;
    }

    private PostDto toDto(Post post) {
        return PostDto.builder()
                .postDtoId(post.getPostId())
                .title(post.getTitle())
                .content(post.getContent())
                .slug(post.getSlug())
                .published(post.isPublished())
                .createdDate(post.getCreatedDate())
                .updatedDate(post.getUpdatedDate())
                .publishedDate(post.getPublishedDate())
                .viewCount(post.getViewCount())
                .images(post.getImageReferences() == null ? null : post.getImageReferences().stream()
                        .map(this::toImageDto)
                        .collect(Collectors.toList()))
                .build();
    }

    private PostDto.ImageDto toImageDto(ImageReference img) {
        return PostDto.ImageDto.builder()
                .imageId(img.getImageId())
                .caption(img.getCaption())
                .displayOrder(img.getDisplayOrder())
                .build();
    }
}
