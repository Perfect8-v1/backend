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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "customers", indexes = {
        @Index(name = "idx_customer_email", columnList = "email"),
        @Index(name = "idx_customer_business_id", columnList = "customer_business_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"orders", "addresses", "cart", "roles"})
@ToString(exclude = {"orders", "addresses", "cart", "roles"})
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "customer_business_id", unique = true, nullable = false, length = 20)
    private String customerBusinessId;

    @NotBlank(message = "First name is required")
    @Size(max = 50)
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50)
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    // Role as string for now (will convert to enum later)
    @Column(name = "role", nullable = false, length = 32)
    private String role = "CUSTOMER";

    // Account status fields
    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Builder.Default
    @Column(name = "is_email_verified")
    private Boolean isEmailVerified = false;

    @Column(name = "email_verification_token")
    private String emailVerificationToken;

    @Column(name = "email_verification_token_expiry")
    private LocalDateTime emailVerificationTokenExpiry;

    @Column(name = "email_verified_at")
    private LocalDateTime emailVerifiedAt;

    // Security fields
    @Builder.Default
    @Column(name = "login_attempts")
    private Integer loginAttempts = 0;

    @Builder.Default
    @Column(name = "is_account_locked")
    private Boolean isAccountLocked = false;

    @Column(name = "account_locked_at")
    private LocalDateTime accountLockedAt;

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    @Column(name = "password_reset_token")
    private String passwordResetToken;

    @Column(name = "password_reset_token_expiry")
    private LocalDateTime passwordResetTokenExpiry;

    // Timestamps
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    // Relations
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Order> orders = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Address> addresses = new ArrayList<>();

    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private Cart cart;

    // Many-to-many with roles (for future use, but keeping for backward compatibility)
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "customer_roles",
            joinColumns = @JoinColumn(name = "customer_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    // Default shipping address
    @ManyToOne
    @JoinColumn(name = "default_shipping_address_id")
    private Address defaultShippingAddress;

    // Helper methods
    public String getCustomerFullName() {
        return firstName + " " + lastName;
    }

    // Backward compatibility methods
    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        this.isActive = active;
    }

    public Boolean getAccountLocked() {
        return isAccountLocked;
    }

    public void setAccountLocked(Boolean accountLocked) {
        this.isAccountLocked = accountLocked;
    }

    public Boolean isActive() {
        return isActive != null && isActive;
    }

    public Boolean isEmailVerified() {
        return isEmailVerified != null && isEmailVerified;
    }

    public void setIsEmailVerified(Boolean emailVerified) {
        this.isEmailVerified = emailVerified;
    }

    // Legacy getId() method for backward compatibility
    public Long getId() {
        return customerId;
    }

    // Helper method to add address
    public void addAddress(Address address) {
        addresses.add(address);
        address.setCustomer(this);
    }

    // Helper method to remove address
    public void removeAddress(Address address) {
        addresses.remove(address);
        address.setCustomer(null);
    }

    // Helper method to add order
    public void addOrder(Order order) {
        orders.add(order);
        order.setCustomer(this);
    }

    // Helper method to remove order
    public void removeOrder(Order order) {
        orders.remove(order);
        order.setCustomer(null);
    }
}