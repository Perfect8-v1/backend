package com.perfect8.blog.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Post entity for blog posts
 *
 * FIXED:
 * - @JoinColumn(name = "user_id") already correct - matches SQL
 * - boolean published field (Hibernate maps to "published" column)
 * - Removed explicit column names where Hibernate defaults are correct
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

    private String excerpt;

    @Column(nullable = false)
    @Builder.Default
    private boolean published = false;  // Hibernate maps: published → published column

    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private LocalDateTime publishedDate;

    @Builder.Default
    private Integer viewCount = 0;

    // Relationer
    @ManyToOne
    @JoinColumn(name = "user_id")  // CORRECT: Matches SQL column user_id
    private User user;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ImageReference> images = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "post_links", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "url")
    @Builder.Default
    private List<String> links = new ArrayList<>();

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