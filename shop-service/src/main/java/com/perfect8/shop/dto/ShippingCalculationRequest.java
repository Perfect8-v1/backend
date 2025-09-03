package com.perfect8.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Request DTO for calculating shipping costs and options.
 * Contains all necessary information to determine shipping rates.
 * TODO skall kunna hantera SI-enheter
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingCalculationRequest {

    /**
     * Total weight of the shipment (in pounds)
     */
    @NotNull(message = "Total weight is required")
    @DecimalMin(value = "0.01", message = "Weight must be greater than 0")
    @DecimalMax(value = "150.00", message = "Weight exceeds maximum shipping limit")
    private BigDecimal totalWeight;

    /**
     * Package dimensions
     */
    @Valid
    private PackageDimensions dimensions;

    /**
     * Number of items in the shipment
     */
    @NotNull(message = "Item count is required")
    @Min(value = 1, message = "Item count must be at least 1")
    private Integer itemCount;

    /**
     * Total value of items (for insurance purposes)
     */
    @NotNull(message = "Total value is required")
    @DecimalMin(value = "0.00", message = "Total value cannot be negative")
    private BigDecimal totalValue;

    /**
     * Destination ZIP/postal code
     */
    @NotBlank(message = "Destination ZIP code is required")
    @Pattern(regexp = "^\\d{5}(-\\d{4})?$", message = "Invalid ZIP code format")
    private String destinationZip;

    /**
     * Origin ZIP code (warehouse location)
     */
    private String originZip;

    /**
     * Whether the shipment contains fragile items
     */
    @Builder.Default
    private Boolean hasFragileItems = false;

    /**
     * Whether signature is required on delivery
     */
    @Builder.Default
    private Boolean signatureRequired = false;

    /**
     * Whether insurance is requested
     */
    @Builder.Default
    private Boolean insuranceRequested = false;

    /**
     * Whether this is a residential delivery
     */
    @Builder.Default
    private Boolean residentialDelivery = true;

    /**
     * Whether Saturday delivery is needed
     */
    @Builder.Default
    private Boolean saturdayDelivery = false;

    /**
     * List of product categories (for restriction checking)
     */
    private List<String> productCategories;

    /**
     * Special handling codes
     */
    private List<String> specialHandlingCodes;

    /**
     * Inner class for package dimensions
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PackageDimensions {

        @NotNull(message = "Length is required")
        @DecimalMin(value = "0.01", message = "Length must be greater than 0")
        @DecimalMax(value = "108.00", message = "Length exceeds maximum limit")
        private BigDecimal length;

        @NotNull(message = "Width is required")
        @DecimalMin(value = "0.01", message = "Width must be greater than 0")
        @DecimalMax(value = "108.00", message = "Width exceeds maximum limit")
        private BigDecimal width;

        @NotNull(message = "Height is required")
        @DecimalMin(value = "0.01", message = "Height must be greater than 0")
        @DecimalMax(value = "108.00", message = "Height exceeds maximum limit")
        private BigDecimal height;

        /**
         * Calculate volume in cubic inches
         */
        public BigDecimal getVolume() {
            if (length == null || width == null || height == null) {
                return BigDecimal.ZERO;
            }
            return length.multiply(width).multiply(height);
        }

        /**
         * Calculate dimensional weight (L x W x H / 166 for domestic)
         */
        public BigDecimal getDimensionalWeight() {
            return getVolume().divide(new BigDecimal("166"), 2, BigDecimal.ROUND_UP);
        }

        /**
         * Check if package is oversized
         */
        public boolean isOversized() {
            if (length == null || width == null || height == null) {
                return false;
            }

            // Package is oversized if any dimension exceeds 48 inches
            // or if length + 2*(width + height) > 108
            BigDecimal maxDimension = length.max(width).max(height);
            if (maxDimension.compareTo(new BigDecimal("48")) > 0) {
                return true;
            }

            BigDecimal girth = width.add(height).multiply(new BigDecimal("2"));
            BigDecimal totalSize = length.add(girth);
            return totalSize.compareTo(new BigDecimal("108")) > 0;
        }
    }

    /**
     * Get billable weight (greater of actual weight or dimensional weight)
     */
    public BigDecimal getBillableWeight() {
        if (totalWeight == null) {
            return BigDecimal.ZERO;
        }

        if (dimensions != null) {
            BigDecimal dimWeight = dimensions.getDimensionalWeight();
            return totalWeight.max(dimWeight);
        }

        return totalWeight;
    }

    /**
     * Check if expedited shipping is possible
     */
    public boolean canShipExpedited() {
        // Cannot expedite if too heavy or oversized
        if (totalWeight != null && totalWeight.compareTo(new BigDecimal("70")) > 0) {
            return false;
        }

        if (dimensions != null && dimensions.isOversized()) {
            return false;
        }

        // Cannot expedite certain categories (would check against restricted list)
        if (productCategories != null && productCategories.contains("HAZMAT")) {
            return false;
        }

        return true;
    }

    /**
     * Check if package requires special handling
     */
    public boolean requiresSpecialHandling() {
        return hasFragileItems ||
                (totalValue != null && totalValue.compareTo(new BigDecimal("1000")) > 0) ||
                (dimensions != null && dimensions.isOversized()) ||
                (specialHandlingCodes != null && !specialHandlingCodes.isEmpty());
    }

    /**
     * Calculate insurance value (if requested)
     */
    public BigDecimal getInsuranceValue() {
        if (!insuranceRequested || totalValue == null) {
            return BigDecimal.ZERO;
        }

        // Standard insurance covers up to $100, additional coverage for higher values
        if (totalValue.compareTo(new BigDecimal("100")) <= 0) {
            return BigDecimal.ZERO; // Included in base rate
        }

        return totalValue.subtract(new BigDecimal("100"));
    }

    /**
     * Validate request completeness
     */
    public boolean isValid() {
        return totalWeight != null &&
                totalWeight.compareTo(BigDecimal.ZERO) > 0 &&
                itemCount != null &&
                itemCount > 0 &&
                destinationZip != null &&
                !destinationZip.trim().isEmpty();
    }
}