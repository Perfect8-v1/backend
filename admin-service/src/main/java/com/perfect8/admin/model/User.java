package com.perfect8.admin.model;

import com.perfect8.common.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * User Entity - Version 1.3 Unified
 * 
 * Central användartabell för både admins och customers.
 * - Admins: roles = {SUPER_ADMIN, ADMIN, STAFF, WRITER}
 * - Customers: roles = {USER, CUSTOMER}
 * 
 * ÄNDRINGAR v1.3:
 * - Borttaget: passwordSalt (v1.2 legacy, används ej i industristandard)
 * - Tillagt: phone (för customer-profiler)
 * - Industristandard: password skickas plaintext över HTTPS, hashas i backend
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    /**
     * BCrypt hash av lösenord
     * Hashas i backend med BCryptPasswordEncoder
     */
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    /**
     * Telefonnummer (för customers)
     * TODO: Verifiera om detta ska ligga här eller i Customer entity
     */
    @Column(name = "phone", length = 20)
    private String phone;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @Column(name = "is_email_verified", nullable = false)
    @Builder.Default
    private boolean isEmailVerified = false;

    @Column(name = "email_verification_token", length = 255)
    private String emailVerificationToken;

    @Column(name = "reset_password_token", length = 255)
    private String resetPasswordToken;

    @Column(name = "reset_password_token_expiry")
    private LocalDateTime resetPasswordTokenExpiry;

    @Column(name = "failed_login_attempts", nullable = false)
    @Builder.Default
    private int failedLoginAttempts = 0;

    @Column(name = "account_locked_until")
    private LocalDateTime accountLockedUntil;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "last_login_date")
    private LocalDateTime lastLoginDate;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        if (roles == null || roles.isEmpty()) {
            roles = new HashSet<>();
            roles.add(Role.USER);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }

    // ========== Role Convenience Methods ==========

    public boolean hasRole(Role role) {
        return roles != null && roles.contains(role);
    }

    public boolean isAdmin() {
        return hasRole(Role.ADMIN) || hasRole(Role.SUPER_ADMIN);
    }

    public boolean isSuperAdmin() {
        return hasRole(Role.SUPER_ADMIN);
    }

    public boolean isStaff() {
        return hasRole(Role.STAFF) || isAdmin();
    }

    public boolean isWriter() {
        return hasRole(Role.WRITER) || isAdmin();
    }

    public boolean isCustomer() {
        return hasRole(Role.CUSTOMER) || hasRole(Role.USER);
    }

    public boolean isLocked() {
        return accountLockedUntil != null &&
                accountLockedUntil.isAfter(LocalDateTime.now());
    }

    // ========== Name Methods ==========

    public String getFullName() {
        if (firstName == null && lastName == null) {
            return email;
        }
        return (firstName != null ? firstName : "") +
                (lastName != null ? " " + lastName : "").trim();
    }

    // ========== Account Status ==========

    public boolean canLogin() {
        return isActive && !isLocked();
    }
}
