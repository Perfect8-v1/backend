package com.perfect8.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Shipping Option DTO for Shop Service
 * Version 1.0 - Core shipping option information
 * Represents available shipping methods and their details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingOptionDTO {

    private Long id;  // Added for backwards compatibility
    private Long shippingOptionId;  // Primary ID field
    private String name;
    private String description;
    private String carrier;
    private BigDecimal price;
    private Integer estimatedDays;
    private LocalDate estimatedDeliveryDate;
    private String deliveryTimeFrame;
    private Boolean isExpress;
    private Boolean isAvailable;
    private String trackingAvailable;
    private BigDecimal maxWeight;
    private BigDecimal minOrderAmount;
    private String serviceType;

    // Additional fields for compatibility
    private String shippingMethod;
    private BigDecimal baseCost;
    private BigDecimal weightCost;
    private String transitTime;
    private Boolean requiresSignature;
    private Boolean insuranceAvailable;
    private BigDecimal insuranceCost;
    private String restrictions;

    // Helper methods

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
     * Get formatted price
     */
    public String getFormattedPrice() {
        if (price != null) {
            return "$" + price.toString();
        }
        return "Free";
    }

    /**
     * Check if this is standard shipping
     */
    public boolean isStandardShipping() {
        return !Boolean.TRUE.equals(isExpress);
    }

    /**
     * Get delivery estimate text
     */
    public String getDeliveryEstimate() {
        if (estimatedDays != null) {
            if (estimatedDays == 1) {
                return "Next business day";
            } else {
                return estimatedDays + " business days";
            }
        }
        if (deliveryTimeFrame != null) {
            return deliveryTimeFrame;
        }
        return "Standard delivery";
    }

    /**
     * Check if option is available for given weight
     */
    public boolean isAvailableForWeight(BigDecimal weight) {
        if (maxWeight == null) {
            return true;
        }
        return weight.compareTo(maxWeight) <= 0;
    }

    /**
     * Check if option is available for given order amount
     */
    public boolean isAvailableForAmount(BigDecimal amount) {
        if (minOrderAmount == null) {
            return true;
        }
        return amount.compareTo(minOrderAmount) >= 0;
    }

    // Builder helper for backwards compatibility
    public static class ShippingOptionDTOBuilder {
        // Ensure both id and shippingOptionId are set
        public ShippingOptionDTOBuilder shippingOptionId(Long shippingOptionId) {
            this.shippingOptionId = shippingOptionId;
            this.id = shippingOptionId;  // Also set id for compatibility
            return this;
        }

        public ShippingOptionDTOBuilder id(Long id) {
            this.id = id;
            this.shippingOptionId = id;  // Also set shippingOptionId
            return this;
        }
    }
}