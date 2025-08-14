package com.perfect8.shop.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Shipment status enumeration representing the lifecycle of a shipment.
 *
 * Status Flow:
 * PENDING -> PREPARED -> SHIPPED -> IN_TRANSIT -> OUT_FOR_DELIVERY -> DELIVERED
 *    |         |                                          |
 *    v         v                                          v
 * CANCELLED CANCELLED                                 RETURNED
 */
public enum ShipmentStatus {

    PENDING("pending", "Shipment Pending", "Shipment is being prepared", 1),
    PREPARED("prepared", "Shipment Prepared", "Package is ready for pickup", 2),
    SHIPPED("shipped", "Shipment Shipped", "Package has been picked up by carrier", 3),
    IN_TRANSIT("in_transit", "In Transit", "Package is on its way to destination", 4),
    OUT_FOR_DELIVERY("out_for_delivery", "Out for Delivery", "Package is out for delivery today", 5),
    DELIVERED("delivered", "Delivered", "Package has been successfully delivered", 6),
    RETURNED("returned", "Returned to Sender", "Package has been returned to sender", 7),
    CANCELLED("cancelled", "Shipment Cancelled", "Shipment has been cancelled", 8);

    private final String code;
    private final String displayName;
    private final String description;
    private final int orderSequence;

    ShipmentStatus(String code, String displayName, String description, int orderSequence) {
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
    public boolean canTransitionTo(ShipmentStatus targetStatus) {
        if (this == targetStatus) {
            return false; // No transition needed
        }

        return switch (this) {
            case PENDING -> targetStatus == PREPARED || targetStatus == CANCELLED;
            case PREPARED -> targetStatus == SHIPPED || targetStatus == CANCELLED;
            case SHIPPED -> targetStatus == IN_TRANSIT;
            case IN_TRANSIT -> targetStatus == OUT_FOR_DELIVERY || targetStatus == RETURNED;
            case OUT_FOR_DELIVERY -> targetStatus == DELIVERED || targetStatus == RETURNED;
            case DELIVERED -> false; // Final state
            case RETURNED -> false; // Final state
            case CANCELLED -> false; // Final state
        };
    }

    /**
     * Get all valid next statuses from current status
     */
    public ShipmentStatus[] getValidNextStatuses() {
        return switch (this) {
            case PENDING -> new ShipmentStatus[]{PREPARED, CANCELLED};
            case PREPARED -> new ShipmentStatus[]{SHIPPED, CANCELLED};
            case SHIPPED -> new ShipmentStatus[]{IN_TRANSIT};
            case IN_TRANSIT -> new ShipmentStatus[]{OUT_FOR_DELIVERY, RETURNED};
            case OUT_FOR_DELIVERY -> new ShipmentStatus[]{DELIVERED, RETURNED};
            case DELIVERED, RETURNED, CANCELLED -> new ShipmentStatus[]{}; // No valid transitions
        };
    }

    /**
     * Check if this is a final status (no further transitions possible)
     */
    public boolean isFinalStatus() {
        return this == DELIVERED || this == RETURNED || this == CANCELLED;
    }

    /**
     * Check if this status indicates successful delivery
     */
    public boolean isDeliveredStatus() {
        return this == DELIVERED;
    }

    /**
     * Check if shipment is currently moving
     */
    public boolean isInTransitStatus() {
        return this == SHIPPED || this == IN_TRANSIT || this == OUT_FOR_DELIVERY;
    }

    /**
     * Check if shipment can be tracked
     */
    public boolean isTrackable() {
        return isInTransitStatus() || this == DELIVERED;
    }

    /**
     * Check if customer should be notified about this status
     */
    public boolean requiresCustomerNotification() {
        return this == SHIPPED || this == OUT_FOR_DELIVERY || this == DELIVERED || this == RETURNED;
    }

    /**
     * Get CSS class for UI styling
     */
    public String getCssClass() {
        return switch (this) {
            case PENDING -> "shipment-pending";
            case PREPARED -> "shipment-prepared";
            case SHIPPED -> "shipment-shipped";
            case IN_TRANSIT -> "shipment-in-transit";
            case OUT_FOR_DELIVERY -> "shipment-out-for-delivery";
            case DELIVERED -> "shipment-delivered";
            case RETURNED -> "shipment-returned";
            case CANCELLED -> "shipment-cancelled";
        };
    }

    /**
     * Get Bootstrap color class for UI styling
     */
    public String getBootstrapClass() {
        return switch (this) {
            case PENDING -> "secondary";
            case PREPARED -> "info";
            case SHIPPED -> "primary";
            case IN_TRANSIT -> "warning";
            case OUT_FOR_DELIVERY -> "warning";
            case DELIVERED -> "success";
            case RETURNED -> "danger";
            case CANCELLED -> "dark";
        };
    }

    /**
     * Get emoji representation
     */
    public String getEmoji() {
        return switch (this) {
            case PENDING -> "📋";
            case PREPARED -> "📦";
            case SHIPPED -> "🚚";
            case IN_TRANSIT -> "🛣️";
            case OUT_FOR_DELIVERY -> "🚪";
            case DELIVERED -> "✅";
            case RETURNED -> "↩️";
            case CANCELLED -> "❌";
        };
    }

    /**
     * Get progress percentage (0-100) for progress bars
     */
    public int getProgressPercentage() {
        return switch (this) {
            case PENDING -> 10;
            case PREPARED -> 20;
            case SHIPPED -> 40;
            case IN_TRANSIT -> 60;
            case OUT_FOR_DELIVERY -> 80;
            case DELIVERED -> 100;
            case RETURNED, CANCELLED -> 0;
        };
    }

    /**
     * Get customer-friendly message
     */
    public String getCustomerMessage() {
        return switch (this) {
            case PENDING -> "We're preparing your order for shipment.";
            case PREPARED -> "Your package is ready and waiting for pickup.";
            case SHIPPED -> "Your package has been shipped and is on its way!";
            case IN_TRANSIT -> "Your package is in transit to your location.";
            case OUT_FOR_DELIVERY -> "Your package is out for delivery today!";
            case DELIVERED -> "Your package has been delivered successfully.";
            case RETURNED -> "Your package has been returned to our warehouse.";
            case CANCELLED -> "Your shipment has been cancelled.";
        };
    }

    /**
     * Parse status from string code
     */
    public static ShipmentStatus fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return PENDING; // Default status
        }

        for (ShipmentStatus status : values()) {
            if (status.code.equalsIgnoreCase(code.trim())) {
                return status;
            }
        }

        throw new IllegalArgumentException("Invalid shipment status code: " + code);
    }

    /**
     * Get all active statuses (non-final states)
     */
    public static ShipmentStatus[] getActiveStatuses() {
        return new ShipmentStatus[]{PENDING, PREPARED, SHIPPED, IN_TRANSIT, OUT_FOR_DELIVERY};
    }

    /**
     * Get all final statuses
     */
    public static ShipmentStatus[] getFinalStatuses() {
        return new ShipmentStatus[]{DELIVERED, RETURNED, CANCELLED};
    }

    /**
     * Get all trackable statuses
     */
    public static ShipmentStatus[] getTrackableStatuses() {
        return new ShipmentStatus[]{SHIPPED, IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED};
    }

    @Override
    public String toString() {
        return displayName;
    }
}