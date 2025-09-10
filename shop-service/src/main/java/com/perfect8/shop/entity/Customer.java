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
    @Column(name = "customer_id")
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

    // ========== Helper methods ==========

    /**
     * Get customer's full name
     * Used in emails, orders, and display purposes
     */
    public String getCustomerFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Alias for getCustomerFullName() for backward compatibility
     * @deprecated Use getCustomerFullName() instead
     */
    @Deprecated
    public String getFullName() {
        return getCustomerFullName();
    }

    /**
     * Get customer's display name (for UI)
     */
    public String getCustomerDisplayName() {
        if (firstName != null && !firstName.trim().isEmpty()) {
            return firstName;
        }
        return email.split("@")[0];
    }

    /**
     * Get formatted customer info for admin display
     */
    public String getCustomerInfo() {
        return String.format("%s %s (%s)", firstName, lastName, email);
    }

    // ========== Address management ==========

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

    // ========== Status checks ==========

    public boolean hasVerifiedEmail() {
        return Boolean.TRUE.equals(isEmailVerified);
    }

    public boolean isActiveCustomer() {
        return Boolean.TRUE.equals(isActive) && !Boolean.TRUE.equals(isDeleted);
    }

    public boolean canPlaceOrder() {
        return isActiveCustomer() && hasVerifiedEmail();
    }

    public boolean canLogin() {
        return isActiveCustomer();
    }

    // ========== Account management ==========

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

    public void deactivateAccount() {
        this.isActive = false;
    }

    public void reactivateAccount() {
        if (!Boolean.TRUE.equals(isDeleted)) {
            this.isActive = true;
        }
    }

    public void updateLastLogin() {
        this.lastLoginDate = LocalDateTime.now();
    }

    // ========== Order management ==========

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

    // ========== Password reset methods ==========

    public void setPasswordResetToken(String token) {
        this.passwordResetToken = token;
        this.passwordResetTokenExpiry = LocalDateTime.now().plusHours(24);
    }

    public boolean isPasswordResetTokenValid() {
        return passwordResetToken != null &&
                passwordResetTokenExpiry != null &&
                passwordResetTokenExpiry.isAfter(LocalDateTime.now());
    }

    public boolean isPasswordResetTokenValid(String token) {
        return token != null &&
                token.equals(this.passwordResetToken) &&
                isPasswordResetTokenValid();
    }

    public void clearPasswordResetToken() {
        this.passwordResetToken = null;
        this.passwordResetTokenExpiry = null;
    }

    public void changePassword(String newPasswordHash) {
        this.password = newPasswordHash;
        clearPasswordResetToken();
        this.updatedAt = LocalDateTime.now();
    }

    // ========== Email verification methods ==========

    public void setEmailVerificationToken(String token) {
        this.emailVerificationToken = token;
        this.emailVerificationSentDate = LocalDateTime.now();
    }

    public boolean isEmailVerificationTokenValid(String token) {
        if (token == null || emailVerificationToken == null) {
            return false;
        }

        // Token valid for 48 hours
        if (emailVerificationSentDate != null) {
            LocalDateTime expiryTime = emailVerificationSentDate.plusHours(48);
            if (LocalDateTime.now().isAfter(expiryTime)) {
                return false;
            }
        }

        return token.equals(emailVerificationToken);
    }

    public void verifyEmail() {
        this.isEmailVerified = true;
        this.emailVerificationToken = null;
        this.emailVerificationSentDate = null;
        this.updatedAt = LocalDateTime.now();
    }

    public void resendEmailVerification(String newToken) {
        this.emailVerificationToken = newToken;
        this.emailVerificationSentDate = LocalDateTime.now();
    }

    // ========== Utility methods ==========

    public boolean needsEmailVerification() {
        return !hasVerifiedEmail() && isActiveCustomer();
    }

    public boolean canRequestPasswordReset() {
        return isActiveCustomer();
    }

    public String getCustomerSince() {
        if (registrationDate != null) {
            return registrationDate.toLocalDate().toString();
        }
        return createdAt.toLocalDate().toString();
    }

    @Override
    public String toString() {
        return "Customer{" +
                "customerId=" + customerId +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", isActive=" + isActive +
                ", isEmailVerified=" + isEmailVerified +
                '}';
    }

    /* VERSION 2.0 FEATURES (kommenterat bort för v1.0):
     * - Loyalty points och rewards
     * - Customer segments och tiers
     * - Wishlist functionality
     * - Product reviews och ratings
     * - Customer preferences (marketing opt-in/out)
     * - Social media login integration
     * - Two-factor authentication
     * - Login history och device tracking
     * - Customer tags för marknadsföring
     * - Referral program tracking
     * - Birthday rewards
     * - Customer lifetime value tracking
     * - Abandoned cart recovery
     * - Customer communication preferences
     */
}