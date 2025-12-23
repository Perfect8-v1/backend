package com.perfect8.shop.dto;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO for updating customer information
 * Version 1.0 - Core customer update functionality
 * Field names MUST match Customer entity exactly!
 *
 * PEDAGOGICAL NOTE for GitHub portfolio:
 * Uses 'passwordHash' instead of 'password' to make it clear that passwords
 * are ALREADY HASHED by the frontend before transmission.
 * This demonstrates security awareness and prevents misconceptions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerUpdateDTO {

    // Basic information - matches Customer entity
    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s'-]+$", message = "First name contains invalid characters")
    private String firstName;

    @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s'-]+$", message = "Last name contains invalid characters")
    private String lastName;

    @Pattern(regexp = "^[+]?[0-9\\s\\-()]+$", message = "Invalid phone format")
    @Size(max = 20, message = "Phone too long")
    private String phone;  // FIXED: phoneNumber → phone (matches Customer.phone)

    // Marketing preferences - EXACT names from Customer entity
    private Boolean newsletterSubscribed;
    private Boolean marketingConsent;

    // Preferences - matches Customer entity
    @Size(max = 5, message = "Language code too long")
    private String preferredLanguage;

    @Size(max = 3, message = "Currency code too long")
    private String preferredCurrency;

    // Password update fields (not in entity but needed for update operation)
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
            message = "Password must contain at least one lowercase letter, one uppercase letter, and one digit")
    private String newPasswordHash;  // CHANGED: newPassword → newPasswordHash

    @Size(min = 8, max = 100, message = "Password confirmation must be between 8 and 100 characters")
    private String confirmNewPasswordHash;  // CHANGED: confirmNewPassword → confirmNewPasswordHash

    // Note: @NotNull removed - isPasswordChangeValid() handles conditional validation
    @Size(max = 100, message = "Current password hash too long")
    private String currentPasswordHash;  // CHANGED: currentPassword → currentPasswordHash

    // Internal notes (admin use only)
    @Size(max = 5000, message = "Internal notes too long")
    private String internalNotes;

    // ========================================
    // Validation methods
    // ========================================

    /**
     * Validate that new password hashes match
     */
    @AssertTrue(message = "New password hashes do not match")
    public boolean isNewPasswordMatching() {
        if (newPasswordHash == null && confirmNewPasswordHash == null) {
            return true; // No password change requested
        }
        return newPasswordHash != null && newPasswordHash.equals(confirmNewPasswordHash);
    }

    /**
     * Check if password change is requested
     */
    public boolean isPasswordChangeRequested() {
        return newPasswordHash != null && !newPasswordHash.trim().isEmpty();
    }

    /**
     * Validate password change request
     */
    @AssertTrue(message = "Current password hash required for password change")
    public boolean isPasswordChangeValid() {
        if (isPasswordChangeRequested()) {
            return currentPasswordHash != null && !currentPasswordHash.trim().isEmpty();
        }
        return true; // No validation needed if no password change
    }

    // ========================================
    // Utility methods
    // ========================================

    /**
     * Check if any marketing preferences are being updated
     */
    public boolean hasMarketingPreferenceChanges() {
        return newsletterSubscribed != null || marketingConsent != null;
    }

    /**
     * Check if any profile fields are being updated
     */
    public boolean hasProfileChanges() {
        return firstName != null ||
                lastName != null ||
                phone != null;
    }

    /**
     * Check if any preferences are being updated
     */
    public boolean hasPreferenceChanges() {
        return preferredLanguage != null ||
                preferredCurrency != null;
    }

    /**
     * Get full name if both parts are provided
     */
    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        }
        return null;
    }

    /**
     * Clear sensitive password data
     */
    public void clearSensitiveData() {
        this.newPasswordHash = null;
        this.confirmNewPasswordHash = null;
        this.currentPasswordHash = null;
    }

    /**
     * Check if any field is being updated
     */
    public boolean hasAnyChanges() {
        return hasProfileChanges() ||
                hasMarketingPreferenceChanges() ||
                hasPreferenceChanges() ||
                isPasswordChangeRequested() ||
                internalNotes != null;
    }

    @Override
    public String toString() {
        return "CustomerUpdateDTO{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", phone='" + phone + '\'' +
                ", newsletterSubscribed=" + newsletterSubscribed +
                ", marketingConsent=" + marketingConsent +
                ", preferredLanguage='" + preferredLanguage + '\'' +
                ", preferredCurrency='" + preferredCurrency + '\'' +
                ", hasPasswordChange=" + isPasswordChangeRequested() +
                ", hasInternalNotes=" + (internalNotes != null) +
                '}';
    }

    // ========================================
    // Version 2.0 features - commented out
    // ========================================

    /**
     * In v2.0, we'll add:
     * - dateOfBirth
     * - gender
     * - occupation
     * - bio
     * - timezone
     * - emailOrderConfirmations
     * - smsOrderUpdates
     * - emailPromotions
     * - acceptSmsMarketing
     * - socialMediaHandles
     * - communicationPreferences
     */
}