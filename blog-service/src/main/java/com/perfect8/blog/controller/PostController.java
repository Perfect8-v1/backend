package com.perfect8.blog.controller;

import com.perfect8.blog.dto.PostDto;
import com.perfect8.blog.model.ImageReference;
import com.perfect8.blog.model.Post;
import com.perfect8.blog.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

/**
 * REST Controller for blog posts
 * 
 * FIXED (2025-11-12):
 * - Removed excerpt from toDto()
 * - Removed links from toDto()
 * - Changed imageId from String to Long
 * - Removed url, alt from toImageDto()
 * - Added displayOrder to toImageDto()
 * - 100% match with updated entities
 */
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final UserRepository userRepository;

    @GetMapping
    public Page<PostDto> listPublished(Pageable pageable) {
        return postService.listPublished(pageable).map(this::toDto);
    }

    @GetMapping("/{slug}")
    public ResponseEntity<PostDto> getBySlug(@PathVariable String slug) {
        Post post = postService.getBySlug(slug);
        return ResponseEntity.ok(toDto(post));
    }

    @PostMapping
    public ResponseEntity<PostDto> createPost(@RequestBody PostDto postDto,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = null;
        if (userDetails != null) {
            userId = userRepository.findByUsername(userDetails.getUsername())
                    .map(u -> u.getUserId())
                    .orElse(null);
        }
        Post created = postService.create(postDto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostDto> updatePost(@PathVariable Long id,
                                              @RequestBody PostDto postDto) {
        Post updated = postService.update(id, postDto);
        return ResponseEntity.ok(toDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // --- Mapping helpers ---

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
                .images(post.getImages() == null ? null : post.getImages().stream()
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
