package com.perfect8.shop.enums;

/**
 * Enum representing the various states of an individual order item.
 * Used for tracking partial fulfillment and item-level status.
 * Version 1.0 - Core functionality only
 */
public enum ItemStatus {

    /**
     * Item is pending processing
     */
    PENDING("Pending", "Item is waiting to be processed"),

    /**
     * Item is being processed
     */
    PROCESSING("Processing", "Item is being prepared"),

    /**
     * Item has been partially shipped (some quantity shipped)
     */
    PARTIALLY_SHIPPED("Partially Shipped", "Part of the item quantity has been shipped"),

    /**
     * Item has been fully shipped
     */
    SHIPPED("Shipped", "Item has been completely shipped"),

    /**
     * Item has been delivered to customer
     */
    DELIVERED("Delivered", "Item has been delivered to customer"),

    /**
     * Item has been cancelled
     */
    CANCELLED("Cancelled", "Item has been cancelled"),

    /**
     * Item has been returned by customer
     */
    RETURNED("Returned", "Item has been returned by customer"),

    /**
     * Item has been refunded
     */
    REFUNDED("Refunded", "Item has been refunded"),

    /**
     * Item is out of stock
     */
    OUT_OF_STOCK("Out of Stock", "Item is currently out of stock"),

    /**
     * Item is on backorder
     */
    BACKORDERED("Backordered", "Item is on backorder");

    private final String displayName;
    private final String description;

    ItemStatus(String displayName, String description) {
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
     * Check if the item status allows cancellation
     * @return true if item can be cancelled in this status
     */
    public boolean canBeCancelled() {
        return this == PENDING || this == PROCESSING || this == BACKORDERED;
    }

    /**
     * Check if the item status allows returns
     * @return true if item can be returned in this status
     */
    public boolean canBeReturned() {
        return this == DELIVERED;
    }

    /**
     * Check if the item can be shipped
     * @return true if item can be shipped in this status
     */
    public boolean canBeShipped() {
        return this == PENDING || this == PROCESSING || this == PARTIALLY_SHIPPED;
    }

    /**
     * Check if the item is in a final state (cannot be modified)
     * @return true if item is in final state
     */
    public boolean isFinalState() {
        return this == DELIVERED || this == CANCELLED ||
                this == RETURNED || this == REFUNDED;
    }

    /**
     * Check if the item has been shipped (fully or partially)
     * @return true if item has any shipped quantity
     */
    public boolean hasShipped() {
        return this == PARTIALLY_SHIPPED || this == SHIPPED ||
                this == DELIVERED || this == RETURNED;
    }

    /**
     * Get next possible statuses from current status
     * @return array of possible next statuses
     */
    public ItemStatus[] getNextPossibleStatuses() {
        switch (this) {
            case PENDING:
                return new ItemStatus[]{PROCESSING, OUT_OF_STOCK, BACKORDERED, CANCELLED};
            case PROCESSING:
                return new ItemStatus[]{SHIPPED, PARTIALLY_SHIPPED, CANCELLED};
            case PARTIALLY_SHIPPED:
                return new ItemStatus[]{SHIPPED, CANCELLED};
            case SHIPPED:
                return new ItemStatus[]{DELIVERED};
            case DELIVERED:
                return new ItemStatus[]{RETURNED};
            case RETURNED:
                return new ItemStatus[]{REFUNDED};
            case OUT_OF_STOCK:
                return new ItemStatus[]{BACKORDERED, PENDING, CANCELLED};
            case BACKORDERED:
                return new ItemStatus[]{PENDING, CANCELLED};
            default:
                return new ItemStatus[0]; // Final states have no next status
        }
    }

    /**
     * Check if transition to new status is valid
     * @param newStatus the status to transition to
     * @return true if transition is allowed
     */
    public boolean canTransitionTo(ItemStatus newStatus) {
        if (newStatus == null) {
            return false;
        }

        ItemStatus[] possibleStatuses = getNextPossibleStatuses();
        for (ItemStatus status : possibleStatuses) {
            if (status == newStatus) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get ItemStatus from string value (case-insensitive)
     * @param status string representation of status
     * @return ItemStatus enum value or null if not found
     */
    public static ItemStatus fromString(String status) {
        if (status == null || status.trim().isEmpty()) {
            return null;
        }

        try {
            return ItemStatus.valueOf(status.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            // Try to match by display name
            for (ItemStatus itemStatus : ItemStatus.values()) {
                if (itemStatus.getDisplayName().equalsIgnoreCase(status)) {
                    return itemStatus;
                }
            }
            return null;
        }
    }

    @Override
    public String toString() {
        return displayName;
    }
}