package com.perfect8.common.enums;

/**
 * Item status enum for order items
 * Used across shop-service and potentially other services
 * Version 1.0 - Core statuses for order item lifecycle
 */
public enum ItemStatus {

    // Initial state
    PENDING,          // Item added to order, not yet processed

    // Processing states
    PROCESSING,       // Item being prepared
    PARTIALLY_SHIPPED, // Some quantity shipped
    SHIPPED,          // Item fully shipped

    // Final states
    DELIVERED,        // Item delivered to customer
    CANCELLED,        // Item cancelled
    RETURNED,         // Item returned by customer
    REFUNDED;         // Item refunded

    /**
     * Check if this is a final state
     */
    public boolean isFinalState() {
        return this == DELIVERED ||
                this == CANCELLED ||
                this == RETURNED ||
                this == REFUNDED;
    }

    /**
     * Check if item is in active processing
     */
    public boolean isActive() {
        return this == PENDING ||
                this == PROCESSING ||
                this == PARTIALLY_SHIPPED;
    }

    /**
     * Check if item has been shipped (fully or partially)
     */
    public boolean isShipped() {
        return this == PARTIALLY_SHIPPED ||
                this == SHIPPED ||
                this == DELIVERED;
    }

    /**
     * Check if item can be cancelled
     */
    public boolean canBeCancelled() {
        return this == PENDING || this == PROCESSING;
    }

    /**
     * Check if item can be returned
     */
    public boolean canBeReturned() {
        return this == DELIVERED;
    }

    /**
     * Get display name for UI
     */
    public String getDisplayName() {
        return switch (this) {
            case PENDING -> "Pending";
            case PROCESSING -> "Processing";
            case PARTIALLY_SHIPPED -> "Partially Shipped";
            case SHIPPED -> "Shipped";
            case DELIVERED -> "Delivered";
            case CANCELLED -> "Cancelled";
            case RETURNED -> "Returned";
            case REFUNDED -> "Refunded";
        };
    }

    /**
     * Get color code for UI
     */
    public String getColorCode() {
        return switch (this) {
            case PENDING -> "#FFA500";        // Orange
            case PROCESSING -> "#4169E1";     // Royal Blue
            case PARTIALLY_SHIPPED -> "#00CED1"; // Dark Turquoise
            case SHIPPED -> "#20B2AA";        // Light Sea Green
            case DELIVERED -> "#228B22";      // Forest Green
            case CANCELLED -> "#A9A9A9";      // Dark Gray
            case RETURNED -> "#8B4513";       // Saddle Brown
            case REFUNDED -> "#9370DB";       // Medium Purple
        };
    }
}