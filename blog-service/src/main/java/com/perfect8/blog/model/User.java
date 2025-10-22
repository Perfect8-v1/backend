package com.perfect8.blog.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * User Entity - Version 1.0
 * FIXED: Removed all @Column(name=...) - Hibernate handles camelCase → snake_case
 * FIXED: id → userId (Magnum Opus)
 * FIXED: createdAt/updatedAt → createdDate/updatedDate (Magnum Opus)
 * FIXED: Added Lombok annotations
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"roles"})
@ToString(exclude = {"roles"})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;  // → DB: user_id (Magnum Opus)

    @Column(unique = true, nullable = false)
    private String username;  // → DB: username

    @Column(unique = true, nullable = false)
    private String email;  // → DB: email

    @Column(nullable = false)
    private String password;  // → DB: password

    private LocalDateTime createdDate;  // → DB: created_date (Magnum Opus)

    private LocalDateTime updatedDate;  // → DB: updated_date (Magnum Opus)

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

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