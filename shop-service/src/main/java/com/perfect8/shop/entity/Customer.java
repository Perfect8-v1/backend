package com.perfect8.shop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a customer in the system.
 * Version 1.0 - Core customer functionality
 * NO BACKWARD COMPATIBILITY - Built right from the start!
 */
@Entity
@Table(name = "customers", indexes = {
        @Index(name = "idx_customer_email", columnList = "email", unique = true),
        @Index(name = "idx_customer_created", columnList = "created_at"),
        @Index(name = "idx_customer_active", columnList = "is_active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"addresses", "orders", "cart"})
@ToString(exclude = {"addresses", "orders", "cart", "password"})
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Long customerId;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Column(name = "password", nullable = false)
    private String password;

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name cannot exceed 100 characters")
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "role", nullable = false, length = 50)
    @Builder.Default
    private String role = "ROLE_USER";

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @Column(name = "is_email_verified", nullable = false)
    @Builder.Default
    private boolean isEmailVerified = false;

    @Column(name = "email_verification_token")
    private String emailVerificationToken;

    @Column(name = "email_verification_sent_at")
    private LocalDateTime emailVerificationSentAt;

    @Column(name = "password_reset_token")
    private String passwordResetToken;

    @Column(name = "password_reset_expires_at")
    private LocalDateTime passwordResetExpiresAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "failed_login_attempts")
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    @Column(name = "account_locked_until")
    private LocalDateTime accountLockedUntil;

    // Relationships
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Address> addresses = new ArrayList<>();

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Order> orders = new ArrayList<>();

    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Cart cart;

    @Column(name = "default_billing_address_id")
    private Long defaultBillingAddressId;

    @Column(name = "default_shipping_address_id")
    private Long defaultShippingAddressId;

    // Customer preferences (v1.0)
    @Column(name = "newsletter_subscribed")
    @Builder.Default
    private Boolean newsletterSubscribed = false;

    @Column(name = "marketing_consent")
    @Builder.Default
    private Boolean marketingConsent = false;

    @Column(name = "preferred_language", length = 5)
    @Builder.Default
    private String preferredLanguage = "en";

    @Column(name = "preferred_currency", length = 3)
    @Builder.Default
    private String preferredCurrency = "USD";

    // Notes
    @Column(name = "internal_notes", columnDefinition = "TEXT")
    private String internalNotes;

    // Timestamps
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ========================================
    // Business methods - PROPER NAMING
    // ========================================

    /**
     * Get full name of customer
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Check if account is locked
     */
    public boolean isAccountLocked() {
        return accountLockedUntil != null &&
                accountLockedUntil.isAfter(LocalDateTime.now());
    }

    /**
     * Lock account for specified minutes
     */
    public void lockAccount(int minutes) {
        this.accountLockedUntil = LocalDateTime.now().plusMinutes(minutes);
    }

    /**
     * Unlock account
     */
    public void unlockAccount() {
        this.accountLockedUntil = null;
        this.failedLoginAttempts = 0;
    }

    /**
     * Increment failed login attempts
     */
    public void incrementFailedLoginAttempts() {
        if (failedLoginAttempts == null) {
            failedLoginAttempts = 0;
        }
        failedLoginAttempts++;
    }

    /**
     * Reset failed login attempts
     */
    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
    }

    /**
     * Update last login time
     */
    public void updateLastLoginTime() {
        this.lastLoginAt = LocalDateTime.now();
    }

    /**
     * Check if password reset token is valid
     */
    public boolean isPasswordResetTokenValid() {
        return passwordResetToken != null &&
                passwordResetExpiresAt != null &&
                passwordResetExpiresAt.isAfter(LocalDateTime.now());
    }

    /**
     * Clear password reset token
     */
    public void clearPasswordResetToken() {
        this.passwordResetToken = null;
        this.passwordResetExpiresAt = null;
    }

    /**
     * Generate email verification token
     */
    public void generateEmailVerificationToken() {
        this.emailVerificationToken = generateToken();
        this.emailVerificationSentAt = LocalDateTime.now();
    }

    /**
     * Verify email
     */
    public void verifyEmail() {
        this.isEmailVerified = true;
        this.emailVerificationToken = null;
        this.emailVerificationSentAt = null;
    }

    /**
     * Generate password reset token
     */
    public void generatePasswordResetToken() {
        this.passwordResetToken = generateToken();
        this.passwordResetExpiresAt = LocalDateTime.now().plusHours(1);
    }

    /**
     * Add address
     */
    public void addAddress(Address address) {
        if (addresses == null) {
            addresses = new ArrayList<>();
        }
        addresses.add(address);
        address.setCustomer(this);
    }

    /**
     * Remove address
     */
    public void removeAddress(Address address) {
        if (addresses != null) {
            addresses.remove(address);
            address.setCustomer(null);

            // Clear default if removed
            if (address.getAddressId() != null) {
                if (address.getAddressId().equals(defaultBillingAddressId)) {
                    defaultBillingAddressId = null;
                }
                if (address.getAddressId().equals(defaultShippingAddressId)) {
                    defaultShippingAddressId = null;
                }
            }
        }
    }

    /**
     * Get default billing address
     */
    public Address getDefaultBillingAddress() {
        if (defaultBillingAddressId == null || addresses == null) {
            return null;
        }
        return addresses.stream()
                .filter(a -> defaultBillingAddressId.equals(a.getAddressId()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Get default shipping address
     */
    public Address getDefaultShippingAddress() {
        if (defaultShippingAddressId == null || addresses == null) {
            return null;
        }
        return addresses.stream()
                .filter(a -> defaultShippingAddressId.equals(a.getAddressId()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Check if customer has admin role
     */
    public boolean isAdmin() {
        return "ROLE_ADMIN".equals(role);
    }

    /**
     * Check if customer has staff role
     */
    public boolean isStaff() {
        return "ROLE_STAFF".equals(role) || isAdmin();
    }

    /**
     * Get total order count
     */
    public int getTotalOrderCount() {
        return orders != null ? orders.size() : 0;
    }

    /**
     * Check if customer can place order
     */
    public boolean canPlaceOrder() {
        return isActive && isEmailVerified && !isAccountLocked();
    }

    @PrePersist
    public void prePersist() {
        if (role == null) {
            role = "ROLE_USER";
        }
        if (preferredLanguage == null) {
            preferredLanguage = "en";
        }
        if (preferredCurrency == null) {
            preferredCurrency = "USD";
        }
        if (failedLoginAttempts == null) {
            failedLoginAttempts = 0;
        }
        if (newsletterSubscribed == null) {
            newsletterSubscribed = Boolean.FALSE;
        }
        if (marketingConsent == null) {
            marketingConsent = Boolean.FALSE;
        }
    }

    /**
     * Generate unique token
     */
    private String generateToken() {
        Long timestamp = System.currentTimeMillis();
        String nanoTime = String.valueOf(System.nanoTime());
        return timestamp + "-" + nanoTime.substring(nanoTime.length() - 6);
    }

    /**
     * Version 2.0 features - commented out
     *
     * In v2.0, we'll add:
     * - Customer segments
     * - Loyalty points
     * - Wishlist
     * - Product reviews
     * - Customer tags
     * - Purchase history analytics
     * - Preferred payment methods
     * - Social login connections
     */
}