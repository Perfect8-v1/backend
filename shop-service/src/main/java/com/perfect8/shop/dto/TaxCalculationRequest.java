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
 * Request DTO for calculating taxes on an order.
 * Contains all necessary information to determine applicable tax rates.
 * Version 1.0 - US tax calculation support
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxCalculationRequest {

    /**
     * Subtotal amount before tax
     */
    @NotNull(message = "Subtotal is required")
    @DecimalMin(value = "0.00", message = "Subtotal cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Invalid subtotal format")
    private BigDecimal subtotal;

    /**
     * Shipping address for tax calculation
     */
    @NotNull(message = "Shipping address is required")
    @Valid
    private AddressDTO shippingAddress;

    /**
     * Billing address (if different from shipping)
     */
    @Valid
    private AddressDTO billingAddress;

    /**
     * Shipping cost (may be taxable in some states)
     */
    @DecimalMin(value = "0.00", message = "Shipping cost cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Invalid shipping cost format")
    private BigDecimal shippingCost;

    /**
     * Whether the customer is tax exempt
     */
    @Builder.Default
    private Boolean taxExempt = false;

    /**
     * Tax exemption certificate number (if applicable)
     */
    @Size(max = 50, message = "Tax exemption number cannot exceed 50 characters")
    private String taxExemptionNumber;

    /**
     * List of items for category-specific tax rules
     */
    private List<TaxableItem> items;

    /**
     * Customer type (INDIVIDUAL, BUSINESS)
     */
    @Builder.Default
    private CustomerType customerType = CustomerType.INDIVIDUAL;

    /**
     * Whether this is an international order
     */
    @Builder.Default
    private Boolean international = false;

    /**
     * Discount amount (may affect tax calculation)
     */
    @DecimalMin(value = "0.00", message = "Discount cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Invalid discount format")
    private BigDecimal discountAmount;

    /**
     * Inner class for taxable items
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaxableItem {

        @NotNull(message = "Product ID is required")
        private Long productId;

        @NotNull(message = "Product category is required")
        private String category;

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.00", message = "Amount cannot be negative")
        private BigDecimal amount;

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;

        // Some items may be tax-exempt (like food in certain states)
        @Builder.Default
        private Boolean taxExempt = false;

        // Special tax category (FOOD, CLOTHING, MEDICINE, etc.)
        private TaxCategory taxCategory;
    }

    /**
     * Tax categories for special rates
     */
    public enum TaxCategory {
        GENERAL,
        FOOD,
        CLOTHING,
        MEDICINE,
        DIGITAL_GOODS,
        SERVICES,
        ALCOHOL,
        TOBACCO,
        LUXURY
    }

    /**
     * Customer types
     */
    public enum CustomerType {
        INDIVIDUAL,
        BUSINESS,
        NON_PROFIT,
        GOVERNMENT,
        RESELLER
    }

    /**
     * Get taxable subtotal (after discounts)
     */
    public BigDecimal getTaxableSubtotal() {
        if (subtotal == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal taxableAmount = subtotal;

        // Subtract discount if present
        if (discountAmount != null && discountAmount.compareTo(BigDecimal.ZERO) > 0) {
            taxableAmount = taxableAmount.subtract(discountAmount);
        }

        // Ensure not negative
        return taxableAmount.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : taxableAmount;
    }

    /**
     * Get total taxable amount (including shipping if applicable)
     */
    public BigDecimal getTotalTaxableAmount(boolean shippingIsTaxable) {
        BigDecimal taxableAmount = getTaxableSubtotal();

        if (shippingIsTaxable && shippingCost != null && shippingCost.compareTo(BigDecimal.ZERO) > 0) {
            taxableAmount = taxableAmount.add(shippingCost);
        }

        return taxableAmount;
    }

    /**
     * Check if order is tax exempt
     */
    public boolean isTaxExempt() {
        if (taxExempt != null && taxExempt) {
            return true;
        }

        // Some customer types may be tax exempt
        return customerType == CustomerType.NON_PROFIT ||
                customerType == CustomerType.GOVERNMENT;
    }

    /**
     * Check if this is a resale (no tax collected)
     */
    public boolean isResale() {
        return customerType == CustomerType.RESELLER &&
                taxExemptionNumber != null &&
                !taxExemptionNumber.trim().isEmpty();
    }

    /**
     * Check if shipping should be taxed (state-specific)
     */
    public boolean shouldTaxShipping() {
        if (shippingAddress == null || shippingAddress.getState() == null) {
            return false;
        }

        // States that tax shipping (simplified list)
        String state = shippingAddress.getState().toUpperCase();
        return List.of("AR", "CT", "DC", "GA", "HI", "KS", "KY", "MI", "MN",
                "MS", "NJ", "NM", "NY", "NC", "ND", "OH", "PA", "RI",
                "SC", "SD", "TN", "TX", "VT", "WA", "WV", "WI").contains(state);
    }

    /**
     * Get tax jurisdiction string
     * FIXED: Using getPostalCode() instead of getZipCode()
     */
    public String getTaxJurisdiction() {
        if (shippingAddress == null) {
            return "Unknown";
        }

        StringBuilder jurisdiction = new StringBuilder();

        if (shippingAddress.getState() != null) {
            jurisdiction.append(shippingAddress.getState());
        }

        if (shippingAddress.getCity() != null) {
            if (jurisdiction.length() > 0) jurisdiction.append(" - ");
            jurisdiction.append(shippingAddress.getCity());
        }

        // FIXED: Changed from getZipCode() to getPostalCode()
        if (shippingAddress.getPostalCode() != null) {
            if (jurisdiction.length() > 0) jurisdiction.append(" ");
            jurisdiction.append("(").append(shippingAddress.getPostalCode()).append(")");
        }

        return jurisdiction.length() > 0 ? jurisdiction.toString() : "Unknown";
    }

    /**
     * Validate request
     * FIXED: Using getPostalCode() instead of getZipCode()
     */
    public boolean isValid() {
        return subtotal != null &&
                subtotal.compareTo(BigDecimal.ZERO) >= 0 &&
                shippingAddress != null &&
                shippingAddress.getState() != null &&
                shippingAddress.getPostalCode() != null;  // FIXED
    }

    /**
     * Check if any items have special tax treatment
     */
    public boolean hasSpecialTaxItems() {
        if (items == null || items.isEmpty()) {
            return false;
        }

        return items.stream().anyMatch(item ->
                item.taxCategory != null && item.taxCategory != TaxCategory.GENERAL
        );
    }
}