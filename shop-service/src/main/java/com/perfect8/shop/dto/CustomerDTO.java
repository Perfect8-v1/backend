package com.perfect8.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Customer DTO for Shop Service
 * Version 1.0 - Core customer data transfer
 * FIXED: Correct field types and names matching CustomerService
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDTO {

    /**
     * Customer ID - FIXED: Using Long, not String!
     */
    private Long customerId;

    /**
     * Basic information
     */
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;

    /**
     * Security role
     */
    private String role;

    /**
     * Account status
     */
    private boolean isActive;
    private boolean isEmailVerified;

    /**
     * Customer preferences (v1.0)
     */
    private Boolean newsletterSubscribed;
    private Boolean marketingConsent;
    private String preferredLanguage;
    private String preferredCurrency;

    /**
     * Timestamps
     */
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private LocalDateTime lastLoginDate;

    /**
     * Addresses
     */
    private List<AddressDTO> addresses;

    /**
     * Order information (optional, populated when needed)
     */
    private Integer orderCount;
    private Boolean hasOrders;

    /**
     * Helper methods
     */
    public String getFullName() {
        StringBuilder sb = new StringBuilder();
        if (firstName != null && !firstName.isEmpty()) {
            sb.append(firstName);
        }
        if (lastName != null && !lastName.isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(lastName);
        }
        return sb.toString();
    }

    /**
     * Check if customer is admin
     */
    public boolean isAdmin() {
        return "ROLE_ADMIN".equals(role);
    }

    /**
     * Check if customer is staff
     */
    public boolean isStaff() {
        return "ROLE_STAFF".equals(role) || isAdmin();
    }

    /**
     * Get display name for UI
     */
    public String getDisplayName() {
        String fullName = getFullName();
        if (!fullName.isEmpty()) {
            return fullName;
        }
        // Fall back to email if no name
        return email != null ? email : "Customer #" + customerId;
    }

    /**
     * Get customer since date (alias for createdDate for UI)
     */
    public LocalDateTime getCustomerSince() {
        return createdDate;
    }

    /**
     * Get registration date (alias for createdDate for UI)
     */
    public LocalDateTime getRegistrationDate() {
        return createdDate;
    }
}