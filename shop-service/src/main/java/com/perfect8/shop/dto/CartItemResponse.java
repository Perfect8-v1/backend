package com.perfect8.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for individual cart items.
 * Used in CartResponse to represent items in the shopping cart.
 * FIXED: Changed 'id' to 'cartItemId' (Magnum Opus principle)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {

    /**
     * Cart item ID
     * FIXED: Changed from 'id' to 'cartItemId' for clarity (Magnum Opus principle)
     */
    private Long cartItemId;

    /**
     * Product ID
     */
    private Long productId;

    /**
     * Product name for display
     */
    private String productName;

    /**
     * Product SKU
     */
    private String productSku;

    /**
     * Product image URL
     */
    private String imageUrl;

    /**
     * Quantity in cart
     */
    private Integer quantity;

    /**
     * Unit price at time of adding to cart
     */
    private BigDecimal unitPrice;

    /**
     * Total price for this line item (quantity * unitPrice)
     */
    private BigDecimal totalPrice;

    /**
     * Current stock available
     */
    private Integer stockAvailable;

    /**
     * Maximum quantity allowed per order
     */
    private Integer maxQuantity;

    /**
     * Whether the item is in stock
     */
    @Builder.Default
    private Boolean inStock = true;

    /**
     * Whether the price has changed since adding to cart
     */
    @Builder.Default
    private Boolean priceChanged = false;

    /**
     * Current product price (for comparison with unitPrice)
     */
    private BigDecimal currentPrice;

    /**
     * Discount amount applied to this item
     */
    private BigDecimal discountAmount;

    /**
     * Discount percentage applied
     */
    private Integer discountPercent;

    /**
     * Weight of single item
     */
    private Double itemWeight;

    /**
     * Total weight for quantity
     */
    private Double totalWeight;

    /**
     * Any special notes for this item
     */
    private String notes;

    /**
     * Product category
     */
    private String category;

    /**
     * Whether item is eligible for promotions
     */
    @Builder.Default
    private Boolean promotionEligible = true;

    /**
     * Calculate total price if not set
     */
    public BigDecimal getTotalPrice() {
        if (totalPrice == null && unitPrice != null && quantity != null) {
            totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));

            if (discountAmount != null) {
                totalPrice = totalPrice.subtract(discountAmount);
            }
        }
        return totalPrice;
    }

    /**
     * Calculate total weight if not set
     */
    public Double getTotalWeight() {
        if (totalWeight == null && itemWeight != null && quantity != null) {
            totalWeight = itemWeight * quantity;
        }
        return totalWeight;
    }

    /**
     * Check if quantity is valid
     */
    public boolean isQuantityValid() {
        if (quantity == null || quantity <= 0) {
            return false;
        }

        if (stockAvailable != null && quantity > stockAvailable) {
            return false;
        }

        if (maxQuantity != null && quantity > maxQuantity) {
            return false;
        }

        return true;
    }

    /**
     * Check if item has discount
     */
    public boolean hasDiscount() {
        return (discountAmount != null && discountAmount.compareTo(BigDecimal.ZERO) > 0) ||
                (discountPercent != null && discountPercent > 0);
    }

    /**
     * Get effective unit price after discount
     */
    public BigDecimal getEffectiveUnitPrice() {
        if (unitPrice == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal effective = unitPrice;

        if (discountPercent != null && discountPercent > 0) {
            BigDecimal discount = unitPrice.multiply(BigDecimal.valueOf(discountPercent))
                    .divide(BigDecimal.valueOf(100));
            effective = effective.subtract(discount);
        } else if (discountAmount != null && quantity != null && quantity > 0) {
            BigDecimal unitDiscount = discountAmount.divide(BigDecimal.valueOf(quantity), 2, BigDecimal.ROUND_HALF_UP);
            effective = effective.subtract(unitDiscount);
        }

        return effective;
    }

    /**
     * Get display text for stock status
     */
    public String getStockStatusText() {
        if (!inStock) {
            return "Out of Stock";
        }

        if (stockAvailable != null) {
            if (stockAvailable <= 5) {
                return "Only " + stockAvailable + " left";
            } else if (stockAvailable <= 10) {
                return "Limited Stock";
            }
        }

        return "In Stock";
    }
}