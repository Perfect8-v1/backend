package com.perfect8.email.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Customer DTO for email service
 * Contains only the customer information needed for sending emails
 * Version 1.0 - Enhanced with error handling support
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerEmailDTO {

    private Long customerEmailDTOId;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String preferredLanguage;
    private boolean acceptsMarketing;
    private boolean active;

    // Error handling for fallback scenarios
    private String errorMessage;

    // Additional fields for email context
    private String customerSegment; // VIP, REGULAR, NEW
    private String preferredContactTime; // MORNING, AFTERNOON, EVENING
    private boolean subscribedToNewsletter;

    // Computed fields
    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        }
        return "";
    }

    public String getDisplayName() {
        if (firstName != null && !firstName.trim().isEmpty()) {
            return firstName;
        }
        String fullName = getFullName();
        return !fullName.isEmpty() ? fullName : "Valued Customer";
    }

    public boolean hasValidEmail() {
        return email != null &&
                email.contains("@") &&
                email.contains(".") &&
                email.length() > 5;
    }

    public boolean canReceiveMarketing() {
        return hasValidEmail() && acceptsMarketing && active;
    }

    public boolean canReceiveNewsletter() {
        return hasValidEmail() && subscribedToNewsletter && active;
    }

    public String getPreferredLanguage() {
        return preferredLanguage != null ? preferredLanguage : "en";
    }

    public boolean isVip() {
        return "VIP".equalsIgnoreCase(customerSegment);
    }

    public boolean hasError() {
        return errorMessage != null && !errorMessage.isEmpty();
    }
}