package com.perfect8.shop.enums;

/**
 * Payment status enumeration for version 1.0
 * Represents the various states a payment can be in
 */
public enum PaymentStatus {
    PENDING("Pending"),
    PROCESSING("Processing"),
    COMPLETED("Completed"),
    FAILED("Failed"),
    CANCELLED("Cancelled"),
    REFUNDED("Refunded"),
    PARTIALLY_REFUNDED("Partially Refunded");

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Convert string to PaymentStatus
     * Handles both enum name and display name
     */
    public static PaymentStatus fromString(String statusString) {
        if (statusString == null) {
            return PENDING;
        }

        // Try exact enum match first
        try {
            return PaymentStatus.valueOf(statusString.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Try display name match
            for (PaymentStatus status : PaymentStatus.values()) {
                if (status.displayName.equalsIgnoreCase(statusString)) {
                    return status;
                }
            }
            // Default to pending if no match
            return PENDING;
        }
    }

    /**
     * Check if payment is in a final state
     */
    public boolean isFinalState() {
        return this == COMPLETED || this == FAILED ||
                this == CANCELLED || this == REFUNDED;
    }

    /**
     * Check if payment can be refunded
     */
    public boolean isRefundable() {
        return this == COMPLETED || this == PARTIALLY_REFUNDED;
    }
}