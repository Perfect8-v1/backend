package com.perfect8.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for cart operations response.
 * Used by CartService and CartController.
 * FIXED: Uses createdDate/updatedDate to match Cart entity (Magnum Opus)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {

    /**
     * Cart ID
     */
    private Long cartId;

    /**
     * Customer ID who owns the cart
     */
    private Long customerId;

    /**
     * List of items in the cart
     */
    private List<CartItemResponse> items;

    /**
     * Total amount for all items (before tax and shipping)
     */
    private BigDecimal totalAmount;

    /**
     * Number of items in cart
     */
    private Integer itemCount;

    /**
     * Total quantity of all items
     */
    private Integer totalQuantity;

    /**
     * Applied coupon code
     */
    private String couponCode;

    /**
     * Discount amount from coupon
     */
    private BigDecimal discountAmount;

    /**
     * Subtotal after discount
     */
    private BigDecimal subtotalAfterDiscount;

    /**
     * Estimated tax amount
     */
    private BigDecimal estimatedTax;

    /**
     * Estimated shipping cost
     */
    private BigDecimal estimatedShipping;

    /**
     * Grand total (subtotal + tax + shipping - discount)
     */
    private BigDecimal grandTotal;

    /**
     * When the cart was created
     * FIXED: Changed from createdAt to createdDate (Magnum Opus - match Cart entity)
     */
    private LocalDateTime createdDate;

    /**
     * When the cart was last updated
     * FIXED: Changed from updatedAt to updatedDate (Magnum Opus - match Cart entity)
     */
    private LocalDateTime updatedDate;

    /**
     * Whether the cart is ready for checkout
     */
    @Builder.Default
    private Boolean checkoutReady = false;

    /**
     * Any validation messages
     */
    private List<String> validationMessages;

    /**
     * Calculate total quantity if not set
     */
    public Integer getTotalQuantity() {
        if (totalQuantity == null && items != null) {
            totalQuantity = items.stream()
                    .mapToInt(CartItemResponse::getQuantity)
                    .sum();
        }
        return totalQuantity;
    }

    /**
     * Calculate grand total if not set
     */
    public BigDecimal getGrandTotal() {
        if (grandTotal == null) {
            BigDecimal total = totalAmount != null ? totalAmount : BigDecimal.ZERO;

            if (discountAmount != null) {
                total = total.subtract(discountAmount);
            }

            if (estimatedTax != null) {
                total = total.add(estimatedTax);
            }

            if (estimatedShipping != null) {
                total = total.add(estimatedShipping);
            }

            grandTotal = total;
        }
        return grandTotal;
    }

    /**
     * Check if cart is empty
     */
    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }

    /**
     * Check if cart has discount
     */
    public boolean hasDiscount() {
        return discountAmount != null && discountAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Get item count (fallback to items list size if not set)
     */
    public Integer getItemCount() {
        if (itemCount == null && items != null) {
            itemCount = items.size();
        }
        return itemCount;
    }
}
