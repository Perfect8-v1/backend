// blog-service/src/test/java/com/perfect8/blog/service/PostServiceTest.java
//

        package com.perfect8.blog.service;

import com.perfect8.blog.dto.PostDto;
import com.perfect8.blog.model.Post;
import com.perfect8.blog.model.User;
import com.perfect8.blog.repository.PostRepository;
import com.perfect8.blog.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PostService postService;

    private User testUser;
    private Post testPost;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@test.com");

        testPost = new Post();
        testPost.setId(1L);
        testPost.setTitle("Test Post");
        testPost.setContent("Test Content");
        testPost.setAuthor(testUser);
    }

    @Test
    void createPost_Success() {
        PostDto postDto = new PostDto();
        postDto.setTitle("New Post");
        postDto.setContent("New Content");
        postDto.setPublished(false);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(postRepository.save(any(Post.class))).thenReturn(testPost);

        PostDto result = postService.createPost(postDto, "testuser");

        assertNotNull(result);
        assertEquals("Test Post", result.getTitle());
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    void getPostBySlug_Success() {
        when(postRepository.findBySlug("test-post")).thenReturn(Optional.of(testPost));

        PostDto result = postService.getPostBySlug("test-post");

        assertNotNull(result);
        assertEquals("Test Post", result.getTitle());
    }
}