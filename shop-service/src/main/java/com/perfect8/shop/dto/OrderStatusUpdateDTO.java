package com.perfect8.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Order Status Updates
 * Version 1.0 - Core order workflow management
 *
 * Critical: Accurate status tracking prevents customer confusion!
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * New order status - Required
     */
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(PENDING|CONFIRMED|PROCESSING|SHIPPED|OUT_FOR_DELIVERY|DELIVERED|CANCELLED|FAILED|RETURNED|REFUNDED)$",
            message = "Invalid order status")
    private String status;

    /**
     * Update notes/reason
     */
    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;

    /**
     * User who made the update (for audit trail)
     */
    @Size(max = 100, message = "Updated by cannot exceed 100 characters")
    private String updatedBy;

    /**
     * Whether to notify customer of this update
     */
    @Builder.Default
    private Boolean notifyCustomer = true;

    /**
     * Custom notification message (overrides default)
     */
    @Size(max = 500, message = "Notification message cannot exceed 500 characters")
    private String customerNotificationMessage;

    // === SHIPPING FIELDS (when marking as SHIPPED) ===

    /**
     * Tracking number (required when status = SHIPPED)
     */
    @Size(max = 100, message = "Tracking number cannot exceed 100 characters")
    @Pattern(regexp = "^[A-Z0-9\\-]*$", message = "Invalid tracking number format")
    private String trackingNumber;

    /**
     * Carrier name (required when status = SHIPPED)
     */
    @Pattern(regexp = "^(FEDEX|UPS|USPS|DHL|ONTRAC|LASERSHIP|AMAZON)?$",
            message = "Invalid carrier")
    private String carrier;

    /**
     * Estimated delivery date
     */
    private LocalDateTime estimatedDeliveryDate;

    // === CANCELLATION FIELDS (when status = CANCELLED) ===

    /**
     * Cancellation reason
     */
    @Size(max = 500, message = "Cancellation reason cannot exceed 500 characters")
    private String cancellationReason;

    /**
     * Who requested cancellation
     */
    @Pattern(regexp = "^(CUSTOMER|ADMIN|SYSTEM|PAYMENT_FAILED)?$",
            message = "Invalid cancellation requestor")
    private String cancelledBy;

    /**
     * Whether to restock items
     */
    @Builder.Default
    private Boolean restockItems = true;

    // === RETURN FIELDS (when status = RETURNED) ===

    /**
     * Return reason
     */
    @Size(max = 500, message = "Return reason cannot exceed 500 characters")
    private String returnReason;

    /**
     * Return tracking number
     */
    @Size(max = 100, message = "Return tracking number cannot exceed 100 characters")
    private String returnTrackingNumber;

    /**
     * Return condition
     */
    @Pattern(regexp = "^(UNOPENED|OPENED|DAMAGED|DEFECTIVE)?$",
            message = "Invalid return condition")
    private String returnCondition;

    // === DELIVERY FIELDS (when status = DELIVERED) ===

    /**
     * Actual delivery date/time
     */
    private LocalDateTime actualDeliveryDate;

    /**
     * Who received the package
     */
    @Size(max = 100, message = "Delivered to cannot exceed 100 characters")
    private String deliveredTo;

    /**
     * Delivery notes
     */
    @Size(max = 500, message = "Delivery notes cannot exceed 500 characters")
    private String deliveryNotes;

    /**
     * Signature URL (if captured)
     */
    @Size(max = 500, message = "Signature URL cannot exceed 500 characters")
    private String signatureUrl;

    // === FAILURE FIELDS (when status = FAILED) ===

    /**
     * Failure reason
     */
    @Size(max = 500, message = "Failure reason cannot exceed 500 characters")
    private String failureReason;

    /**
     * Failure type
     */
    @Pattern(regexp = "^(PAYMENT_FAILED|OUT_OF_STOCK|FRAUD_DETECTED|SHIPPING_ISSUE|SYSTEM_ERROR)?$",
            message = "Invalid failure type")
    private String failureType;

    /**
     * Recommended action
     */
    @Size(max = 500, message = "Recommended action cannot exceed 500 characters")
    private String recommendedAction;

    // === PROCESSING FIELDS ===

    /**
     * Processing location/warehouse
     */
    @Size(max = 100, message = "Processing location cannot exceed 100 characters")
    private String processingLocation;

    /**
     * Order priority
     */
    @Min(value = 1, message = "Priority must be at least 1")
    @Max(value = 5, message = "Priority cannot exceed 5")
    private Integer priority;

    // Utility methods

    /**
     * Check if this is a terminal status
     */
    public boolean isTerminalStatus() {
        return "DELIVERED".equals(status) ||
                "CANCELLED".equals(status) ||
                "REFUNDED".equals(status);
    }

    /**
     * Check if this requires shipping info
     */
    public boolean requiresShippingInfo() {
        return "SHIPPED".equals(status) &&
                (trackingNumber == null || carrier == null);
    }

    /**
     * Check if customer should be notified
     */
    public boolean shouldNotifyCustomer() {
        return Boolean.TRUE.equals(notifyCustomer);
    }

    /**
     * Check if items should be restocked
     */
    public boolean shouldRestockItems() {
        return Boolean.TRUE.equals(restockItems) &&
                ("CANCELLED".equals(status) || "RETURNED".equals(status));
    }

    /**
     * Get customer-friendly status message
     */
    public String getCustomerMessage() {
        if (customerNotificationMessage != null && !customerNotificationMessage.trim().isEmpty()) {
            return customerNotificationMessage;
        }

        switch (status) {
            case "PENDING":
                return "Your order has been received and is being processed.";
            case "CONFIRMED":
                return "Your order has been confirmed and payment verified.";
            case "PROCESSING":
                return "Your order is being prepared for shipment.";
            case "SHIPPED":
                if (trackingNumber != null && carrier != null) {
                    return String.format("Your order has been shipped via %s. Tracking: %s",
                            carrier, trackingNumber);
                }
                return "Your order has been shipped.";
            case "OUT_FOR_DELIVERY":
                return "Your order is out for delivery today.";
            case "DELIVERED":
                return "Your order has been delivered successfully.";
            case "CANCELLED":
                if (cancellationReason != null) {
                    return "Your order has been cancelled: " + cancellationReason;
                }
                return "Your order has been cancelled.";
            case "FAILED":
                if (failureReason != null) {
                    return "Order processing failed: " + failureReason;
                }
                return "Your order could not be processed.";
            case "RETURNED":
                return "Your return has been processed.";
            case "REFUNDED":
                return "Your refund has been processed.";
            default:
                return "Your order status has been updated to: " + status;
        }
    }

    /**
     * Validate that required fields are present for specific statuses
     */
    @AssertTrue(message = "Tracking number and carrier required when marking as shipped")
    private boolean isShippingInfoValid() {
        if (!"SHIPPED".equals(status)) {
            return true;
        }
        return trackingNumber != null && !trackingNumber.trim().isEmpty() &&
                carrier != null && !carrier.trim().isEmpty();
    }

    /**
     * Validate cancellation reason is provided
     */
    @AssertTrue(message = "Cancellation reason required when cancelling order")
    private boolean isCancellationValid() {
        if (!"CANCELLED".equals(status)) {
            return true;
        }
        return cancellationReason != null && !cancellationReason.trim().isEmpty();
    }

    /**
     * Validate return reason is provided
     */
    @AssertTrue(message = "Return reason required when marking as returned")
    private boolean isReturnValid() {
        if (!"RETURNED".equals(status)) {
            return true;
        }
        return returnReason != null && !returnReason.trim().isEmpty();
    }

    /**
     * Validate failure reason is provided
     */
    @AssertTrue(message = "Failure reason required when marking as failed")
    private boolean isFailureValid() {
        if (!"FAILED".equals(status)) {
            return true;
        }
        return failureReason != null && !failureReason.trim().isEmpty();
    }
}