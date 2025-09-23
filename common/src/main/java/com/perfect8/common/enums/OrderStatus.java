package com.perfect8.common.enums;

/**
 * Order status enum used across all services
 * Version 1.0 - Core statuses for e-commerce flow
 *
 * Updated with COMPLETED and REFUNDED for shop-service
 */
public enum OrderStatus {

    // Initial states
    PENDING,          // Order created, waiting for payment
    PAYMENT_FAILED,   // Payment attempt failed

    // Payment confirmed states
    PAID,             // Payment successful

    // Fulfillment states
    PROCESSING,       // Order being prepared
    SHIPPED,          // Order sent out
    DELIVERED,        // Order delivered to customer
    COMPLETED,        // Order successfully completed (after delivery confirmation)

    // Hold state
    ON_HOLD,          // Order temporarily paused

    // Final/Terminal states
    CANCELLED,        // Order cancelled (before delivery)
    RETURNED,         // Order returned (after delivery)
    REFUNDED;         // Payment refunded to customer

    /**
     * Check if this is a final state (no further transitions possible)
     */
    public boolean isFinalState() {
        return this == COMPLETED ||
                this == CANCELLED ||
                this == RETURNED ||
                this == REFUNDED;
    }

    /**
     * Check if this state requires payment
     */
    public boolean requiresPayment() {
        return this == PENDING;
    }

    /**
     * Check if this state means the order is active
     */
    public boolean isActive() {
        return !isFinalState() && this != PAYMENT_FAILED;
    }

    /**
     * Check if this state means successful completion
     */
    public boolean isSuccessful() {
        return this == DELIVERED || this == COMPLETED;
    }

    /**
     * Check if this state means the order failed or was cancelled
     */
    public boolean isFailed() {
        return this == CANCELLED ||
                this == PAYMENT_FAILED ||
                this == RETURNED ||
                this == REFUNDED;
    }

    /**
     * Get display name for UI
     */
    public String getDisplayName() {
        return switch (this) {
            case PENDING -> "Pending Payment";
            case PAYMENT_FAILED -> "Payment Failed";
            case PAID -> "Payment Confirmed";
            case PROCESSING -> "Processing";
            case SHIPPED -> "Shipped";
            case DELIVERED -> "Delivered";
            case COMPLETED -> "Completed";
            case ON_HOLD -> "On Hold";
            case CANCELLED -> "Cancelled";
            case RETURNED -> "Returned";
            case REFUNDED -> "Refunded";
        };
    }

    /**
     * Get color code for UI (hex color)
     */
    public String getColorCode() {
        return switch (this) {
            case PENDING -> "#FFA500";        // Orange
            case PAYMENT_FAILED -> "#DC143C"; // Crimson
            case PAID -> "#32CD32";           // Lime Green
            case PROCESSING -> "#4169E1";     // Royal Blue
            case SHIPPED -> "#00CED1";        // Dark Turquoise
            case DELIVERED -> "#228B22";      // Forest Green
            case COMPLETED -> "#008000";      // Green
            case ON_HOLD -> "#FFD700";        // Gold
            case CANCELLED -> "#A9A9A9";      // Dark Gray
            case RETURNED -> "#8B4513";       // Saddle Brown
            case REFUNDED -> "#9370DB";       // Medium Purple
        };
    }

    /**
     * Get priority for sorting (lower number = higher priority)
     */
    public int getPriority() {
        return switch (this) {
            case PAYMENT_FAILED -> 0;
            case ON_HOLD -> 1;
            case PENDING -> 2;
            case PAID -> 3;
            case PROCESSING -> 4;
            case SHIPPED -> 5;
            case DELIVERED -> 6;
            case COMPLETED -> 7;
            case CANCELLED, RETURNED, REFUNDED -> 9;
        };
    }
}