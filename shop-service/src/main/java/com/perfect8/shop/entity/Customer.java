package com.perfect8.shop.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Customer Entity - Version 1.0
 * Magnum Opus Compliant: No alias methods, Lombok generates all getters/setters
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
    private Long customerId;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(length = 20)
    private String phone;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    private LocalDateTime emailVerifiedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;

    private LocalDateTime lastLoginDate;

    private String resetPasswordToken;

    private LocalDateTime resetPasswordTokenExpiry;

    @Column(length = 50)
    @Builder.Default
    private String role = "CUSTOMER";

    @Builder.Default
    private Boolean newsletterSubscribed = false;

    @Builder.Default
    private Boolean marketingConsent = false;

    private String preferredLanguage;

    private String preferredCurrency;

    private String emailVerificationToken;

    private LocalDateTime emailVerificationSentAt;

    @Builder.Default
    private Integer failedLoginAttempts = 0;

    private LocalDateTime accountLockedUntil;

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

    // ========== Business Logic Helper Methods (NOT Aliases) ==========

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean isActive() {
        return Boolean.TRUE.equals(active);
    }

    public boolean isEmailVerified() {
        return Boolean.TRUE.equals(emailVerified);
    }

    public boolean isAccountLocked() {
        return accountLockedUntil != null && accountLockedUntil.isAfter(LocalDateTime.now());
    }

    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts = (this.failedLoginAttempts == null ? 0 : this.failedLoginAttempts) + 1;
        if (this.failedLoginAttempts >= 5) {
            this.accountLockedUntil = LocalDateTime.now().plusMinutes(30);
        }
    }

    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
        this.accountLockedUntil = null;
    }

    public void updateLastLoginTime() {
        this.lastLoginDate = LocalDateTime.now();
    }

    public void generateEmailVerificationToken() {
        this.emailVerificationToken = java.util.UUID.randomUUID().toString();
        this.emailVerificationSentAt = LocalDateTime.now();
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
                .filter(a -> "SHIPPING".equals(a.getAddressType()) && a.getIsDefault())
                .findFirst()
                .orElse(addresses.stream()
                        .filter(a -> "SHIPPING".equals(a.getAddressType()))
                        .findFirst()
                        .orElse(null));
    }
}