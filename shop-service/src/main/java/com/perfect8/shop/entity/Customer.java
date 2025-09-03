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
        @Index(name = "idx_customer_email", columnList = "email", unique = true),
        @Index(name = "idx_customer_phone", columnList = "phone_number")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"orders", "addresses", "cart"})
@ToString(exclude = {"orders", "addresses", "cart"})
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;  // Changed from customerId to id for consistency

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "password", nullable = false)
    private String password;  // Changed from passwordHash to password for simplicity

    @Column(name = "email_verified")
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    // Relationships
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Order> orders = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Address> addresses = new ArrayList<>();

    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Cart cart;

    // Default address references
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_shipping_address_id")
    private Address defaultShippingAddress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_billing_address_id")
    private Address defaultBillingAddress;

    // Marketing preferences
    @Column(name = "newsletter_subscribed")
    @Builder.Default
    private Boolean newsletterSubscribed = false;

    @Column(name = "sms_notifications")
    @Builder.Default
    private Boolean smsNotifications = false;

    // Additional fields that were missing but referenced in services
    @Column(name = "phone", length = 20)
    private String phone;  // Alias for phoneNumber, some code uses this

    @Column(name = "date_of_birth")
    private LocalDateTime dateOfBirth;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "accepts_marketing")
    @Builder.Default
    private Boolean acceptsMarketing = false;

    @Column(name = "preferred_language", length = 10)
    @Builder.Default
    private String preferredLanguage = "en";

    // Security
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "failed_login_attempts")
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    @Column(name = "account_locked")
    @Builder.Default
    private Boolean accountLocked = false;

    @Column(name = "password_reset_token")
    private String passwordResetToken;

    @Column(name = "password_reset_token_expires")
    private LocalDateTime passwordResetTokenExpires;

    // Metadata
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Add registration date as alias for createdAt (some code uses this)
    public LocalDateTime getRegistrationDate() {
        return createdAt;
    }

    /**
     * Get full name
     */
    public String getFullName() {
        return String.format("%s %s", firstName, lastName);
    }

    /**
     * Add an address
     */
    public void addAddress(Address address) {
        if (addresses == null) {
            addresses = new ArrayList<>();
        }
        addresses.add(address);
        address.setCustomer(this);
    }

    /**
     * Remove an address
     */
    public void removeAddress(Address address) {
        if (addresses != null) {
            addresses.remove(address);
            address.setCustomer(null);
        }

        // Clear default if removed
        if (address.equals(defaultShippingAddress)) {
            defaultShippingAddress = null;
        }
        if (address.equals(defaultBillingAddress)) {
            defaultBillingAddress = null;
        }
    }

    /**
     * Check if account is usable
     */
    public boolean canLogin() {
        return Boolean.TRUE.equals(isActive) &&
                !Boolean.TRUE.equals(accountLocked) &&
                Boolean.TRUE.equals(emailVerified);
    }

    /**
     * Record successful login
     */
    public void recordSuccessfulLogin() {
        this.lastLoginAt = LocalDateTime.now();
        this.failedLoginAttempts = 0;
    }

    /**
     * Record failed login attempt
     */
    public void recordFailedLogin() {
        this.failedLoginAttempts = (failedLoginAttempts == null ? 0 : failedLoginAttempts) + 1;
        if (this.failedLoginAttempts >= 5) {
            this.accountLocked = true;
        }
    }

    /**
     * Generate password reset token
     */
    public void generatePasswordResetToken(String token) {
        this.passwordResetToken = token;
        this.passwordResetTokenExpires = LocalDateTime.now().plusHours(24);
    }

    /**
     * Clear password reset token
     */
    public void clearPasswordResetToken() {
        this.passwordResetToken = null;
        this.passwordResetTokenExpires = null;
    }

    /**
     * Check if password reset token is valid
     */
    public boolean isPasswordResetTokenValid(String token) {
        return passwordResetToken != null &&
                passwordResetToken.equals(token) &&
                passwordResetTokenExpires != null &&
                passwordResetTokenExpires.isAfter(LocalDateTime.now());
    }

    // Method aliases for code compatibility
    public void setIsEmailVerified(boolean verified) {
        this.emailVerified = verified;
    }

    public boolean isActive() {
        return Boolean.TRUE.equals(this.isActive);
    }
}