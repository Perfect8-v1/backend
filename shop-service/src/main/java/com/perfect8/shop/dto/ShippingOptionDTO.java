package com.perfect8.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO representing a single shipping option.
 * Used in ShippingOptionsDTO list and when creating shipments.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingOptionDTO {

    /**
     * Unique identifier for this shipping option
     */
    private Long id;

    /**
     * Name of the shipping method
     */
    @NotBlank(message = "Shipping option name is required")
    private String name;

    /**
     * Description of the shipping method
     */
    private String description;

    /**
     * Price for this shipping option
     */
    @NotNull(message = "Shipping price is required")
    @DecimalMin(value = "0.00", message = "Shipping price cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Invalid price format")
    private BigDecimal price;

    /**
     * Estimated delivery time in business days
     */
    @NotNull(message = "Estimated days is required")
    @Min(value = 0, message = "Estimated days cannot be negative")
    @Max(value = 365, message = "Estimated days seems unrealistic")
    private Integer estimatedDays;

    /**
     * Carrier name (USPS, FedEx, UPS, etc.)
     */
    @NotBlank(message = "Carrier is required")
    private String carrier;

    /**
     * Carrier service code (for API integration)
     */
    private String serviceCode;

    /**
     * Estimated delivery date
     */
    private LocalDate estimatedDeliveryDate;

    /**
     * Whether this is an express/expedited option
     */
    @Builder.Default
    private Boolean isExpress = false;

    /**
     * Whether signature is required
     */
    @Builder.Default
    private Boolean signatureRequired = false;

    /**
     * Whether insurance is included
     */
    @Builder.Default
    private Boolean insuranceIncluded = false;

    /**
     * Maximum insurance amount included
     */
    private BigDecimal insuranceAmount;

    /**
     * Whether tracking is available
     */
    @Builder.Default
    private Boolean trackingAvailable = true;

    /**
     * Whether this option is currently available
     */
    @Builder.Default
    private Boolean available = true;

    /**
     * Reason if not available
     */
    private String unavailableReason;

    /**
     * Delivery time window (e.g., "8am-5pm")
     */
    private String deliveryWindow;

    /**
     * Whether Saturday delivery is included
     */
    @Builder.Default
    private Boolean saturdayDelivery = false;

    /**
     * Whether Sunday delivery is included
     */
    @Builder.Default
    private Boolean sundayDelivery = false;

    /**
     * Priority/sort order for display
     */
    private Integer displayOrder;

    /**
     * Check if this is overnight shipping
     */
    public boolean isOvernight() {
        return estimatedDays != null && estimatedDays <= 1;
    }

    /**
     * Check if this is standard shipping
     */
    public boolean isStandard() {
        return !isExpress && estimatedDays != null && estimatedDays >= 5;
    }

    /**
     * Check if this is free shipping
     */
    public boolean isFree() {
        return price != null && price.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * Get display name with carrier
     */
    public String getDisplayName() {
        if (carrier != null && !carrier.isEmpty()) {
            return name + " (" + carrier + ")";
        }
        return name;
    }

    /**
     * Get delivery estimate text
     */
    public String getDeliveryEstimateText() {
        if (estimatedDays == null) {
            return "Unknown delivery time";
        }

        if (estimatedDays == 0) {
            return "Same day delivery";
        } else if (estimatedDays == 1) {
            return "Next business day";
        } else {
            return estimatedDays + " business days";
        }
    }

    /**
     * Calculate estimated delivery date from today
     */
    public LocalDate calculateEstimatedDeliveryDate() {
        if (estimatedDays == null) {
            return null;
        }

        LocalDate date = LocalDate.now();
        int daysToAdd = estimatedDays;

        // Skip weekends (simplified - doesn't account for holidays)
        while (daysToAdd > 0) {
            date = date.plusDays(1);
            if (date.getDayOfWeek().getValue() <= 5) { // Monday-Friday
                daysToAdd--;
            }
        }

        return date;
    }

    /**
     * Check if option is suitable for fragile items
     */
    public boolean isSuitableForFragile() {
        // Express and overnight shipping typically have better handling
        return isExpress || isOvernight() || insuranceIncluded;
    }
}