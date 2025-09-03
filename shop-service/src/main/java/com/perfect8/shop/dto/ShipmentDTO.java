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
import java.time.temporal.ChronoUnit;

/**
 * Data Transfer Object for Shipment
 * Version 1.0 - Critical tracking and delivery information
 *
 * Accurate shipment tracking prevents customer anxiety and support calls!
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Shipment ID - null for new shipments
     */
    private Long id;

    /**
     * Associated order ID - Required
     */
    @NotNull(message = "Order ID is required")
    private Long orderId;

    /**
     * Tracking number - Critical for customer tracking!
     */
    @NotBlank(message = "Tracking number is required")
    @Size(max = 100, message = "Tracking number cannot exceed 100 characters")
    @Pattern(regexp = "^[A-Z0-9\\-]+$", message = "Invalid tracking number format")
    private String trackingNumber;

    /**
     * Carrier name (FEDEX, UPS, USPS, DHL, etc.)
     */
    @NotBlank(message = "Carrier is required")
    @Pattern(regexp = "^(FEDEX|UPS|USPS|DHL|ONTRAC|LASERSHIP|AMAZON)$",
            message = "Carrier must be FEDEX, UPS, USPS, DHL, ONTRAC, LASERSHIP, or AMAZON")
    private String carrier;

    /**
     * Current shipment status
     */
    @NotNull(message = "Status is required")
    @Pattern(regexp = "^(PENDING|PROCESSING|SHIPPED|IN_TRANSIT|OUT_FOR_DELIVERY|DELIVERED|FAILED|RETURNED|CANCELLED)$",
            message = "Invalid shipment status")
    @Builder.Default
    private String status = "PENDING";

    /**
     * Shipping method type
     */
    @NotBlank(message = "Shipping method is required")
    @Pattern(regexp = "^(STANDARD|EXPRESS|OVERNIGHT|SAME_DAY|ECONOMY|PICKUP)$",
            message = "Invalid shipping method")
    @Builder.Default
    private String shippingMethod = "STANDARD";

    /**
     * Ship date - When package left warehouse
     */
    private LocalDateTime shippedAt;

    /**
     * Estimated delivery date - Critical for customer expectations!
     */
    @NotNull(message = "Estimated delivery date is required")
    @Future(message = "Estimated delivery date must be in the future")
    private LocalDate estimatedDeliveryDate;

    /**
     * Actual delivery date
     */
    private LocalDateTime deliveredAt;

    /**
     * Shipping cost
     */
    @NotNull(message = "Shipping cost is required")
    @DecimalMin(value = "0.00", message = "Shipping cost cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Invalid shipping cost format")
    private BigDecimal shippingCost;

    /**
     * Total weight in kilograms
     */
    @NotNull(message = "Weight is required")
    @DecimalMin(value = "0.01", message = "Weight must be greater than 0")
    @Digits(integer = 8, fraction = 3, message = "Invalid weight format")
    private BigDecimal weight;

    /**
     * Package dimensions (LxWxH in cm)
     */
    @NotBlank(message = "Dimensions are required")
    @Pattern(regexp = "^\\d+(\\.\\d+)?x\\d+(\\.\\d+)?x\\d+(\\.\\d+)?$",
            message = "Dimensions must be in format LxWxH (e.g., 30x20x10)")
    private String dimensions;

    /**
     * Recipient name - Critical for delivery!
     */
    @NotBlank(message = "Recipient name is required")
    @Size(min = 2, max = 100, message = "Recipient name must be between 2 and 100 characters")
    private String recipientName;

    /**
     * Recipient phone - Critical for delivery issues!
     */
    @NotBlank(message = "Recipient phone is required for delivery coordination")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$",
            message = "Invalid phone number format")
    private String recipientPhone;

    /**
     * Recipient email for notifications
     */
    @Email(message = "Invalid email format")
    private String recipientEmail;

    /**
     * Full shipping address as text
     */
    @NotBlank(message = "Shipping address is required")
    @Size(max = 500, message = "Shipping address cannot exceed 500 characters")
    private String shippingAddress;

    /**
     * Structured shipping address ID (references AddressDTO)
     */
    private Long shippingAddressId;

    /**
     * Current location/status from carrier
     */
    @Size(max = 255, message = "Current location cannot exceed 255 characters")
    private String currentLocation;

    /**
     * Last tracking update timestamp
     */
    private LocalDateTime lastTrackingUpdate;

    /**
     * Delivery instructions - Important for successful delivery!
     */
    @Size(max = 500, message = "Delivery instructions cannot exceed 500 characters")
    private String deliveryInstructions;

    /**
     * Signature required flag
     */
    @Builder.Default
    private boolean signatureRequired = false;

    /**
     * Insurance amount (if insured)
     */
    @DecimalMin(value = "0.00", message = "Insurance amount cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Invalid insurance amount format")
    private BigDecimal insuranceAmount;

    /**
     * Shipping label URL
     */
    @Size(max = 500, message = "Label URL cannot exceed 500 characters")
    @Pattern(regexp = "^(https?://.*)?$", message = "Invalid URL format")
    private String labelUrl;

    /**
     * Number of packages in this shipment
     */
    @Min(value = 1, message = "Must have at least 1 package")
    @Max(value = 100, message = "Cannot exceed 100 packages")
    @Builder.Default
    private Integer numberOfPackages = 1;

    /**
     * Package type
     */
    @Pattern(regexp = "^(BOX|ENVELOPE|TUBE|PALLET|CUSTOM)$",
            message = "Invalid package type")
    @Builder.Default
    private String packageType = "BOX";

    /**
     * Special handling flags
     */
    @Builder.Default
    private boolean fragile = false;

    @Builder.Default
    private boolean hazardous = false;

    @Builder.Default
    private boolean perishable = false;

    /**
     * Priority handling
     */
    @Builder.Default
    private boolean priorityHandling = false;

    /**
     * Delivery confirmation type
     */
    @Pattern(regexp = "^(NONE|SIGNATURE|ADULT_SIGNATURE|INDIRECT_SIGNATURE)$",
            message = "Invalid confirmation type")
    @Builder.Default
    private String confirmationType = "NONE";

    /**
     * Failed delivery attempts
     */
    @Min(value = 0, message = "Delivery attempts cannot be negative")
    @Builder.Default
    private Integer deliveryAttempts = 0;

    /**
     * Exception/problem description if status is FAILED
     */
    @Size(max = 1000, message = "Exception description cannot exceed 1000 characters")
    private String exceptionDescription;

    /**
     * Return tracking number (if returned)
     */
    @Size(max = 100, message = "Return tracking number cannot exceed 100 characters")
    private String returnTrackingNumber;

    /**
     * Internal notes
     */
    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;

    // Utility methods

    /**
     * Check if shipment is delivered
     */
    public boolean isDelivered() {
        return "DELIVERED".equals(status);
    }

    /**
     * Check if shipment is in transit
     */
    public boolean isInTransit() {
        return "IN_TRANSIT".equals(status) ||
                "OUT_FOR_DELIVERY".equals(status) ||
                "SHIPPED".equals(status);
    }

    /**
     * Check if shipment has failed
     */
    public boolean hasFailed() {
        return "FAILED".equals(status) || "RETURNED".equals(status);
    }

    /**
     * Check if shipment is cancelled
     */
    public boolean isCancelled() {
        return "CANCELLED".equals(status);
    }

    /**
     * Check if shipment requires special handling
     */
    public boolean requiresSpecialHandling() {
        return fragile || hazardous || perishable || priorityHandling;
    }

    /**
     * Check if shipment is insured
     */
    public boolean isInsured() {
        return insuranceAmount != null && insuranceAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Check if shipment is express
     */
    public boolean isExpress() {
        return "EXPRESS".equals(shippingMethod) ||
                "OVERNIGHT".equals(shippingMethod) ||
                "SAME_DAY".equals(shippingMethod);
    }

    /**
     * Get days in transit
     */
    public Long getDaysInTransit() {
        if (shippedAt == null) {
            return 0L;
        }
        LocalDateTime endDate = deliveredAt != null ? deliveredAt : LocalDateTime.now();
        return ChronoUnit.DAYS.between(shippedAt, endDate);
    }

    /**
     * Check if delivery is late
     */
    public boolean isLate() {
        if (estimatedDeliveryDate == null || isDelivered()) {
            return false;
        }
        return LocalDate.now().isAfter(estimatedDeliveryDate);
    }

    /**
     * Get tracking URL based on carrier
     */
    public String getTrackingUrl() {
        if (trackingNumber == null || carrier == null) {
            return null;
        }

        switch (carrier.toUpperCase()) {
            case "FEDEX":
                return "https://www.fedex.com/fedextrack/?tracknumbers=" + trackingNumber;
            case "UPS":
                return "https://www.ups.com/track?tracknum=" + trackingNumber;
            case "USPS":
                return "https://tools.usps.com/go/TrackConfirmAction?tLabels=" + trackingNumber;
            case "DHL":
                return "https://www.dhl.com/en/express/tracking.html?AWB=" + trackingNumber;
            case "ONTRAC":
                return "https://www.ontrac.com/tracking.asp?tracking=" + trackingNumber;
            case "LASERSHIP":
                return "https://www.lasership.com/track/" + trackingNumber;
            case "AMAZON":
                return "https://track.amazon.com/tracking/" + trackingNumber;
            default:
                return null;
        }
    }

    /**
     * Get human-readable status
     */
    public String getStatusDisplay() {
        switch (status) {
            case "PENDING":
                return "Preparing for shipment";
            case "PROCESSING":
                return "Processing";
            case "SHIPPED":
                return "Shipped";
            case "IN_TRANSIT":
                return "In transit";
            case "OUT_FOR_DELIVERY":
                return "Out for delivery";
            case "DELIVERED":
                return "Delivered";
            case "FAILED":
                return "Delivery failed";
            case "RETURNED":
                return "Returned to sender";
            case "CANCELLED":
                return "Cancelled";
            default:
                return status;
        }
    }

    /**
     * Get shipping method display text
     */
    public String getShippingMethodDisplay() {
        switch (shippingMethod) {
            case "STANDARD":
                return "Standard Shipping (5-7 business days)";
            case "EXPRESS":
                return "Express Shipping (2-3 business days)";
            case "OVERNIGHT":
                return "Overnight Delivery";
            case "SAME_DAY":
                return "Same Day Delivery";
            case "ECONOMY":
                return "Economy Shipping (7-10 business days)";
            case "PICKUP":
                return "Store Pickup";
            default:
                return shippingMethod;
        }
    }

    /**
     * Validate that delivered date is after shipped date
     */
    @AssertTrue(message = "Delivered date must be after shipped date")
    private boolean isDeliveryDateValid() {
        if (deliveredAt == null || shippedAt == null) {
            return true;
        }
        return deliveredAt.isAfter(shippedAt);
    }

    /**
     * Get a brief summary for notifications
     */
    public String getSummary() {
        return String.format("Package %s via %s - %s",
                trackingNumber,
                carrier,
                getStatusDisplay());
    }

    // Version 2.0 fields - commented out for future implementation
    /*
    // Advanced tracking - Version 2.0
    private List<TrackingEvent> trackingHistory;
    private String estimatedDeliveryWindow; // e.g., "2-4 PM"
    private GeolocationDTO lastKnownLocation;
    private String deliveryProofUrl;
    private String signatureImageUrl;

    // Carbon footprint - Version 2.0
    private BigDecimal carbonEmissions;
    private String transportMode;

    // Multi-carrier support - Version 2.0
    private List<String> handoffCarriers;
    private Map<String, String> carrierTrackingNumbers;

    // Customs for international - Version 2.0
    private String customsNumber;
    private String customsStatus;
    private BigDecimal customsDuty;
    */
}