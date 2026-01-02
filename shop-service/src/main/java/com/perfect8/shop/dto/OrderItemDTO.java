package com.perfect8.shop.dto;

import lombok.*;
import java.math.BigDecimal;

/**
 * Data Transfer Object for Order Item
 * Version 1.0 - Core functionality only
 *
 * Uses descriptive field names (orderItemId instead of customerEmailDTOId) for clarity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {

    private Long orderItemId;
    private Long orderId;
    private Long productId;
    private String productName;
    private String productSku;
    private String productDescription;

    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;

    // Status for individual item tracking
    private String itemStatus;
    private Integer shippedQuantity;
    private Integer returnedQuantity;

    // Digital product flag - needed by OrderRequestDTO
    private Boolean isDigital;

    // Notes
    private String notes;

    // ========================================
    // Helper methods
    // ========================================

    /**
     * Get isDigital flag (needed by OrderRequestDTO)
     * @return true if product is digital, false if null
     */
    public Boolean getIsDigital() {
        return this.isDigital != null ? this.isDigital : Boolean.FALSE;
    }

    /**
     * Calculate total including tax
     * @return subtotal + tax amount
     */
    public BigDecimal getTotal() {
        BigDecimal sub = subtotal != null ? subtotal : BigDecimal.ZERO;
        BigDecimal tax = taxAmount != null ? taxAmount : BigDecimal.ZERO;
        return sub.add(tax);
    }

    /**
     * Check if item is fully shipped
     * @return true if all quantity has been shipped
     */
    public boolean isFullyShipped() {
        return shippedQuantity != null && shippedQuantity.equals(quantity);
    }

    /**
     * Check if item is partially shipped
     * @return true if some but not all quantity has been shipped
     */
    public boolean isPartiallyShipped() {
        return shippedQuantity != null && shippedQuantity > 0 && shippedQuantity < quantity;
    }

    // Version 2.0 - Commented out for future
    // private BigDecimal discountAmount;
    // private BigDecimal discountPercentage;
    // private String couponCode;
}