package com.perfect8.blog.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Post entity for blog posts
 *
 * FIXED (2025-11-12):
 * - Removed excerpt (v1.0 - not needed)
 * - Removed links collection (v1.0 - not needed)
 * - 100% match with blog-CREATE-TABLE.sql
 * - Magnum Opus compliance
 */
@Entity
@Table(name = "posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(unique = true)
    private String slug;

    @Column(nullable = false)
    @Builder.Default
    private boolean published = false;  // Hibernate maps: published → published column

    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private LocalDateTime publishedDate;

    @Builder.Default
    private Integer viewCount = 0;

    // Relations
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ImageReference> images = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }
}
