package com.perfect8.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Data Transfer Object for Order Items
 * Version 1.0 - Represents a single line item in an order
 *
 * Critical: Accurate order items prevent customer complaints!
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Order item ID (null for new items)
     */
    private Long id;

    /**
     * Product ID - CRITICAL!
     */
    @NotNull(message = "Product ID is required")
    @Positive(message = "Invalid product ID")
    private Long productId;

    /**
     * Quantity ordered
     */
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 999, message = "Quantity cannot exceed 999")
    private Integer quantity;

    /**
     * Unit price at time of order
     * Captured to preserve historical pricing
     */
    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Invalid price format")
    private BigDecimal unitPrice;

    /**
     * Product name for display
     */
    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name cannot exceed 255 characters")
    private String productName;

    /**
     * Product SKU for reference
     */
    @Size(max = 100, message = "SKU cannot exceed 100 characters")
    private String productSku;

    /**
     * Product image URL for order confirmation
     */
    @Size(max = 500, message = "Image URL cannot exceed 500 characters")
    private String productImageUrl;

    /**
     * Whether this is a digital product
     */
    @Builder.Default
    private Boolean isDigital = false;

    /**
     * Weight per unit (kg) - important for shipping!
     */
    @DecimalMin(value = "0.0", message = "Weight cannot be negative")
    @Digits(integer = 8, fraction = 3, message = "Invalid weight format")
    private BigDecimal unitWeight;

    /**
     * Special instructions for this item
     */
    @Size(max = 500, message = "Item notes cannot exceed 500 characters")
    private String itemNotes;

    /**
     * Personalization text (for customizable products)
     */
    @Size(max = 200, message = "Personalization cannot exceed 200 characters")
    private String personalizationText;

    /**
     * Gift wrap requested
     */
    @Builder.Default
    private Boolean giftWrap = false;

    /**
     * Gift wrap price (if applicable)
     */
    @DecimalMin(value = "0.00", message = "Gift wrap price cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Invalid gift wrap price format")
    @Builder.Default
    private BigDecimal giftWrapPrice = BigDecimal.ZERO;

    /**
     * Whether this item is a gift
     */
    @Builder.Default
    private Boolean isGift = false;

    /**
     * Tax rate for this item (percentage)
     */
    @DecimalMin(value = "0.00", message = "Tax rate cannot be negative")
    @DecimalMax(value = "100.00", message = "Tax rate cannot exceed 100%")
    @Digits(integer = 3, fraction = 4, message = "Invalid tax rate format")
    private BigDecimal taxRate;

    /**
     * Tax amount for this line item
     */
    @DecimalMin(value = "0.00", message = "Tax amount cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Invalid tax amount format")
    private BigDecimal taxAmount;

    /**
     * Whether item requires shipping
     */
    @Builder.Default
    private Boolean requiresShipping = true;

    /**
     * Shipping class (affects shipping cost)
     */
    @Pattern(regexp = "^(STANDARD|FRAGILE|OVERSIZED|HAZMAT)?$",
            message = "Invalid shipping class")
    @Builder.Default
    private String shippingClass = "STANDARD";

    /**
     * Stock status at time of order
     */
    @Pattern(regexp = "^(IN_STOCK|BACKORDER|PREORDER)?$",
            message = "Invalid stock status")
    @Builder.Default
    private String stockStatus = "IN_STOCK";

    /**
     * Expected ship date (for backorders/preorders)
     */
    private String expectedShipDate;

    /**
     * Vendor/supplier (for marketplace orders)
     */
    @Size(max = 100, message = "Vendor name cannot exceed 100 characters")
    private String vendorName;

    /**
     * Vendor ID (for marketplace orders)
     */
    private Long vendorId;

    // Calculated fields - not stored but computed

    /**
     * Calculate line subtotal (quantity * unit price)
     */
    public BigDecimal getLineSubtotal() {
        if (unitPrice == null || quantity == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));

        // Add gift wrap if applicable
        if (Boolean.TRUE.equals(giftWrap) && giftWrapPrice != null) {
            subtotal = subtotal.add(giftWrapPrice);
        }

        return subtotal.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate tax for this line item
     */
    public BigDecimal calculateTax() {
        if (taxAmount != null) {
            return taxAmount;
        }

        if (taxRate == null || taxRate.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal subtotal = getLineSubtotal();
        return subtotal.multiply(taxRate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate line total (subtotal + tax)
     */
    public BigDecimal getLineTotal() {
        BigDecimal subtotal = getLineSubtotal();
        BigDecimal tax = calculateTax();
        return subtotal.add(tax).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Get total weight for this line item
     */
    public BigDecimal getTotalWeight() {
        if (unitWeight == null || quantity == null || Boolean.TRUE.equals(isDigital)) {
            return BigDecimal.ZERO;
        }
        return unitWeight.multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * Check if item is available for immediate shipping
     */
    public boolean isAvailableNow() {
        return "IN_STOCK".equals(stockStatus);
    }

    /**
     * Check if item is on backorder
     */
    public boolean isBackorder() {
        return "BACKORDER".equals(stockStatus);
    }

    /**
     * Check if item is preorder
     */
    public boolean isPreorder() {
        return "PREORDER".equals(stockStatus);
    }

    /**
     * Check if item needs special handling
     */
    public boolean needsSpecialHandling() {
        return "FRAGILE".equals(shippingClass) ||
                "HAZMAT".equals(shippingClass) ||
                "OVERSIZED".equals(shippingClass);
    }

    /**
     * Get display name with SKU
     */
    public String getDisplayName() {
        if (productName == null || productName.trim().isEmpty()) {
            return "Product #" + productId;
        }

        if (productSku != null && !productSku.trim().isEmpty()) {
            return productName + " (" + productSku + ")";
        }

        return productName;
    }

    /**
     * Get short description for notifications
     */
    public String getShortDescription() {
        return quantity + "x " + getDisplayName();
    }

    /**
     * Validate that gift wrap price is provided if gift wrap is requested
     */
    @AssertTrue(message = "Gift wrap price required when gift wrap is selected")
    private boolean isGiftWrapValid() {
        if (!Boolean.TRUE.equals(giftWrap)) {
            return true;
        }
        return giftWrapPrice != null && giftWrapPrice.compareTo(BigDecimal.ZERO) >= 0;
    }

    /**
     * Validate that expected ship date is provided for backorders/preorders
     */
    @AssertTrue(message = "Expected ship date required for backorders and preorders")
    private boolean isExpectedShipDateValid() {
        if ("IN_STOCK".equals(stockStatus)) {
            return true;
        }
        return expectedShipDate != null && !expectedShipDate.trim().isEmpty();
    }

    /**
     * Validate that digital products don't have shipping weight
     */
    @AssertTrue(message = "Digital products should not have weight")
    private boolean isDigitalProductValid() {
        if (!Boolean.TRUE.equals(isDigital)) {
            return true;
        }
        return unitWeight == null || unitWeight.compareTo(BigDecimal.ZERO) == 0;
    }

    // Version 2.0 fields - commented out
    /*
    // Discounts - Version 2.0
    private BigDecimal discountAmount;
    private Integer discountPercent;
    private String couponCode;

    // Subscription - Version 2.0
    private Boolean isSubscriptionItem;
    private String subscriptionFrequency;
    private Integer subscriptionDuration;

    // Bundling - Version 2.0
    private Long bundleId;
    private List<Long> bundleItemIds;

    // Warranty - Version 2.0
    private Boolean warrantyIncluded;
    private Integer warrantyMonths;
    private BigDecimal warrantyPrice;
    */
}