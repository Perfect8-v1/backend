package com.perfect8.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Request DTO for creating a new product
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateRequest {

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

    // Weight as Double for easy input from frontend
    @DecimalMin(value = "0.00", inclusive = true, message = "Weight cannot be negative")
    private Double weight;

    // FIXED: Changed from String to List<String>
    private List<String> dimensions;  // e.g., ["10cm", "20cm", "30cm"] for L x W x H

    private List<String> tags;

    // Additional product information
    private String brand;
    private String manufacturer;
    private String model;
    private String barcode;
    private String color;
    private String size;
    private String material;

    // SEO fields
    private String metaTitle;
    private String metaDescription;
    private String metaKeywords;

    // Images gallery
    private List<String> galleryImages;

    // Related products
    private List<Long> relatedProductIds;
}