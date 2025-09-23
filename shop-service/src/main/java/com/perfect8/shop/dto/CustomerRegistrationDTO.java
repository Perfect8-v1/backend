package com.perfect8.shop.dto;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO for customer registration
 * Version 1.0 - Core registration functionality
 * Field names MUST match Customer entity exactly!
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRegistrationDTO {

    // Required fields - matches Customer entity
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s'-]+$", message = "First name contains invalid characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s'-]+$", message = "Last name contains invalid characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 255, message = "Email address too long")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
            message = "Password must contain at least one lowercase letter, one uppercase letter, and one digit")
    private String password;

    @NotBlank(message = "Password confirmation is required")
    private String confirmPassword;

    // Optional fields - matches Customer entity
    @Pattern(regexp = "^[+]?[0-9\\s\\-()]+$", message = "Invalid phone number format")
    @Size(max = 20, message = "Phone number too long")
    private String phoneNumber;

    // Marketing preferences - EXACT names from Customer entity
    @Builder.Default
    private Boolean newsletterSubscribed = false;  // Was: subscribeToNewsletter

    @Builder.Default
    private Boolean marketingConsent = false;       // Was: acceptMarketingEmails
    // REMOVED: acceptSmsMarketing - not in entity (v2.0 feature)

    // Registration-only fields (not stored in entity but needed for legal compliance)
    @NotNull(message = "You must accept the terms and conditions")
    @AssertTrue(message = "You must accept the terms and conditions")
    private Boolean acceptTermsAndConditions;

    @NotNull(message = "You must accept the privacy policy")
    @AssertTrue(message = "You must accept the privacy policy")
    private Boolean acceptPrivacyPolicy;

    // Preferences - matches Customer entity (optional at registration)
    @Size(max = 5, message = "Language code too long")
    @Builder.Default
    private String preferredLanguage = "en";

    @Size(max = 3, message = "Currency code too long")
    @Builder.Default
    private String preferredCurrency = "USD";

    // ========================================
    // Validation methods
    // ========================================

    /**
     * Validate that passwords match
     */
    @AssertTrue(message = "Passwords do not match")
    public boolean isPasswordMatching() {
        return password != null && password.equals(confirmPassword);
    }

    /**
     * Validate minimum age (if needed for legal compliance)
     * For v1.0, we don't collect dateOfBirth, so this returns true
     */
    public boolean isAgeValid() {
        return true; // v1.0: Age verification not implemented
    }

    // ========================================
    // Utility methods
    // ========================================

    /**
     * Get full name
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Check if any marketing consent was given
     */
    public boolean hasMarketingConsent() {
        return Boolean.TRUE.equals(marketingConsent) ||
                Boolean.TRUE.equals(newsletterSubscribed);
    }

    /**
     * Clear sensitive data after registration
     */
    public void clearSensitiveData() {
        this.password = null;
        this.confirmPassword = null;
    }

    /**
     * Validate all required consents
     */
    public boolean hasRequiredConsents() {
        return Boolean.TRUE.equals(acceptTermsAndConditions) &&
                Boolean.TRUE.equals(acceptPrivacyPolicy);
    }

    @Override
    public String toString() {
        return "CustomerRegistrationDTO{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", newsletterSubscribed=" + newsletterSubscribed +
                ", marketingConsent=" + marketingConsent +
                ", preferredLanguage='" + preferredLanguage + '\'' +
                ", preferredCurrency='" + preferredCurrency + '\'' +
                ", acceptTermsAndConditions=" + acceptTermsAndConditions +
                ", acceptPrivacyPolicy=" + acceptPrivacyPolicy +
                '}';
    }

    // ========================================
    // Version 2.0 features - commented out
    // ========================================

    /**
     * In v2.0, we'll add:
     * - dateOfBirth (for age verification)
     * - acceptSmsMarketing
     * - referralCode
     * - howDidYouHearAboutUs
     * - socialMediaConnections
     * - preferredContactMethod
     * - marketingSegments
     */
}