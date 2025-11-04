package com.perfect8.blog.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Post Entity - Version 1.0
 * FIXED: Removed all @Column(name=...) - Hibernate handles camelCase → snake_case
 * FIXED: id → postId (Magnum Opus)
 * FIXED: createdAt/updatedAt → createdDate/updatedDate (Magnum Opus)
 * FIXED: Added Lombok annotations
 */
@Entity
@Table(name = "posts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"author", "images"})
@ToString(exclude = {"author", "images"})
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;  // → DB: post_id (Magnum Opus)

    @Column(nullable = false)
    private String title;  // → DB: title

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;  // → DB: content

    @Column(unique = true)
    private String slug;  // → DB: slug

    private String excerpt;  // → DB: excerpt

    @Builder.Default
    private boolean published = false;  // → DB: published

    private LocalDateTime createdDate;  // → DB: created_date (Magnum Opus)

    private LocalDateTime updatedDate;  // → DB: updated_date (Magnum Opus)

    private LocalDateTime publishedAt;  // → DB: published_at

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

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