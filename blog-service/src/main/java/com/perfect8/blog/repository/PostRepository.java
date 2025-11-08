package com.perfect8.blog.repository;

import com.perfect8.blog.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByPublishedTrue(Pageable pageable);

    // Navigates Post.user.userId (User PK field = userId)
    Page<Post> findByUserUserIdAndPublishedTrue(Long userId, Pageable pageable);

    Optional<Post> findBySlug(String slug);
    boolean existsBySlug(String slug);
}
