package com.perfect8.blog.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Image Reference Entity - Version 1.0
 * FIXED: Removed all @Column(name=...) - Hibernate handles camelCase → snake_case
 * FIXED: id → imageReferenceId (Magnum Opus)
 * FIXED: createdAt → createdDate (Magnum Opus)
 * FIXED: Added Lombok annotations
 */
@Entity
@Table(name = "image_references")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"post"})
@ToString(exclude = {"post"})
public class ImageReference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageReferenceId;  // → DB: image_reference_id (Magnum Opus)

    @Column(nullable = false)
    private String imageId;  // → DB: image_id

    private String imageUrl;  // → DB: image_url

    private String altText;  // → DB: alt_text

    private String caption;  // → DB: caption

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    private LocalDateTime createdDate;  // → DB: created_date (Magnum Opus)

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
    }
}