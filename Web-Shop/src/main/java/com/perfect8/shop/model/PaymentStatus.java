package com.perfect8.shop.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Payment status enumeration representing the lifecycle of a payment.
 *
 * Status Flow:
 * PENDING -> AUTHORIZED -> COMPLETED
 *    |           |            |
 *    v           v            v
 * FAILED    CANCELLED    REFUNDED
 */
public enum PaymentStatus {

    PENDING("pending", "Payment Pending", "Payment has been initiated but not processed", 1),
    AUTHORIZED("authorized", "Payment Authorized", "Payment has been authorized but not captured", 2),
    COMPLETED("completed", "Payment Completed", "Payment has been successfully completed", 3),
    FAILED("failed", "Payment Failed", "Payment has failed to process", 4),
    CANCELLED("cancelled", "Payment Cancelled", "Payment has been cancelled", 5),
    REFUNDED("refunded", "Payment Refunded", "Payment has been refunded to customer", 6);

    private final String code;
    private final String displayName;
    private final String description;
    private final int orderSequence;

    PaymentStatus(String code, String displayName, String description, int orderSequence) {
        this.code = code;
        this.displayName = displayName;
        this.description = description;
        this.orderSequence = orderSequence;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public int getOrderSequence() {
        return orderSequence;
    }

    /**
     * Check if this status can transition to the target status
     */
    public boolean canTransitionTo(PaymentStatus targetStatus) {
        if (this == targetStatus) {
            return false; // No transition needed
        }

        return switch (this) {
            case PENDING -> targetStatus == AUTHORIZED || targetStatus == FAILED || targetStatus == CANCELLED;
            case AUTHORIZED -> targetStatus == COMPLETED || targetStatus == CANCELLED || targetStatus == FAILED;
            case COMPLETED -> targetStatus == REFUNDED;
            case FAILED -> targetStatus == PENDING; // Allow retry
            case CANCELLED -> false; // Final state
            case REFUNDED -> false; // Final state
        };
    }

    /**
     * Get all valid next statuses from current status
     */
    public PaymentStatus[] getValidNextStatuses() {
        return switch (this) {
            case PENDING -> new PaymentStatus[]{AUTHORIZED, FAILED, CANCELLED};
            case AUTHORIZED -> new PaymentStatus[]{COMPLETED, CANCELLED, FAILED};
            case COMPLETED -> new PaymentStatus[]{REFUNDED};
            case FAILED -> new PaymentStatus[]{PENDING}; // Allow retry
            case CANCELLED, REFUNDED -> new PaymentStatus[]{}; // No valid transitions
        };
    }

    /**
     * Check if this is a final status (no further transitions possible)
     */
    public boolean isFinalStatus() {
        return this == COMPLETED || this == CANCELLED || this == REFUNDED;
    }

    /**
     * Check if this status indicates successful payment
     */
    public boolean isSuccessfulStatus() {
        return this == COMPLETED;
    }

    /**
     * Check if this status indicates failed payment
     */
    public boolean isFailedStatus() {
        return this == FAILED || this == CANCELLED;
    }

    /**
     * Check if payment can be retried
     */
    public boolean allowsRetry() {
        return this == FAILED;
    }

    /**
     * Check if payment can be refunded
     */
    public boolean allowsRefund() {
        return this == COMPLETED;
    }

    /**
     * Get CSS class for UI styling
     */
    public String getCssClass() {
        return switch (this) {
            case PENDING -> "payment-pending";
            case AUTHORIZED -> "payment-authorized";
            case COMPLETED -> "payment-completed";
            case FAILED -> "payment-failed";
            case CANCELLED -> "payment-cancelled";
            case REFUNDED -> "payment-refunded";
        };
    }

    /**
     * Get Bootstrap color class for UI styling
     */
    public String getBootstrapClass() {
        return switch (this) {
            case PENDING -> "warning";
            case AUTHORIZED -> "info";
            case COMPLETED -> "success";
            case FAILED -> "danger";
            case CANCELLED -> "secondary";
            case REFUNDED -> "primary";
        };
    }

    /**
     * Get emoji representation
     */
    public String getEmoji() {
        return switch (this) {
            case PENDING -> "⏳";
            case AUTHORIZED -> "🔒";
            case COMPLETED -> "✅";
            case FAILED -> "❌";
            case CANCELLED -> "🚫";
            case REFUNDED -> "↩️";
        };
    }

    /**
     * Get progress percentage (0-100) for progress bars
     */
    public int getProgressPercentage() {
        return switch (this) {
            case PENDING -> 25;
            case AUTHORIZED -> 50;
            case COMPLETED -> 100;
            case FAILED, CANCELLED -> 0;
            case REFUNDED -> 100; // Completed process, but refunded
        };
    }

    /**
     * Parse status from string code
     */
    public static PaymentStatus fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return PENDING; // Default status
        }

        for (PaymentStatus status : values()) {
            if (status.code.equalsIgnoreCase(code.trim())) {
                return status;
            }
        }

        throw new IllegalArgumentException("Invalid payment status code: " + code);
    }

    /**
     * Get all active statuses (non-final states)
     */
    public static PaymentStatus[] getActiveStatuses() {
        return new PaymentStatus[]{PENDING, AUTHORIZED};
    }

    /**
     * Get all final statuses
     */
    public static PaymentStatus[] getFinalStatuses() {
        return new PaymentStatus[]{COMPLETED, CANCELLED, REFUNDED};
    }

    /**
     * Get all successful statuses
     */
    public static PaymentStatus[] getSuccessfulStatuses() {
        return new PaymentStatus[]{COMPLETED};
    }

    /**
     * Get all failed statuses
     */
    public static PaymentStatus[] getFailedStatuses() {
        return new PaymentStatus[]{FAILED, CANCELLED};
    }

    @Override
    public String toString() {
        return displayName;
    }
}