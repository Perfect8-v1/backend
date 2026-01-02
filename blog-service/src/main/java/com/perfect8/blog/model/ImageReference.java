package com.perfect8.blog.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ImageReference entity for linking posts with images
 *
 * FIXED (2025-11-12):
 * - imageId: BIGINT → Long (correct Java type)
 * - Removed url (not in SQL)
 * - Removed alt (not in SQL)
 * - Removed updatedDate (not in SQL)
 * - Added displayOrder (exists in SQL)
 * - Removed @PreUpdate (no updatedDate field)
 * - 100% match with blog-CREATE-TABLE.sql
 */
@Entity
@Table(name = "image_references")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageReference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageReferenceId;

    @Column(nullable = false)
    private Long imageId;  // FK to image-service

    @Column(length = 500)
    private String caption;

    @Column(nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    private LocalDateTime createdDate;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
    }
}
