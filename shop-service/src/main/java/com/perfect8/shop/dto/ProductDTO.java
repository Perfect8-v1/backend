package com.perfect8.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Product DTO
 * Version 1.0 - Core product data transfer
 * FIXED: Changed 'customerEmailDTOId' to 'productId', 'createdDate/updatedDate' to 'createdDate/updatedDate' (Magnum Opus principle)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {

    /**
     * FIXED: Changed from 'customerEmailDTOId' to 'productId' for clarity (Magnum Opus principle)
     */
    private Long productId;

    @NotBlank(message = "Product name is required")
    @Size(max = 200, message = "Product name must not exceed 200 characters")
    private String name;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.00", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Invalid price format")
    private BigDecimal price;

    @DecimalMin(value = "0.00", inclusive = true, message = "Discount price cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Invalid discount price format")
    private BigDecimal discountPrice;

    @NotBlank(message = "SKU is required")
    @Size(max = 100, message = "SKU must not exceed 100 characters")
    private String sku;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    private String imageUrl;

    @NotNull(message = "Category is required")
    private Long categoryId;

    private boolean featured;
    private boolean active;

    @DecimalMin(value = "0.00", inclusive = true, message = "Weight cannot be negative")
    private BigDecimal weight;

    // FIXED: Changed from String to List<String> to match Product entity
    private List<String> dimensions;

    private List<String> tags;

    private String brand;
    private String manufacturer;
    private String model;

    @Size(max = 50, message = "Status must not exceed 50 characters")
    private String status;

    /**
     * FIXED: Changed from 'createdDate' to 'createdDate' for consistency with entities (Magnum Opus principle)
     */
    private LocalDateTime createdDate;

    /**
     * FIXED: Changed from 'updatedDate' to 'updatedDate' for consistency with entities (Magnum Opus principle)
     */
    private LocalDateTime updatedDate;

    // Additional product information
    private String barcode;
    private String color;
    private String size;
    private String material;

    // SEO fields
    private String metaTitle;
    private String metaDescription;
    private String metaKeywords;

    // Inventory tracking
    private Integer lowStockThreshold;
    private Integer reorderPoint;
    private Integer reorderQuantity;

    // Pricing options
    private BigDecimal costPrice;
    private BigDecimal compareAtPrice;
    private Integer taxClassId;

    // Shipping information
    private BigDecimal shippingWeight;
    private BigDecimal shippingLength;
    private BigDecimal shippingWidth;
    private BigDecimal shippingHeight;
    private String shippingClass;

    // Product variants (for future use)
    private List<ProductVariantDTO> variants;

    // Images gallery
    private List<String> galleryImages;

    // Product options
    private List<ProductOptionDTO> options;

    // Related products
    private List<Long> relatedProductIds;

    // Cross-sell and up-sell
    private List<Long> crossSellProductIds;
    private List<Long> upSellProductIds;

    // Ratings and reviews summary
    private Double averageRating;
    private Integer reviewCount;

    // Validation helpers
    public boolean hasDiscount() {
        return discountPrice != null &&
                discountPrice.compareTo(BigDecimal.ZERO) > 0 &&
                discountPrice.compareTo(price) < 0;
    }

    public BigDecimal getEffectivePrice() {
        if (hasDiscount()) {
            return discountPrice;
        }
        return price;
    }

    public BigDecimal getDiscountPercentage() {
        if (!hasDiscount()) {
            return BigDecimal.ZERO;
        }
        BigDecimal discount = price.subtract(discountPrice);
        return discount.divide(price, 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    public boolean isInStock() {
        return stockQuantity != null && stockQuantity > 0;
    }

    public boolean isLowStock() {
        if (lowStockThreshold == null) {
            return stockQuantity != null && stockQuantity < 10;
        }
        return stockQuantity != null && stockQuantity <= lowStockThreshold;
    }

    public boolean needsReorder() {
        if (reorderPoint == null) {
            return isLowStock();
        }
        return stockQuantity != null && stockQuantity <= reorderPoint;
    }

    // Nested DTOs for complex fields

    /**
     * Product Variant DTO
     * FIXED: Changed 'customerEmailDTOId' to 'productVariantId' (Magnum Opus principle)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductVariantDTO {
        /**
         * FIXED: Changed from 'customerEmailDTOId' to 'productVariantId' for clarity (Magnum Opus principle)
         */
        private Long productVariantId;
        private String name;
        private String sku;
        private BigDecimal price;
        private Integer stockQuantity;
        private String color;
        private String size;
        private String imageUrl;
        private boolean active;
    }

    /**
     * Product Option DTO
     * FIXED: Changed 'customerEmailDTOId' to 'productOptionId' (Magnum Opus principle)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductOptionDTO {
        /**
         * FIXED: Changed from 'customerEmailDTOId' to 'productOptionId' for clarity (Magnum Opus principle)
         */
        private Long productOptionId;
        private String name;
        private String type; // "color", "size", "material", etc.
        private List<String> values;
        private boolean required;
    }
}