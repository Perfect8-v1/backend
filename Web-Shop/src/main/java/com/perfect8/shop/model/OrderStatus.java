package com.perfect8.shop.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Order status enumeration representing the lifecycle of an order.
 *
 * Status Flow:
 * PENDING -> CONFIRMED -> SHIPPED -> DELIVERED
 *    |                       |
 *    v                       v
 * CANCELLED            CANCELLED
 */
public enum OrderStatus {

    PENDING("pending", "Order Pending", "Order has been created but not yet confirmed", 1),
    CONFIRMED("confirmed", "Order Confirmed", "Order has been confirmed and payment processed", 2),
    SHIPPED("shipped", "Order Shipped", "Order has been shipped to customer", 3),
    DELIVERED("delivered", "Order Delivered", "Order has been successfully delivered", 4),
    CANCELLED("cancelled", "Order Cancelled", "Order has been cancelled", 5);

    private final String code;
    private final String displayName;
    private final String description;
    private final int orderSequence;

    OrderStatus(String code, String displayName, String description, int orderSequence) {
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
    public boolean canTransitionTo(OrderStatus targetStatus) {
        if (this == targetStatus) {
            return false; // No transition needed
        }

        return switch (this) {
            case PENDING -> targetStatus == CONFIRMED || targetStatus == CANCELLED;
            case CONFIRMED -> targetStatus == SHIPPED || targetStatus == CANCELLED;
            case SHIPPED -> targetStatus == DELIVERED || targetStatus == CANCELLED;
            case DELIVERED -> false; // Final state - no transitions allowed
            case CANCELLED -> false; // Final state - no transitions allowed
        };
    }

    /**
     * Get all valid next statuses from current status
     */
    public OrderStatus[] getValidNextStatuses() {
        return switch (this) {
            case PENDING -> new OrderStatus[]{CONFIRMED, CANCELLED};
            case CONFIRMED -> new OrderStatus[]{SHIPPED, CANCELLED};
            case SHIPPED -> new OrderStatus[]{DELIVERED, CANCELLED};
            case DELIVERED, CANCELLED -> new OrderStatus[]{}; // No valid transitions
        };
    }

    /**
     * Check if this is a final status (no further transitions possible)
     */
    public boolean isFinalStatus() {
        return this == DELIVERED || this == CANCELLED;
    }

    /**
     * Check if this status indicates the order is active (not cancelled/delivered)
     */
    public boolean isActiveStatus() {
        return this == PENDING || this == CONFIRMED || this == SHIPPED;
    }

    /**
     * Check if this status indicates the order is completed successfully
     */
    public boolean isCompletedStatus() {
        return this == DELIVERED;
    }

    /**
     * Check if this status indicates the order was cancelled
     */
    public boolean isCancelledStatus() {
        return this == CANCELLED;
    }

    /**
     * Check if payment should be processed for this status
     */
    public boolean requiresPayment() {
        return this == CONFIRMED;
    }

    /**
     * Check if inventory should be reserved for this status
     */
    public boolean requiresInventoryReservation() {
        return this == PENDING || this == CONFIRMED || this == SHIPPED;
    }

    /**
     * Check if shipping information is needed for this status
     */
    public boolean requiresShippingInfo() {
        return this == SHIPPED || this == DELIVERED;
    }

    /**
     * Get CSS class for UI styling
     */
    public String getCssClass() {
        return switch (this) {
            case PENDING -> "status-pending";
            case CONFIRMED -> "status-confirmed";
            case SHIPPED -> "status-shipped";
            case DELIVERED -> "status-delivered";
            case CANCELLED -> "status-cancelled";
        };
    }

    /**
     * Get Bootstrap color class for UI styling
     */
    public String getBootstrapClass() {
        return switch (this) {
            case PENDING -> "warning";
            case CONFIRMED -> "info";
            case SHIPPED -> "primary";
            case DELIVERED -> "success";
            case CANCELLED -> "danger";
        };
    }

    /**
     * Get emoji representation for fun UI elements
     */
    public String getEmoji() {
        return switch (this) {
            case PENDING -> "⏳";
            case CONFIRMED -> "✅";
            case SHIPPED -> "🚚";
            case DELIVERED -> "📦";
            case CANCELLED -> "❌";
        };
    }

    /**
     * Get progress percentage (0-100) for progress bars
     */
    public int getProgressPercentage() {
        return switch (this) {
            case PENDING -> 25;
            case CONFIRMED -> 50;
            case SHIPPED -> 75;
            case DELIVERED -> 100;
            case CANCELLED -> 0;
        };
    }

    /**
     * Parse status from string code
     */
    public static OrderStatus fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return PENDING; // Default status
        }

        for (OrderStatus status : values()) {
            if (status.code.equalsIgnoreCase(code.trim())) {
                return status;
            }
        }

        throw new IllegalArgumentException("Invalid order status code: " + code);
    }

    /**
     * Parse status from display name
     */
    public static OrderStatus fromDisplayName(String displayName) {
        if (displayName == null || displayName.trim().isEmpty()) {
            return PENDING;
        }

        for (OrderStatus status : values()) {
            if (status.displayName.equalsIgnoreCase(displayName.trim())) {
                return status;
            }
        }

        throw new IllegalArgumentException("Invalid order status display name: " + displayName);
    }

    /**
     * Get all active statuses (excluding final states)
     */
    public static OrderStatus[] getActiveStatuses() {
        return new OrderStatus[]{PENDING, CONFIRMED, SHIPPED};
    }

    /**
     * Get all final statuses
     */
    public static OrderStatus[] getFinalStatuses() {
        return new OrderStatus[]{DELIVERED, CANCELLED};
    }

    @Override
    public String toString() {
        return displayName;
    }
}