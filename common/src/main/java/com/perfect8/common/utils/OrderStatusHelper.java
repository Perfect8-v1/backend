package com.perfect8.common.utils;

import com.perfect8.common.enums.OrderStatus;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Helper class for OrderStatus operations
 * Shared between email-service and shop-service
 * Version 1.0 - Core functionality only
 */
public class OrderStatusHelper {

    // Map över tillåtna status-övergångar
    private static final Map<OrderStatus, List<OrderStatus>> VALID_TRANSITIONS;

    static {
        Map<OrderStatus, List<OrderStatus>> transitions = new HashMap<>();

        // Definiera tillåtna övergångar för varje status
        transitions.put(OrderStatus.PENDING,
                Arrays.asList(OrderStatus.PAID, OrderStatus.PAYMENT_FAILED,
                        OrderStatus.CANCELLED, OrderStatus.ON_HOLD));

        transitions.put(OrderStatus.PAID,
                Arrays.asList(OrderStatus.PROCESSING, OrderStatus.CANCELLED,
                        OrderStatus.ON_HOLD));

        transitions.put(OrderStatus.PROCESSING,
                Arrays.asList(OrderStatus.SHIPPED, OrderStatus.CANCELLED,
                        OrderStatus.ON_HOLD));

        transitions.put(OrderStatus.SHIPPED,
                Arrays.asList(OrderStatus.DELIVERED, OrderStatus.RETURNED));

        transitions.put(OrderStatus.DELIVERED,
                Arrays.asList(OrderStatus.RETURNED));

        transitions.put(OrderStatus.ON_HOLD,
                Arrays.asList(OrderStatus.PROCESSING, OrderStatus.PAID,
                        OrderStatus.CANCELLED));

        transitions.put(OrderStatus.PAYMENT_FAILED,
                Arrays.asList(OrderStatus.PENDING, OrderStatus.CANCELLED));

        // Final states - inga övergångar tillåtna
        transitions.put(OrderStatus.CANCELLED, Collections.emptyList());
        transitions.put(OrderStatus.RETURNED, Collections.emptyList());

        VALID_TRANSITIONS = Collections.unmodifiableMap(transitions);
    }

    /**
     * Check if transition from one status to another is valid
     */
    public static boolean canTransition(OrderStatus from, OrderStatus to) {
        if (from == null || to == null) {
            return false;
        }

        if (from == to) {
            return true; // Same status always allowed
        }

        List<OrderStatus> validNext = VALID_TRANSITIONS.get(from);
        return validNext != null && validNext.contains(to);
    }

    /**
     * Get all possible next statuses from current status
     */
    public static List<OrderStatus> getNextPossibleStatuses(OrderStatus current) {
        if (current == null) {
            return Collections.emptyList();
        }

        List<OrderStatus> next = VALID_TRANSITIONS.get(current);
        return next != null ? next : Collections.emptyList();
    }

    /**
     * Check if order can be cancelled in current status
     */
    public static boolean canBeCancelled(OrderStatus status) {
        if (status == null) {
            return false;
        }

        return status == OrderStatus.PENDING ||
                status == OrderStatus.PAID ||
                status == OrderStatus.PROCESSING ||
                status == OrderStatus.ON_HOLD;
    }

    /**
     * Check if order can be returned in current status
     */
    public static boolean canBeReturned(OrderStatus status) {
        return status == OrderStatus.DELIVERED;
    }

    /**
     * Check if order requires payment action
     */
    public static boolean requiresPayment(OrderStatus status) {
        return status == OrderStatus.PENDING;
    }

    /**
     * Check if order is in fulfillment process
     */
    public static boolean isInFulfillment(OrderStatus status) {
        return status == OrderStatus.PROCESSING ||
                status == OrderStatus.SHIPPED;
    }

    /**
     * Get all active (non-final) statuses
     */
    public static List<OrderStatus> getActiveStatuses() {
        return Arrays.asList(
                OrderStatus.PENDING,
                OrderStatus.PAID,
                OrderStatus.PROCESSING,
                OrderStatus.SHIPPED,
                OrderStatus.ON_HOLD,
                OrderStatus.PAYMENT_FAILED
        );
    }

    /**
     * Get all final statuses
     */
    public static List<OrderStatus> getFinalStatuses() {
        return Arrays.asList(
                OrderStatus.DELIVERED,
                OrderStatus.CANCELLED,
                OrderStatus.RETURNED
        );
    }

    /**
     * Validate status transition and throw exception if invalid
     */
    public static void validateTransition(OrderStatus from, OrderStatus to) {
        if (!canTransition(from, to)) {
            throw new IllegalStateException(
                    String.format("Invalid status transition from %s to %s",
                            from, to)
            );
        }
    }

    /**
     * Check if email should be sent for this status
     * Used by email-service
     */
    public static boolean shouldSendEmailForStatus(OrderStatus status) {
        // In v1.0, we send emails for all statuses except ON_HOLD
        return status != OrderStatus.ON_HOLD;
    }

    /**
     * Get email template name for status
     * Used by email-service
     */
    public static String getEmailTemplateForStatus(OrderStatus status) {
        if (status == null) {
            return null;
        }

        return switch (status) {
            case PENDING -> "order-pending";
            case PAID -> "order-confirmation";
            case PROCESSING -> "order-processing";
            case SHIPPED -> "order-shipped";
            case DELIVERED -> "order-delivered";
            case CANCELLED -> "order-cancelled";
            case RETURNED -> "order-returned";
            case PAYMENT_FAILED -> "payment-failed";
            case ON_HOLD -> null; // No automatic email
        };
    }

    /**
     * Get customer-friendly display name for status
     */
    public static String getDisplayName(OrderStatus status) {
        if (status == null) {
            return "Unknown";
        }

        return switch (status) {
            case PENDING -> "Pending Payment";
            case PAID -> "Payment Confirmed";
            case PROCESSING -> "Processing";
            case SHIPPED -> "Shipped";
            case DELIVERED -> "Delivered";
            case CANCELLED -> "Cancelled";
            case RETURNED -> "Returned";
            case PAYMENT_FAILED -> "Payment Failed";
            case ON_HOLD -> "On Hold";
        };
    }
}