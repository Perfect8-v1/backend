package com.perfect8.shop.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Customer DTO for Shop Service
 * Version 1.0 - Core customer data transfer
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDTO {

    // Primary identifiers
    private String customerId;  // Business ID like "CUST-ABC123"
    private Long id;            // Database ID

    // Basic information
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;

    // Account status
    private Boolean isActive;
    private Boolean isEmailVerified;

    // Dates
    private LocalDateTime registrationDate;
    private LocalDateTime lastLoginDate;
    private LocalDateTime customerSince;

    // Order information
    private Boolean hasOrders;
    private Integer orderCount;

    // Addresses
    private List<AddressDTO> addresses;

    // Helper methods
    public String getFullName() {
        return (firstName != null ? firstName : "") + " " +
                (lastName != null ? lastName : "");
    }

    // Version 2.0 fields - commented out for v1.0
    /*
    private String gender;
    private LocalDate dateOfBirth;
    private BigDecimal totalSpent;
    private String preferredCurrency;
    private String preferredLanguage;
    private String customerTier;
    private Integer loyaltyPoints;
    private LocalDateTime lastPurchaseDate;
    private BigDecimal averageOrderValue;
    private List<String> tags;
    private Map<String, Object> preferences;
    */
}