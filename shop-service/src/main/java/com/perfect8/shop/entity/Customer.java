package com.perfect8.shop.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Customer Entity - Version 1.0
 * 
 * UPDATED (2025-11-20):
 * - Removed auth fields (handled by admin-service)
 * - Added userId reference to User in admin-service
 * - Simplified to customer profile data only
 */
@Entity
@Table(name = "customers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"addresses", "orders", "cart"})
@ToString(exclude = {"addresses", "orders", "cart"})
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Long customerId;

    /**
     * Reference to User in admin-service (central auth)
     * Can be null for guest checkout
     */
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @Column(name = "is_email_verified", nullable = false)
    @Builder.Default
    private boolean isEmailVerified = false;

    @Column(name = "email_verified_date")
    private LocalDateTime emailVerifiedDate;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "last_login_date")
    private LocalDateTime lastLoginDate;

    @Column(name = "newsletter_subscribed", nullable = false)
    @Builder.Default
    private boolean newsletterSubscribed = false;

    @Column(name = "marketing_consent", nullable = false)
    @Builder.Default
    private boolean marketingConsent = false;

    @Column(name = "preferred_language", length = 10)
    private String preferredLanguage;

    @Column(name = "preferred_currency", length = 3)
    private String preferredCurrency;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Address> addresses = new ArrayList<>();

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Order> orders = new ArrayList<>();

    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Cart cart;

    // ========== JPA Lifecycle Callbacks ==========

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }

    // ========== Business Logic Helper Methods ==========

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public void updateLastLoginTime() {
        this.lastLoginDate = LocalDateTime.now();
    }

    public void verifyEmail() {
        this.isEmailVerified = true;
        this.emailVerifiedDate = LocalDateTime.now();
    }

    /**
     * Check if this is a guest customer (no user account)
     */
    public boolean isGuest() {
        return userId == null;
    }

    /**
     * Check if customer has a linked user account
     */
    public boolean hasUserAccount() {
        return userId != null;
    }

    // ========== Address Management ==========

    public void addAddress(Address address) {
        addresses.add(address);
        address.setCustomer(this);
    }

    public void removeAddress(Address address) {
        addresses.remove(address);
        address.setCustomer(null);
    }

    public Address getDefaultShippingAddress() {
        return addresses.stream()
                .filter(a -> "SHIPPING".equals(a.getAddressType()) && a.isDefaultAddress())
                .findFirst()
                .orElse(addresses.stream()
                        .filter(a -> "SHIPPING".equals(a.getAddressType()))
                        .findFirst()
                        .orElse(null));
    }

    public Address getDefaultBillingAddress() {
        return addresses.stream()
                .filter(a -> "BILLING".equals(a.getAddressType()) && a.isDefaultAddress())
                .findFirst()
                .orElse(addresses.stream()
                        .filter(a -> "BILLING".equals(a.getAddressType()))
                        .findFirst()
                        .orElse(null));
    }
}
