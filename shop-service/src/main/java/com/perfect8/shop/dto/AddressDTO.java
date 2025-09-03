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
 * Enhanced validation to prevent delivery failures
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Address ID - null for new addresses
     */
    private Long id;

    /**
     * Address type: BILLING, SHIPPING, or BOTH
     */
    @NotNull(message = "Address type is required")
    @Pattern(regexp = "^(BILLING|SHIPPING|BOTH)$",
            message = "Address type must be BILLING, SHIPPING, or BOTH")
    @Builder.Default
    private String addressType = "BOTH";

    /**
     * Recipient full name - Critical for delivery!
     */
    @NotBlank(message = "Recipient name is required for delivery")
    @Size(min = 2, max = 100, message = "Recipient name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s\\-'.]+$",
            message = "Recipient name contains invalid characters")
    private String recipientName;

    /**
     * Company name (optional)
     */
    @Size(max = 100, message = "Company name cannot exceed 100 characters")
    private String companyName;

    /**
     * Street address line 1 - Required
     */
    @NotBlank(message = "Street address is required")
    @Size(min = 5, max = 255, message = "Street address must be between 5 and 255 characters")
    private String streetAddress;

    /**
     * Street address line 2 - Optional (apartment, suite, etc.)
     */
    @Size(max = 255, message = "Address line 2 cannot exceed 255 characters")
    private String addressLine2;

    /**
     * City - Required
     */
    @NotBlank(message = "City is required")
    @Size(min = 2, max = 100, message = "City must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s\\-'.]+$",
            message = "City name contains invalid characters")
    private String city;

    /**
     * State/Province/Region
     * Required for US addresses
     */
    @Size(min = 2, max = 100, message = "State/Province must be between 2 and 100 characters")
    private String state;

    /**
     * State code (e.g., CA, NY) for US addresses
     */
    @Pattern(regexp = "^[A-Z]{2}$|^$",
            message = "State code must be 2 uppercase letters")
    private String stateCode;

    /**
     * Postal/ZIP code - Required
     * Enhanced validation for common formats
     */
    @NotBlank(message = "Postal code is required")
    @Pattern(regexp = "^[A-Z0-9\\s\\-]{3,10}$",
            message = "Invalid postal code format")
    private String postalCode;

    /**
     * Country - Required
     */
    @NotBlank(message = "Country is required")
    @Size(min = 2, max = 100, message = "Country must be between 2 and 100 characters")
    private String country;

    /**
     * ISO country code (e.g., US, CA, GB)
     * Critical for international shipping!
     */
    @NotBlank(message = "Country code is required for shipping calculations")
    @Pattern(regexp = "^[A-Z]{2}$",
            message = "Country code must be 2 uppercase letters (ISO format)")
    private String countryCode;

    /**
     * Contact phone number - Critical for delivery issues!
     */
    @NotBlank(message = "Phone number is required for delivery coordination")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$",
            message = "Invalid phone number format (use international format, e.g., +1234567890)")
    private String phoneNumber;

    /**
     * Email for delivery notifications
     */
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email cannot exceed 255 characters")
    private String emailAddress;

    /**
     * Delivery instructions (gate code, building access, etc.)
     * Important for successful delivery!
     */
    @Size(max = 500, message = "Delivery instructions cannot exceed 500 characters")
    private String deliveryInstructions;

    /**
     * Whether this is the default address
     */
    @Builder.Default
    private boolean defaultAddress = false;

    /**
     * Whether address has been verified
     * Critical for reducing failed deliveries!
     */
    @Builder.Default
    private boolean verified = false;

    /**
     * Verification date/time
     */
    private String verifiedAt;

    /**
     * Address verification method (e.g., "MANUAL", "API", "GOOGLE")
     */
    private String verificationMethod;

    /**
     * Latitude for precise delivery location
     */
    @DecimalMin(value = "-90.0", message = "Invalid latitude")
    @DecimalMax(value = "90.0", message = "Invalid latitude")
    private Double latitude;

    /**
     * Longitude for precise delivery location
     */
    @DecimalMin(value = "-180.0", message = "Invalid longitude")
    @DecimalMax(value = "180.0", message = "Invalid longitude")
    private Double longitude;

    /**
     * Residential flag - affects shipping costs and delivery times
     */
    @Builder.Default
    private boolean residential = true;

    /**
     * Whether address requires signature on delivery
     */
    @Builder.Default
    private boolean signatureRequired = false;

    /**
     * Custom ID from customer's system
     */
    @Size(max = 100, message = "External ID cannot exceed 100 characters")
    private String externalId;

    // Utility methods

    /**
     * Get formatted full address for display
     */
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

        // City, State ZIP format for US
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

    /**
     * Get single-line address for labels
     */
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

    /**
     * Check if this is a billing address
     */
    public boolean isBillingAddress() {
        return "BILLING".equals(addressType) || "BOTH".equals(addressType);
    }

    /**
     * Check if this is a shipping address
     */
    public boolean isShippingAddress() {
        return "SHIPPING".equals(addressType) || "BOTH".equals(addressType);
    }

    /**
     * Check if address is international (non-US)
     */
    public boolean isInternational() {
        return countryCode != null && !"US".equals(countryCode);
    }

    /**
     * Check if address is for US
     */
    public boolean isUSAddress() {
        return "US".equals(countryCode);
    }

    /**
     * Check if address appears complete
     */
    public boolean isComplete() {
        return recipientName != null && !recipientName.trim().isEmpty() &&
                streetAddress != null && !streetAddress.trim().isEmpty() &&
                city != null && !city.trim().isEmpty() &&
                postalCode != null && !postalCode.trim().isEmpty() &&
                countryCode != null && !countryCode.trim().isEmpty() &&
                phoneNumber != null && !phoneNumber.trim().isEmpty();
    }

    /**
     * Check if coordinates are available
     */
    public boolean hasCoordinates() {
        return latitude != null && longitude != null;
    }

    /**
     * Get display name for address selection
     */
    public String getDisplayName() {
        StringBuilder sb = new StringBuilder();

        if (recipientName != null) {
            sb.append(recipientName);
        }

        if (defaultAddress) {
            sb.append(" (Default)");
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

    /**
     * Validate US ZIP code format
     */
    @AssertTrue(message = "Invalid US ZIP code format")
    private boolean isValidUSZipCode() {
        if (!"US".equals(countryCode)) {
            return true; // Not a US address
        }
        if (postalCode == null) {
            return false;
        }
        // US ZIP: 12345 or 12345-6789
        return postalCode.matches("^\\d{5}(-\\d{4})?$");
    }

    /**
     * Validate state is provided for US addresses
     */
    @AssertTrue(message = "State is required for US addresses")
    private boolean isStateProvidedForUS() {
        if (!"US".equals(countryCode)) {
            return true; // Not a US address
        }
        return (state != null && !state.trim().isEmpty()) ||
                (stateCode != null && !stateCode.trim().isEmpty());
    }

    // Version 2.0 fields - commented out for future implementation
    /*
    // Address validation & standardization - Version 2.0
    private String standardizedAddress;
    private String validationStatus;
    private String validationErrors;
    private Double deliveryConfidence;

    // Delivery preferences - Version 2.0
    private String preferredDeliveryTime;
    private List<String> deliveryBlackoutDates;
    private boolean weekendDelivery;
    private String accessCode;

    // Address metadata - Version 2.0
    private String timezone;
    private String county;
    private String neighborhood;
    private String buildingType; // house, apartment, business
    */
}