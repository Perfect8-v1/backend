package com.perfect8.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for Product information
 * Version 1.0 - Core product display functionality
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String description;
    private String shortDescription;
    private String sku;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private String brand;
    private String imageUrl;
    private List<String> galleryImages;
    private Integer stockQuantity;
    private Boolean featured;
    private Boolean active;
    private String category;  // Can be category name
    private String categoryName;  // Alternative field name
    private Long categoryId;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    // Additional fields for v1.0
    private BigDecimal weight;
    private String dimensions;
    private List<String> tags;

    // Calculated fields
    private BigDecimal discountPercentage;
    private boolean inStock;
    private String availability;

    /**
     * Calculate discount percentage if discount price exists
     */
    public BigDecimal getDiscountPercentage() {
        if (price != null && discountPrice != null && price.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discount = price.subtract(discountPrice);
            return discount.divide(price, 2, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
        return BigDecimal.ZERO;
    }

    /**
     * Check if product is in stock
     */
    public boolean isInStock() {
        return stockQuantity != null && stockQuantity > 0;
    }

    /**
     * Get availability status string
     */
    public String getAvailability() {
        if (stockQuantity == null || stockQuantity <= 0) {
            return "Out of Stock";
        } else if (stockQuantity <= 5) {
            return "Limited Stock";
        } else {
            return "In Stock";
        }
    }

    /**
     * Get effective price (discount price if available, otherwise regular price)
     */
    public BigDecimal getEffectivePrice() {
        if (discountPrice != null && discountPrice.compareTo(BigDecimal.ZERO) > 0) {
            return discountPrice;
        }
        return price;
    }

    /**
     * Check if product has discount
     */
    public boolean hasDiscount() {
        return discountPrice != null &&
                discountPrice.compareTo(BigDecimal.ZERO) > 0 &&
                discountPrice.compareTo(price) < 0;
    }

    /**
     * Get price display text
     */
    public String getPriceDisplay() {
        if (hasDiscount()) {
            return "$" + discountPrice + " (was $" + price + ")";
        }
        return "$" + price;
    }

    /**
     * Check if this is a featured product
     */
    public boolean isFeatured() {
        return Boolean.TRUE.equals(featured);
    }

    /**
     * Check if this product is active
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(active);
    }
}