package com.perfect8.shop.enums;

/**
 * Enum representing the various states of an order in the e-commerce system.
 * Version 1.0 - Core order lifecycle management
 */
public enum OrderStatus {

    /**
     * Order has been created but payment not yet processed
     */
    PENDING("Pending", "Order is waiting for payment processing"),

    /**
     * Payment has been successfully processed
     */
    CONFIRMED("Confirmed", "Order has been confirmed and payment received"),

    /**
     * Order is being prepared for shipment
     */
    PROCESSING("Processing", "Order is being processed and prepared"),

    /**
     * Order has been shipped to customer
     */
    SHIPPED("Shipped", "Order has been shipped"),

    /**
     * Order has been delivered to customer
     */
    DELIVERED("Delivered", "Order has been successfully delivered"),

    /**
     * Order has been cancelled before shipment
     */
    CANCELLED("Cancelled", "Order has been cancelled"),

    /**
     * Order has been returned by customer
     */
    RETURNED("Returned", "Order has been returned"),

    /**
     * Payment failed or other payment issues
     */
    PAYMENT_FAILED("Payment Failed", "Payment processing failed"),

    /**
     * Order is on hold due to various reasons
     */
    ON_HOLD("On Hold", "Order is temporarily on hold");

    private final String displayName;
    private final String description;

    OrderStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Check if transition to another status is allowed
     * @param newStatus the target status
     * @return true if transition is allowed
     */
    public boolean canTransitionTo(OrderStatus newStatus) {
        if (this == newStatus) {
            return true; // Same status is always allowed
        }

        switch (this) {
            case PENDING:
                return newStatus == CONFIRMED ||
                        newStatus == PAYMENT_FAILED ||
                        newStatus == CANCELLED ||
                        newStatus == ON_HOLD;

            case CONFIRMED:
                return newStatus == PROCESSING ||
                        newStatus == CANCELLED ||
                        newStatus == ON_HOLD;

            case PROCESSING:
                return newStatus == SHIPPED ||
                        newStatus == CANCELLED ||
                        newStatus == ON_HOLD;

            case SHIPPED:
                return newStatus == DELIVERED ||
                        newStatus == RETURNED;

            case DELIVERED:
                return newStatus == RETURNED;

            case ON_HOLD:
                return newStatus == PROCESSING ||
                        newStatus == CANCELLED ||
                        newStatus == CONFIRMED;

            case PAYMENT_FAILED:
                return newStatus == PENDING ||
                        newStatus == CANCELLED;

            case CANCELLED:
            case RETURNED:
                return false; // Final states, no transitions allowed

            default:
                return false;
        }
    }

    /**
     * Check if the order status allows cancellation
     * @return true if order can be cancelled in this status
     */
    public boolean canBeCancelled() {
        return this == PENDING ||
                this == CONFIRMED ||
                this == PROCESSING ||
                this == ON_HOLD;
    }

    /**
     * Check if the order status allows returns
     * @return true if order can be returned in this status
     */
    public boolean canBeReturned() {
        return this == DELIVERED;
    }

    /**
     * Check if the order is in a final state (cannot be modified)
     * @return true if order is in final state
     */
    public boolean isFinalState() {
        return this == DELIVERED ||
                this == CANCELLED ||
                this == RETURNED;
    }

    /**
     * Check if this status requires payment
     * @return true if payment is required
     */
    public boolean requiresPayment() {
        return this == PENDING;
    }

    /**
     * Check if this status indicates successful payment
     * @return true if payment was successful
     */
    public boolean isPaymentSuccessful() {
        return this == CONFIRMED ||
                this == PROCESSING ||
                this == SHIPPED ||
                this == DELIVERED;
    }

    /**
     * Check if this status indicates order is in fulfillment
     * @return true if order is being fulfilled
     */
    public boolean isInFulfillment() {
        return this == PROCESSING ||
                this == SHIPPED;
    }

    /**
     * Get next possible statuses from current status
     * @return array of possible next statuses
     */
    public OrderStatus[] getNextPossibleStatuses() {
        switch (this) {
            case PENDING:
                return new OrderStatus[]{CONFIRMED, PAYMENT_FAILED, CANCELLED, ON_HOLD};
            case CONFIRMED:
                return new OrderStatus[]{PROCESSING, ON_HOLD, CANCELLED};
            case PROCESSING:
                return new OrderStatus[]{SHIPPED, ON_HOLD, CANCELLED};
            case SHIPPED:
                return new OrderStatus[]{DELIVERED, RETURNED};
            case DELIVERED:
                return new OrderStatus[]{RETURNED};
            case ON_HOLD:
                return new OrderStatus[]{PROCESSING, CONFIRMED, CANCELLED};
            case PAYMENT_FAILED:
                return new OrderStatus[]{PENDING, CANCELLED};
            default:
                return new OrderStatus[0]; // Final states have no next status
        }
    }

    /**
     * Get OrderStatus from string value (case-insensitive)
     * @param status string representation of status
     * @return OrderStatus enum value or null if not found
     */
    public static OrderStatus fromString(String status) {
        if (status == null || status.trim().isEmpty()) {
            return null;
        }

        try {
            // Try exact match first
            return OrderStatus.valueOf(status.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            // Try to match by display name
            for (OrderStatus orderStatus : OrderStatus.values()) {
                if (orderStatus.getDisplayName().equalsIgnoreCase(status)) {
                    return orderStatus;
                }
            }
            return null;
        }
    }

    /**
     * Check if status string is valid
     * @param status string to check
     * @return true if valid status
     */
    public static boolean isValidStatus(String status) {
        return fromString(status) != null;
    }

    /**
     * Get all active statuses (non-final)
     * @return array of active statuses
     */
    public static OrderStatus[] getActiveStatuses() {
        return new OrderStatus[]{
                PENDING, CONFIRMED, PROCESSING, SHIPPED, ON_HOLD, PAYMENT_FAILED
        };
    }

    /**
     * Get all final statuses
     * @return array of final statuses
     */
    public static OrderStatus[] getFinalStatuses() {
        return new OrderStatus[]{
                DELIVERED, CANCELLED, RETURNED
        };
    }

    @Override
    public String toString() {
        return displayName;
    }
}