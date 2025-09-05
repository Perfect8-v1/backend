package com.perfect8.shop.dto;

import lombok.*;
import java.math.BigDecimal;

/**
 * Data Transfer Object for Order Item
 * Version 1.0 - Core functionality only
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {

    private Long id;
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

    // Notes
    private String notes;

    // Version 2.0 - Commented out for future
    // private BigDecimal discountAmount;
    // private BigDecimal discountPercentage;
    // private String couponCode;

    // Helper methods
    public BigDecimal getTotal() {
        BigDecimal sub = subtotal != null ? subtotal : BigDecimal.ZERO;
        BigDecimal tax = taxAmount != null ? taxAmount : BigDecimal.ZERO;
        return sub.add(tax);
    }

    public boolean isFullyShipped() {
        return shippedQuantity != null && shippedQuantity.equals(quantity);
    }

    public boolean isPartiallyShipped() {
        return shippedQuantity != null && shippedQuantity > 0 && shippedQuantity < quantity;
    }
}