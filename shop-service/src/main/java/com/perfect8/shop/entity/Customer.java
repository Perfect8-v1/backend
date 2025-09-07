package com.perfect8.shop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long customerId;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(length = 10)
    private String gender;

    @Column(name = "registration_date", nullable = false)
    private LocalDateTime registrationDate;

    @Column(name = "last_login_date")
    private LocalDateTime lastLoginDate;

    @Column(name = "is_email_verified")
    @Builder.Default
    private Boolean isEmailVerified = false;

    @Column(name = "email_verification_token", length = 100)
    private String emailVerificationToken;

    @Column(name = "email_verification_sent_date")
    private LocalDateTime emailVerificationSentDate;

    @Column(name = "password_reset_token", length = 100)
    private String passwordResetToken;

    @Column(name = "password_reset_token_expiry")
    private LocalDateTime passwordResetTokenExpiry;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(name = "deleted_date")
    private LocalDateTime deletedDate;

    @Column(name = "preferred_currency", length = 3)
    @Builder.Default
    private String preferredCurrency = "USD";

    @Column(name = "preferred_language", length = 5)
    @Builder.Default
    private String preferredLanguage = "en";

    @Column(columnDefinition = "TEXT")
    private String notes;

    // Relationships
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Address> addresses = new ArrayList<>();

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Order> orders = new ArrayList<>();

    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Cart cart;

    // Timestamps
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (registrationDate == null) {
            registrationDate = LocalDateTime.now();
        }
        if (isActive == null) {
            isActive = true;
        }
        if (isDeleted == null) {
            isDeleted = false;
        }
        if (isEmailVerified == null) {
            isEmailVerified = false;
        }
        if (preferredCurrency == null) {
            preferredCurrency = "USD";
        }
        if (preferredLanguage == null) {
            preferredLanguage = "en";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods
    public String getFullName() {
        return firstName + " " + lastName;
    }

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
                .filter(Address::isDefaultShipping)
                .findFirst()
                .orElse(null);
    }

    public Address getDefaultBillingAddress() {
        return addresses.stream()
                .filter(Address::isDefaultBilling)
                .findFirst()
                .orElse(null);
    }

    public void setDefaultShippingAddress(Address address) {
        // Clear other defaults
        addresses.forEach(a -> a.setDefaultShipping(false));
        // Set new default
        if (addresses.contains(address)) {
            address.setDefaultShipping(true);
        }
    }

    public void setDefaultBillingAddress(Address address) {
        // Clear other defaults
        addresses.forEach(a -> a.setDefaultBilling(false));
        // Set new default
        if (addresses.contains(address)) {
            address.setDefaultBilling(true);
        }
    }

    public boolean hasVerifiedEmail() {
        return Boolean.TRUE.equals(isEmailVerified);
    }

    public boolean isActive() {
        return Boolean.TRUE.equals(isActive) && !Boolean.TRUE.equals(isDeleted);
    }

    public void markAsDeleted() {
        this.isDeleted = true;
        this.isActive = false;
        this.deletedDate = LocalDateTime.now();
        // Clear sensitive data
        this.email = "deleted_" + customerId + "@deleted.com";
        this.password = "DELETED";
        this.phoneNumber = null;
        this.dateOfBirth = null;
    }

    public void updateLastLogin() {
        this.lastLoginDate = LocalDateTime.now();
    }

    public boolean hasOrders() {
        return orders != null && !orders.isEmpty();
    }

    public int getOrderCount() {
        return orders != null ? orders.size() : 0;
    }

    public Optional<Order> getLatestOrder() {
        if (orders == null || orders.isEmpty()) {
            return Optional.empty();
        }
        return orders.stream()
                .max((o1, o2) -> o1.getCreatedAt().compareTo(o2.getCreatedAt()));
    }

    // Password reset methods
    public void setPasswordResetToken(String token) {
        this.passwordResetToken = token;
        this.passwordResetTokenExpiry = LocalDateTime.now().plusHours(24);
    }

    public boolean isPasswordResetTokenValid() {
        return passwordResetToken != null &&
                passwordResetTokenExpiry != null &&
                passwordResetTokenExpiry.isAfter(LocalDateTime.now());
    }

    public void clearPasswordResetToken() {
        this.passwordResetToken = null;
        this.passwordResetTokenExpiry = null;
    }

    // Email verification methods
    public void setEmailVerificationToken(String token) {
        this.emailVerificationToken = token;
        this.emailVerificationSentDate = LocalDateTime.now();
    }

    public void verifyEmail() {
        this.isEmailVerified = true;
        this.emailVerificationToken = null;
        this.emailVerificationSentDate = null;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "customerId=" + customerId +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }
}