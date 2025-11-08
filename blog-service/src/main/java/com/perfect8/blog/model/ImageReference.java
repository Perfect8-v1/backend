package com.perfect8.blog.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    @Column(name = "image_id")
    private String imageId; // optional client-side id/reference

    @Column(nullable = false)
    private String url;

    @Column
    private String alt;

    @Column
    private String caption;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

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
