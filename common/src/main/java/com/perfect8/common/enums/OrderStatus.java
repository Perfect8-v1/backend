package com.perfect8.common.enums;

/**
 * Order Status Enum - Version 1.0
 * Simple enum for order status values shared across services
 *
 * IMPORTANT: Keep this enum simple!
 * Complex logic belongs in shop-service's OrderStatusHelper
 */
public enum OrderStatus {

    // Order lifecycle states
    PENDING,         // Order created, awaiting payment
    PAID,           // Payment confirmed
    PROCESSING,     // Order being prepared
    SHIPPED,        // Order shipped to customer
    DELIVERED,      // Order delivered to customer

    // Alternative end states
    CANCELLED,      // Order cancelled
    RETURNED,       // Order returned by customer

    // Error states
    PAYMENT_FAILED, // Payment processing failed
    ON_HOLD;        // Order on hold (manual intervention needed)

    /**
     * Check if this status represents a completed order
     * (Delivered or successfully processed)
     */
    public boolean isCompleted() {
        return this == DELIVERED;
    }

    /**
     * Check if this status represents a cancelled/failed order
     */
    public boolean isCancelled() {
        return this == CANCELLED || this == RETURNED || this == PAYMENT_FAILED;
    }

    /**
     * Check if order is in progress
     */
    public boolean isInProgress() {
        return this == PENDING || this == PAID || this == PROCESSING || this == SHIPPED;
    }

    /**
     * Check if payment is required
     */
    public boolean requiresPayment() {
        return this == PENDING;
    }
}