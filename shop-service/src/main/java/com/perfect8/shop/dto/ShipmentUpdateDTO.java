package com.perfect8.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Shipment Updates
 * Version 1.0 - For updating existing shipments
 *
 * Used by admin to update shipment information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Updated shipment status
     */
    @Pattern(regexp = "^(PENDING|PROCESSING|SHIPPED|IN_TRANSIT|OUT_FOR_DELIVERY|DELIVERED|FAILED|RETURNED|CANCELLED)$",
            message = "Invalid shipment status")
    private String status;

    /**
     * Updated tracking number (if changed)
     */
    @Size(max = 100, message = "Tracking number cannot exceed 100 characters")
    @Pattern(regexp = "^[A-Z0-9\\-]*$", message = "Invalid tracking number format")
    private String trackingNumber;

    /**
     * Updated carrier (if changed)
     */
    @Pattern(regexp = "^(FEDEX|UPS|USPS|DHL|ONTRAC|LASERSHIP|AMAZON)?$",
            message = "Invalid carrier. Must be FEDEX, UPS, USPS, DHL, ONTRAC, LASERSHIP, or AMAZON")
    private String carrier;

    /**
     * Updated estimated delivery date
     */
    @Future(message = "Estimated delivery date must be in the future")
    private LocalDate estimatedDeliveryDate;

    /**
     * Current location of package
     */
    @Size(max = 255, message = "Current location cannot exceed 255 characters")
    private String currentLocation;

    /**
     * Updated shipping cost
     */
    @DecimalMin(value = "0.00", message = "Shipping cost cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Invalid shipping cost format")
    private BigDecimal shippingCost;

    /**
     * Updated weight in kilograms
     */
    @DecimalMin(value = "0.01", message = "Weight must be greater than 0")
    @Digits(integer = 8, fraction = 3, message = "Invalid weight format")
    private BigDecimal weight;

    /**
     * Updated dimensions (LxWxH in cm)
     */
    @Pattern(regexp = "^(\\d+(\\.\\d+)?x\\d+(\\.\\d+)?x\\d+(\\.\\d+)?)?$",
            message = "Dimensions must be in format LxWxH (e.g., 30x20x10)")
    private String dimensions;

    /**
     * Updated recipient name
     */
    @Size(min = 2, max = 100, message = "Recipient name must be between 2 and 100 characters")
    private String recipientName;

    /**
     * Updated recipient phone
     */
    @Pattern(regexp = "^(\\+?[1-9]\\d{1,14})?$",
            message = "Invalid phone number format")
    private String recipientPhone;

    /**
     * Updated recipient email
     */
    @Email(message = "Invalid email format")
    private String recipientEmail;

    /**
     * Updated shipping address
     */
    @Size(max = 500, message = "Shipping address cannot exceed 500 characters")
    private String shippingAddress;

    /**
     * Updated delivery instructions
     */
    @Size(max = 500, message = "Delivery instructions cannot exceed 500 characters")
    private String deliveryInstructions;

    /**
     * Signature required flag
     */
    private Boolean signatureRequired;

    /**
     * Insurance amount
     */
    @DecimalMin(value = "0.00", message = "Insurance amount cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Invalid insurance amount format")
    private BigDecimal insuranceAmount;

    /**
     * Number of packages
     */
    @Min(value = 1, message = "Must have at least 1 package")
    @Max(value = 100, message = "Cannot exceed 100 packages")
    private Integer numberOfPackages;

    /**
     * Package type
     */
    @Pattern(regexp = "^(BOX|ENVELOPE|TUBE|PALLET|CUSTOM)?$",
            message = "Invalid package type")
    private String packageType;

    /**
     * Special handling flags
     */
    private Boolean fragile;
    private Boolean hazardous;
    private Boolean perishable;
    private Boolean priorityHandling;

    /**
     * Delivery confirmation type
     */
    @Pattern(regexp = "^(NONE|SIGNATURE|ADULT_SIGNATURE|INDIRECT_SIGNATURE)?$",
            message = "Invalid confirmation type")
    private String confirmationType;

    /**
     * Label URL (if regenerated)
     */
    @Size(max = 500, message = "Label URL cannot exceed 500 characters")
    @Pattern(regexp = "^(https?://.*)?$", message = "Invalid URL format")
    private String labelUrl;

    /**
     * Internal notes for this update
     */
    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;

    /**
     * Reason for update (for audit trail)
     */
    @Size(max = 500, message = "Update reason cannot exceed 500 characters")
    private String updateReason;

    /**
     * Last tracking update timestamp
     */
    private LocalDateTime lastTrackingUpdate;

    /**
     * Delivery attempts count
     */
    @Min(value = 0, message = "Delivery attempts cannot be negative")
    @Max(value = 10, message = "Delivery attempts seem unrealistic")
    private Integer deliveryAttempts;

    /**
     * Exception/problem description
     */
    @Size(max = 1000, message = "Exception description cannot exceed 1000 characters")
    private String exceptionDescription;

    /**
     * Return tracking number (if item is being returned)
     */
    @Size(max = 100, message = "Return tracking number cannot exceed 100 characters")
    private String returnTrackingNumber;

    /**
     * Actual delivery date (when marking as delivered)
     */
    private LocalDateTime deliveredAt;

    /**
     * Delivery signature URL
     */
    @Size(max = 500, message = "Signature URL cannot exceed 500 characters")
    @Pattern(regexp = "^(https?://.*)?$", message = "Invalid URL format")
    private String signatureUrl;

    /**
     * Delivery proof photo URL
     */
    @Size(max = 500, message = "Proof URL cannot exceed 500 characters")
    @Pattern(regexp = "^(https?://.*)?$", message = "Invalid URL format")
    private String deliveryProofUrl;

    // Utility methods

    /**
     * Check if this is a status update
     */
    public boolean isStatusUpdate() {
        return status != null && !status.trim().isEmpty();
    }

    /**
     * Check if this is a tracking update
     */
    public boolean isTrackingUpdate() {
        return trackingNumber != null || currentLocation != null || lastTrackingUpdate != null;
    }

    /**
     * Check if this is marking as delivered
     */
    public boolean isDeliveryUpdate() {
        return "DELIVERED".equals(status) || deliveredAt != null;
    }

    /**
     * Check if this is a failed delivery
     */
    public boolean isFailedDelivery() {
        return "FAILED".equals(status) || exceptionDescription != null;
    }

    /**
     * Check if this changes carrier information
     */
    public boolean isCarrierChange() {
        return carrier != null || trackingNumber != null;
    }

    /**
     * Check if this updates recipient information
     */
    public boolean isRecipientUpdate() {
        return recipientName != null || recipientPhone != null ||
                recipientEmail != null || shippingAddress != null;
    }

    /**
     * Check if this updates package details
     */
    public boolean isPackageUpdate() {
        return weight != null || dimensions != null ||
                numberOfPackages != null || packageType != null;
    }

    /**
     * Check if special handling is being updated
     */
    public boolean hasSpecialHandlingUpdate() {
        return fragile != null || hazardous != null ||
                perishable != null || priorityHandling != null;
    }

    /**
     * Validate that delivered date is not in the past
     */
    @AssertTrue(message = "Delivered date cannot be in the future when marking as delivered")
    private boolean isDeliveredDateValid() {
        if (!"DELIVERED".equals(status) || deliveredAt == null) {
            return true;
        }
        return !deliveredAt.isAfter(LocalDateTime.now());
    }

    /**
     * Validate that return tracking is only set when status is RETURNED
     */
    @AssertTrue(message = "Return tracking number can only be set when status is RETURNED")
    private boolean isReturnTrackingValid() {
        if (returnTrackingNumber == null || returnTrackingNumber.trim().isEmpty()) {
            return true;
        }
        return "RETURNED".equals(status);
    }

    /**
     * Get a summary of what's being updated
     */
    public String getUpdateSummary() {
        StringBuilder summary = new StringBuilder("Updating: ");

        if (isStatusUpdate()) {
            summary.append("status to ").append(status).append(", ");
        }
        if (isTrackingUpdate()) {
            summary.append("tracking info, ");
        }
        if (isRecipientUpdate()) {
            summary.append("recipient details, ");
        }
        if (isPackageUpdate()) {
            summary.append("package details, ");
        }

        String result = summary.toString();
        if (result.endsWith(", ")) {
            result = result.substring(0, result.length() - 2);
        }

        return result;
    }
}