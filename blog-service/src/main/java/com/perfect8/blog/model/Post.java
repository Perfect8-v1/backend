package com.perfect8.blog.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(unique = true, length = 255)
    private String slug;

    // Reference to User in admin-service (not a JPA relation)
    @Column(name = "author_id")
    private Long authorId;

    @Column(name = "published_date")
    private LocalDateTime publishedDate;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "is_published", nullable = false)
    @Builder.Default
    private boolean isPublished = false;

    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private int viewCount = 0;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ImageReference> imageReferences = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        if (slug == null || slug.isEmpty()) {
            slug = generateSlug(title);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }

    /**
     * Generate URL-friendly slug from title
     */
    private String generateSlug(String title) {
        if (title == null) return null;
        return title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
    }

    /**
     * Publish the post
     */
    public void publish() {
        this.isPublished = true;
        this.publishedDate = LocalDateTime.now();
    }

    /**
     * Unpublish the post
     */
    public void unpublish() {
        this.isPublished = false;
        this.publishedDate = null;
    }

    /**
     * Increment view count
     */
    public void incrementViewCount() {
        this.viewCount++;
    }

    /**
     * Add image reference
     */
    public void addImageReference(ImageReference imageReference) {
        imageReferences.add(imageReference);
        imageReference.setPost(this);
    }

    /**
     * Remove image reference
     */
    public void removeImageReference(ImageReference imageReference) {
        imageReferences.remove(imageReference);
        imageReference.setPost(null);
    }
}
