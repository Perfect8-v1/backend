package com.perfect8.feign;

import com.perfect8.dto.BlogPostDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "blog-service", url = "http://localhost:8080")
public interface BlogServiceClient {

    @GetMapping("/api/posts")
    ResponseEntity<List<BlogPostDto>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    );

    @GetMapping("/api/posts/{customerEmailDTOId}")
    ResponseEntity<BlogPostDto> getPost(@PathVariable Long id);

    @GetMapping("/api/posts/published")
    ResponseEntity<List<BlogPostDto>> getPublishedPosts();
}
