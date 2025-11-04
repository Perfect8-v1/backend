package com.perfect8.admin.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Admin User Entity - Version 1.0
 * FIXED: Removed all @Column(name=...) - Hibernate handles camelCase → snake_case
 * FIXED: id → adminUserId (Magnum Opus)
 * FIXED: createdAt/updatedAt → createdDate/updatedDate (Magnum Opus)
 * FIXED: Added Lombok annotations
 */
@Entity
@Table(name = "admin_users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long adminUserId;  // → DB: admin_user_id (Magnum Opus)

    @Column(unique = true, nullable = false)
    private String username;  // → DB: username

    @Column(unique = true, nullable = false)
    private String email;  // → DB: email

    @Column(nullable = false)
    private String password;  // → DB: password

    private String firstName;  // → DB: first_name

    private String lastName;  // → DB: last_name

    @Enumerated(EnumType.STRING)
    private Role role;  // → DB: role

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;  // → DB: active

    private LocalDateTime createdDate;  // → DB: created_date (Magnum Opus)

    private LocalDateTime updatedDate;  // → DB: updated_date (Magnum Opus)

    private LocalDateTime lastLogin;  // → DB: last_login

    public enum Role {
        SUPER_ADMIN, SHOP_ADMIN, CONTENT_ADMIN
    }

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