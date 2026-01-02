package com.perfect8.common.utils;

import com.perfect8.common.enums.OrderStatus;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Helper class for OrderStatus operations
 * Shared between email-service and shop-service
 * Version 1.0 - Core functionality only
 *
 * Updated with COMPLETED and REFUNDED status handling
 */
@Slf4j
public class OrderStatusHelper {

    // Map över tillåtna status-övergångar
    private static final Map<OrderStatus, List<OrderStatus>> VALID_TRANSITIONS;

    static {
        Map<OrderStatus, List<OrderStatus>> transitions = new HashMap<>();

        // Initial states
        transitions.put(OrderStatus.PENDING,
                Arrays.asList(OrderStatus.PAID, OrderStatus.PAYMENT_FAILED,
                        OrderStatus.CANCELLED, OrderStatus.ON_HOLD));

        transitions.put(OrderStatus.PAYMENT_FAILED,
                Arrays.asList(OrderStatus.PENDING, OrderStatus.CANCELLED));

        // Payment confirmed
        transitions.put(OrderStatus.PAID,
                Arrays.asList(OrderStatus.PROCESSING, OrderStatus.CANCELLED,
                        OrderStatus.ON_HOLD, OrderStatus.REFUNDED));

        // Processing
        transitions.put(OrderStatus.PROCESSING,
                Arrays.asList(OrderStatus.SHIPPED, OrderStatus.CANCELLED,
                        OrderStatus.ON_HOLD, OrderStatus.REFUNDED));

        // Shipping
        transitions.put(OrderStatus.SHIPPED,
                Arrays.asList(OrderStatus.DELIVERED, OrderStatus.RETURNED));

        // Delivered - can be marked as completed or returned
        transitions.put(OrderStatus.DELIVERED,
                Arrays.asList(OrderStatus.COMPLETED, OrderStatus.RETURNED));

        // On Hold
        transitions.put(OrderStatus.ON_HOLD,
                Arrays.asList(OrderStatus.PROCESSING, OrderStatus.PAID,
                        OrderStatus.CANCELLED));

        // Final states - no transitions allowed
        transitions.put(OrderStatus.COMPLETED, Collections.emptyList());
        transitions.put(OrderStatus.CANCELLED, Collections.emptyList());
        transitions.put(OrderStatus.RETURNED,
                Arrays.asList(OrderStatus.REFUNDED)); // Returned can lead to refund
        transitions.put(OrderStatus.REFUNDED, Collections.emptyList());

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
        return status == OrderStatus.DELIVERED || status == OrderStatus.COMPLETED;
    }

    /**
     * Check if order can be refunded
     */
    public static boolean canBeRefunded(OrderStatus status) {
        return status == OrderStatus.PAID ||
                status == OrderStatus.PROCESSING ||
                status == OrderStatus.RETURNED;
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
                OrderStatus.DELIVERED,
                OrderStatus.ON_HOLD,
                OrderStatus.PAYMENT_FAILED
        );
    }

    /**
     * Get all final statuses
     */
    public static List<OrderStatus> getFinalStatuses() {
        return Arrays.asList(
                OrderStatus.COMPLETED,
                OrderStatus.CANCELLED,
                OrderStatus.RETURNED,
                OrderStatus.REFUNDED
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
            case COMPLETED -> "order-completed";
            case CANCELLED -> "order-cancelled";
            case RETURNED -> "order-returned";
            case REFUNDED -> "order-refunded";
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

        return status.getDisplayName(); // Use the enum's own method
    }

    // ============= METODER FÖR SHOP-SERVICE =============

    /**
     * Log status transition for audit purposes
     * Used by shop-service for tracking status changes
     *
     * @param orderId the order being transitioned
     * @param from the current status
     * @param to the new status
     */
    public static void logTransition(Long orderId, OrderStatus from, OrderStatus to) {
        if (orderId != null) {
            log.info("Order {} status transition: {} -> {}", orderId, from, to);
        }
    }

    /**
     * Check if order is in an editable state
     * Orders can only be edited before processing begins
     *
     * @param status current order status
     * @return true if order can be edited
     */
    public static boolean isEditableState(OrderStatus status) {
        if (status == null) {
            return false;
        }

        // Only allow edits in early states
        return status == OrderStatus.PENDING ||
                status == OrderStatus.PAID ||
                status == OrderStatus.ON_HOLD;
    }

    /**
     * Check if order is in a cancellable state
     * Similar to canBeCancelled but named differently for compatibility
     *
     * @param status current order status
     * @return true if order can be cancelled
     */
    public static boolean isCancellableState(OrderStatus status) {
        return canBeCancelled(status);
    }

    /**
     * Get list of required actions to transition from one status to another
     * Version 1.0 - Returns simple action descriptions
     *
     * @param from current status
     * @param to target status
     * @return list of required actions (empty if transition not valid)
     */
    public static List<String> getRequiredActions(OrderStatus from, OrderStatus to) {
        List<String> actions = new ArrayList<>();

        if (from == null || to == null || !canTransition(from, to)) {
            return actions;
        }

        // Define required actions for each transition
        if (from == OrderStatus.PENDING && to == OrderStatus.PAID) {
            actions.add("Process payment");
            actions.add("Verify payment details");
        } else if (from == OrderStatus.PAID && to == OrderStatus.PROCESSING) {
            actions.add("Confirm inventory availability");
            actions.add("Begin order fulfillment");
        } else if (from == OrderStatus.PROCESSING && to == OrderStatus.SHIPPED) {
            actions.add("Pack items");
            actions.add("Generate shipping label");
            actions.add("Update tracking information");
        } else if (from == OrderStatus.SHIPPED && to == OrderStatus.DELIVERED) {
            actions.add("Confirm delivery");
            actions.add("Update tracking status");
        } else if (from == OrderStatus.DELIVERED && to == OrderStatus.COMPLETED) {
            actions.add("Confirm customer satisfaction");
            actions.add("Close order");
        } else if (to == OrderStatus.CANCELLED) {
            actions.add("Process cancellation");
            if (from == OrderStatus.PAID || from == OrderStatus.PROCESSING) {
                actions.add("Initiate refund");
            }
            actions.add("Return items to inventory");
        } else if (to == OrderStatus.RETURNED) {
            actions.add("Process return request");
            actions.add("Receive returned items");
            actions.add("Inspect returned items");
        } else if (to == OrderStatus.REFUNDED) {
            actions.add("Process refund");
            actions.add("Update payment records");
            actions.add("Send refund confirmation");
        }

        return actions;
    }

    /**
     * Check if the status represents a completed order
     * Used for reporting and statistics
     *
     * @param status the order status to check
     * @return true if order is completed
     */
    public static boolean isCompleted(OrderStatus status) {
        return status == OrderStatus.COMPLETED || status == OrderStatus.DELIVERED;
    }

    /**
     * Check if the status represents a failed/cancelled order
     *
     * @param status the order status to check
     * @return true if order failed or was cancelled
     */
    public static boolean isFailed(OrderStatus status) {
        return status == OrderStatus.CANCELLED ||
                status == OrderStatus.PAYMENT_FAILED ||
                status == OrderStatus.RETURNED ||
                status == OrderStatus.REFUNDED;
    }

    /**
     * Get the priority level for a status (for sorting)
     * Lower number = higher priority
     *
     * @param status the order status
     * @return priority level (0-99)
     */
    public static int getPriority(OrderStatus status) {
        if (status == null) {
            return 99;
        }

        return status.getPriority(); // Use the enum's own method
    }
}