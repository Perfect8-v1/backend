package com.perfect8.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.io.Serializable;

/**
 * Data Transfer Object for Address
 * Version 1.0 - Critical for accurate deliveries!
 * 
 * MAGNUM OPUS (2025-11-22):
 * - Boolean fields WITHOUT "is" prefix
 * - Field: defaultShipping → Lombok: isDefaultShipping(), setDefaultShipping()
 * - Field: defaultBilling → Lombok: isDefaultBilling(), setDefaultBilling()
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long addressId;

    @NotNull(message = "Address type is required")
    @Pattern(regexp = "^(BILLING|SHIPPING|BOTH)$",
            message = "Address type must be BILLING, SHIPPING, or BOTH")
    @Builder.Default
    private String addressType = "BOTH";

    @NotBlank(message = "Recipient name is required for delivery")
    @Size(min = 2, max = 100, message = "Recipient name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s\\-'.]+$",
            message = "Recipient name contains invalid characters")
    private String recipientName;

    @Size(max = 100, message = "Company name cannot exceed 100 characters")
    private String companyName;

    @NotBlank(message = "Street address is required")
    @Size(min = 5, max = 255, message = "Street address must be between 5 and 255 characters")
    private String streetAddress;

    @Size(max = 255, message = "Address line 2 cannot exceed 255 characters")
    private String addressLine2;

    @NotBlank(message = "City is required")
    @Size(min = 2, max = 100, message = "City must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s\\-'.]+$",
            message = "City name contains invalid characters")
    private String city;

    @Size(min = 2, max = 100, message = "State/Province must be between 2 and 100 characters")
    private String state;

    @Pattern(regexp = "^[A-Z]{2}$|^$",
            message = "State code must be 2 uppercase letters")
    private String stateCode;

    @NotBlank(message = "Postal code is required")
    @Pattern(regexp = "^[A-Z0-9\\s\\-]{3,10}$",
            message = "Invalid postal code format")
    private String postalCode;

    @NotBlank(message = "Country is required")
    @Size(min = 2, max = 100, message = "Country must be between 2 and 100 characters")
    private String country;

    @NotBlank(message = "Country code is required for shipping calculations")
    @Pattern(regexp = "^[A-Z]{2}$",
            message = "Country code must be 2 uppercase letters (ISO format)")
    private String countryCode;

    @NotBlank(message = "Phone number is required for delivery coordination")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$",
            message = "Invalid phone number format (use international format, e.g., +1234567890)")
    private String phoneNumber;

    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email cannot exceed 255 characters")
    private String emailAddress;

    @Size(max = 500, message = "Delivery instructions cannot exceed 500 characters")
    private String deliveryInstructions;

    // MAGNUM OPUS: primitive boolean defaultShipping (NOT isDefaultShipping)
    // Lombok generates: isDefaultShipping(), setDefaultShipping()
    @Builder.Default
    private boolean defaultShipping = false;

    // MAGNUM OPUS: primitive boolean defaultBilling (NOT isDefaultBilling)
    // Lombok generates: isDefaultBilling(), setDefaultBilling()
    @Builder.Default
    private boolean defaultBilling = false;

    @Builder.Default
    private boolean verified = false;

    private String verifiedDate;
    private String verificationMethod;

    @DecimalMin(value = "-90.0", message = "Invalid latitude")
    @DecimalMax(value = "90.0", message = "Invalid latitude")
    private Double latitude;

    @DecimalMin(value = "-180.0", message = "Invalid longitude")
    @DecimalMax(value = "180.0", message = "Invalid longitude")
    private Double longitude;

    @Builder.Default
    private boolean residential = true;

    @Builder.Default
    private boolean signatureRequired = false;

    @Size(max = 100, message = "External ID cannot exceed 100 characters")
    private String externalId;

    // ========== Utility methods ==========

    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();

        if (recipientName != null) {
            sb.append(recipientName).append("\n");
        }
        if (companyName != null && !companyName.trim().isEmpty()) {
            sb.append(companyName).append("\n");
        }
        if (streetAddress != null) {
            sb.append(streetAddress).append("\n");
        }
        if (addressLine2 != null && !addressLine2.trim().isEmpty()) {
            sb.append(addressLine2).append("\n");
        }

        if (city != null) {
            sb.append(city);
        }
        if (state != null || stateCode != null) {
            sb.append(", ").append(stateCode != null ? stateCode : state);
        }
        if (postalCode != null) {
            sb.append(" ").append(postalCode);
        }
        sb.append("\n");

        if (country != null) {
            sb.append(country);
        }

        return sb.toString();
    }

    public String getShippingLabel() {
        StringBuilder sb = new StringBuilder();

        if (streetAddress != null) {
            sb.append(streetAddress);
        }
        if (addressLine2 != null && !addressLine2.trim().isEmpty()) {
            sb.append(" ").append(addressLine2);
        }
        if (city != null) {
            sb.append(", ").append(city);
        }
        if (stateCode != null) {
            sb.append(", ").append(stateCode);
        } else if (state != null) {
            sb.append(", ").append(state);
        }
        if (postalCode != null) {
            sb.append(" ").append(postalCode);
        }
        if (countryCode != null && !"US".equals(countryCode)) {
            sb.append(", ").append(countryCode);
        }

        return sb.toString();
    }

    public boolean isBillingAddress() {
        return "BILLING".equals(addressType) || "BOTH".equals(addressType);
    }

    public boolean isShippingAddress() {
        return "SHIPPING".equals(addressType) || "BOTH".equals(addressType);
    }

    // MAGNUM OPUS: Uses Lombok-generated getters
    public boolean isDefaultAddress() {
        return isDefaultShipping() || isDefaultBilling();
    }

    public boolean isInternational() {
        return countryCode != null && !"US".equals(countryCode);
    }

    public boolean isUSAddress() {
        return "US".equals(countryCode);
    }

    public boolean isComplete() {
        return recipientName != null && !recipientName.trim().isEmpty() &&
                streetAddress != null && !streetAddress.trim().isEmpty() &&
                city != null && !city.trim().isEmpty() &&
                postalCode != null && !postalCode.trim().isEmpty() &&
                countryCode != null && !countryCode.trim().isEmpty() &&
                phoneNumber != null && !phoneNumber.trim().isEmpty();
    }

    public boolean hasCoordinates() {
        return latitude != null && longitude != null;
    }

    public String getDisplayName() {
        StringBuilder sb = new StringBuilder();

        if (recipientName != null) {
            sb.append(recipientName);
        }

        // MAGNUM OPUS: Uses Lombok-generated getters
        if (isDefaultShipping() && isDefaultBilling()) {
            sb.append(" (Default)");
        } else if (isDefaultShipping()) {
            sb.append(" (Default Shipping)");
        } else if (isDefaultBilling()) {
            sb.append(" (Default Billing)");
        }

        if (streetAddress != null) {
            if (sb.length() > 0) sb.append(" - ");
            sb.append(streetAddress);
            if (city != null) {
                sb.append(", ").append(city);
            }
        }

        return sb.toString();
    }

    @AssertTrue(message = "Invalid US ZIP code format")
    private boolean isValidUSZipCode() {
        if (!"US".equals(countryCode)) {
            return true;
        }
        if (postalCode == null) {
            return false;
        }
        return postalCode.matches("^\\d{5}(-\\d{4})?$");
    }

    @AssertTrue(message = "State is required for US addresses")
    private boolean isStateProvidedForUS() {
        if (!"US".equals(countryCode)) {
            return true;
        }
        return (state != null && !state.trim().isEmpty()) ||
                (stateCode != null && !stateCode.trim().isEmpty());
    }
}
