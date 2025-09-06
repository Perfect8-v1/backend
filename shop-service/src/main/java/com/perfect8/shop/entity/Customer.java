package com.perfect8.shop.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a customer.
 * Version 1.0 - Core functionality only
 */
@Entity
@Table(name = "customers", indexes = {
        @Index(name = "idx_email", columnList = "email", unique = true),
        @Index(name = "idx_phone", columnList = "phone"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"addresses", "orders", "cart"})
@ToString(exclude = {"addresses", "orders", "cart", "passwordHash"})
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(name = "email_verification_token", length = 255)
    private String emailVerificationToken;

    @Column(name = "email_verification_sent_at")
    private LocalDateTime emailVerificationSentAt;

    @Column(name = "password_reset_token", length = 255)
    private String passwordResetToken;

    @Column(name = "password_reset_token_expires_at")
    private LocalDateTime passwordResetTokenExpiresAt;

    // Customer status
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_blocked", nullable = false)
    @Builder.Default
    private Boolean isBlocked = false;

    @Column(name = "blocked_reason", columnDefinition = "TEXT")
    private String blockedReason;

    @Column(name = "blocked_at")
    private LocalDateTime blockedAt;

    // Marketing preferences
    @Column(name = "accepts_marketing", nullable = false)
    @Builder.Default
    private Boolean acceptsMarketing = false;

    @Column(name = "marketing_consent_updated_at")
    private LocalDateTime marketingConsentUpdatedAt;

    // Customer preferences
    @Column(name = "preferred_language", length = 5)
    @Builder.Default
    private String preferredLanguage = "en";

    @Column(name = "preferred_currency", length = 3)
    @Builder.Default
    private String preferredCurrency = "USD";

    // Relationships
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Address> addresses = new ArrayList<>();

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Order> orders = new ArrayList<>();

    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Cart cart;

    // Customer notes (internal use)
    @Column(name = "internal_notes", columnDefinition = "TEXT")
    private String internalNotes;

    // Customer tags for segmentation (comma-separated)
    @Column(name = "tags", length = 500)
    private String tags;

    // Total spent (cached for performance)
    @Column(name = "total_spent", precision = 10, scale = 2)
    @Builder.Default
    private java.math.BigDecimal totalSpent = java.math.BigDecimal.ZERO;

    @Column(name = "order_count")
    @Builder.Default
    private Integer orderCount = 0;

    // Important dates
    @Column(name = "last_order_date")
    private LocalDateTime lastOrderDate;

    @Column(name = "first_order_date")
    private LocalDateTime firstOrderDate;

    @Column(name = "birth_date")
    private java.time.LocalDate birthDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "last_login_ip", length = 45)
    private String lastLoginIp;

    // ========================================
    // Backward compatibility methods
    // ========================================

    /**
     * Backward compatibility getter for customerId
     * @return the customer id
     */
    public Long getCustomerId() {
        return this.id;
    }

    /**
     * Backward compatibility setter for customerId
     * @param customerId the customer id to set
     */
    public void setCustomerId(Long customerId) {
        this.id = customerId;
    }

    /**
     * Backward compatibility method for emailVerified check
     * @return true if email is verified
     */
    public boolean isEmailVerified() {
        return Boolean.TRUE.equals(this.emailVerified);
    }

    /**
     * Backward compatibility setter for emailVerified
     * @param emailVerified whether email is verified
     */
    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    // ========================================
    // Business logic methods
    // ========================================

    /**
     * Get customer's full name
     */
    public String getFullName() {
        return String.format("%s %s", firstName, lastName);
    }

    /**
     * Add an address to the customer
     */
    public void addAddress(Address address) {
        if (addresses == null) {
            addresses = new ArrayList<>();
        }
        addresses.add(address);
        address.setCustomer(this);
    }

    /**
     * Remove an address from the customer
     */
    public void removeAddress(Address address) {
        if (addresses != null) {
            addresses.remove(address);
            address.setCustomer(null);
        }
    }

    /**
     * Get default shipping address
     */
    public Address getDefaultShippingAddress() {
        if (addresses == null || addresses.isEmpty()) {
            return null;
        }
        return addresses.stream()
                .filter(Address::isDefaultShipping)
                .findFirst()
                .orElse(addresses.get(0));
    }

    /**
     * Get default billing address
     */
    public Address getDefaultBillingAddress() {
        if (addresses == null || addresses.isEmpty()) {
            return null;
        }
        return addresses.stream()
                .filter(Address::isDefaultBilling)
                .findFirst()
                .orElse(getDefaultShippingAddress());
    }

    /**
     * Check if customer can place orders
     */
    public boolean canPlaceOrder() {
        return Boolean.TRUE.equals(isActive) &&
                !Boolean.TRUE.equals(isBlocked) &&
                Boolean.TRUE.equals(emailVerified);
    }

    /**
     * Block customer with reason
     */
    public void block(String reason) {
        this.isBlocked = true;
        this.blockedReason = reason;
        this.blockedAt = LocalDateTime.now();
        this.isActive = false;
    }

    /**
     * Unblock customer
     */
    public void unblock() {
        this.isBlocked = false;
        this.blockedReason = null;
        this.blockedAt = null;
        this.isActive = true;
    }

    /**
     * Update marketing consent
     */
    public void updateMarketingConsent(boolean consent) {
        this.acceptsMarketing = consent;
        this.marketingConsentUpdatedAt = LocalDateTime.now();
    }

    /**
     * Record login
     */
    public void recordLogin(String ipAddress) {
        this.lastLoginAt = LocalDateTime.now();
        this.lastLoginIp = ipAddress;
    }

    /**
     * Update order statistics
     */
    public void updateOrderStatistics(java.math.BigDecimal orderAmount) {
        if (orderAmount != null) {
            this.totalSpent = this.totalSpent.add(orderAmount);
        }
        this.orderCount = this.orderCount + 1;
        this.lastOrderDate = LocalDateTime.now();
        if (this.firstOrderDate == null) {
            this.firstOrderDate = LocalDateTime.now();
        }
    }

    /**
     * Check if password reset token is valid
     */
    public boolean isPasswordResetTokenValid(String token) {
        return token != null &&
                token.equals(this.passwordResetToken) &&
                this.passwordResetTokenExpiresAt != null &&
                LocalDateTime.now().isBefore(this.passwordResetTokenExpiresAt);
    }

    /**
     * Generate email verification token
     */
    public void generateEmailVerificationToken() {
        this.emailVerificationToken = java.util.UUID.randomUUID().toString();
        this.emailVerificationSentAt = LocalDateTime.now();
    }

    /**
     * Generate password reset token
     */
    public void generatePasswordResetToken() {
        this.passwordResetToken = java.util.UUID.randomUUID().toString();
        this.passwordResetTokenExpiresAt = LocalDateTime.now().plusHours(24);
    }

    /**
     * Clear password reset token
     */
    public void clearPasswordResetToken() {
        this.passwordResetToken = null;
        this.passwordResetTokenExpiresAt = null;
    }

    /**
     * Verify email with token
     */
    public boolean verifyEmail(String token) {
        if (token != null && token.equals(this.emailVerificationToken)) {
            this.emailVerified = true;
            this.emailVerificationToken = null;
            this.emailVerificationSentAt = null;
            return true;
        }
        return false;
    }

    /**
     * Check if customer is new (no completed orders)
     */
    public boolean isNewCustomer() {
        return orderCount == null || orderCount == 0;
    }

    /**
     * Get customer segment
     */
    public String getCustomerSegment() {
        if (totalSpent == null || totalSpent.compareTo(java.math.BigDecimal.ZERO) == 0) {
            return "NEW";
        } else if (totalSpent.compareTo(new java.math.BigDecimal("1000")) < 0) {
            return "REGULAR";
        } else if (totalSpent.compareTo(new java.math.BigDecimal("5000")) < 0) {
            return "VIP";
        } else {
            return "PREMIUM";
        }
    }

    /**
     * Handle password synchronization
     */
    @PrePersist
    @PreUpdate
    public void handlePasswordSync() {
        if (this.password != null && !this.password.isEmpty()) {
            this.passwordHash = this.password;
        }
        if (this.passwordHash == null) {
            this.passwordHash = "";
        }
    }

    /**
     * Load password after entity is loaded
     */
    @PostLoad
    public void loadPassword() {
        this.password = this.passwordHash;
    }
}