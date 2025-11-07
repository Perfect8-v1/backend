// blog-service/src/main/java/com/perfect8/blog/service/PostService.java
//

        package com.perfect8.blog.service;

import com.perfect8.blog.dto.PostDto;
import com.perfect8.blog.exception.ResourceNotFoundException;
import com.perfect8.blog.model.Post;
import com.perfect8.blog.model.ImageReference;
import com.perfect8.blog.repository.PostRepository;
import com.perfect8.blog.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostService(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    public Page<PostDto> getAllPublishedPosts(Pageable pageable) {
        return postRepository.findByPublishedTrue(pageable).map(this::convertToDto);
    }

    public PostDto getPostBySlug(String slug) {
        Post post = postRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        return convertToDto(post);
    }

    public PostDto createPost(PostDto postDto, String username) {
        Post post = new Post();
        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());
        post.setSlug(generateSlug(postDto.getTitle()));
        post.setExcerpt(postDto.getExcerpt());
        post.setPublished(postDto.isPublished());

        if (postDto.isPublished()) {
            post.setPublishedDate(LocalDateTime.now());
        }

        post.setAuthor(userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found")));

        if (postDto.getLinks() != null) {
            post.setLinks(postDto.getLinks());
        }

        Post savedPost = postRepository.save(post);
        return convertToDto(savedPost);
    }

    public PostDto updatePost(Long id, PostDto postDto, String username) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        // Check if user is the author
        if (!post.getAuthor().getUsername().equals(username)) {
            throw new RuntimeException("You can only edit your own posts");
        }

        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());
        post.setExcerpt(postDto.getExcerpt());
        post.setPublished(postDto.isPublished());

        if (postDto.isPublished() && post.getPublishedDate() == null) {
            post.setPublishedDate(LocalDateTime.now());
        }

        if (postDto.getLinks() != null) {
            post.setLinks(postDto.getLinks());
        }

        Post updatedPost = postRepository.save(post);
        return convertToDto(updatedPost);
    }

    public void deletePost(Long id, String username) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        // Check if user is the author or admin
        if (!post.getAuthor().getUsername().equals(username)) {
            throw new RuntimeException("You can only delete your own posts");
        }

        postRepository.delete(post);
    }

    private PostDto convertToDto(Post post) {
        PostDto dto = new PostDto();
        dto.setPostDtoId(post.getPostId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setSlug(post.getSlug());
        dto.setExcerpt(post.getExcerpt());
        dto.setPublished(post.isPublished());
        dto.setCreatedDate(post.getCreatedDate());
        dto.setUpdatedDate(post.getUpdatedDate());
        dto.setPublishedDate(post.getPublishedDate());
        dto.setAuthorName(post.getAuthor().getUsername());
        dto.setLinks(post.getLinks());

        if (post.getImages() != null) {
            dto.setImages(post.getImages().stream().map(this::convertImageToDto).collect(Collectors.toList()));
        }

        return dto;
    }

    private PostDto.ImageReferenceDto convertImageToDto(ImageReference image) {
        PostDto.ImageReferenceDto dto = new PostDto.ImageReferenceDto();
        dto.setImageId(image.getImageId());
        dto.setImageUrl(image.getImageUrl());
        dto.setAltText(image.getAltText());
        dto.setCaption(image.getCaption());
        return dto;
    }

    private String generateSlug(String title) {
        String slug = title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();

        // Check if slug exists and make unique if necessary
        String baseSlug = slug;
        int counter = 1;
        while (postRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter++;
        }

        return slug;
    }
}