package com.perfect8.email.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Order Item DTO for email service
 * Represents individual items in an order
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDto {

    private Long orderItemId;
    private Long productId;
    private String productName;
    private String productSku;
    private String productDescription;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal subtotal;
    private String productImageUrl;

    // Calculate subtotal
    public BigDecimal getSubtotal() {
        if (price != null && quantity != null) {
            return price.multiply(BigDecimal.valueOf(quantity));
        }
        return BigDecimal.ZERO;
    }

    // Helper method for display
    public String getDisplayName() {
        if (productName != null && productSku != null) {
            return productName + " (" + productSku + ")";
        }
        return productName != null ? productName : "Unknown Product";
    }

    @Override
    public String toString() {
        return "OrderItemDto{" +
                "productName='" + productName + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                ", subtotal=" + getSubtotal() +
                '}';
    }
}