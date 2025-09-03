package com.perfect8.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRecommendationResponse {

    private Long productId;
    private String name;
    private String description;
    private String shortDescription;
    private String sku;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private BigDecimal discountAmount;
    private BigDecimal discountPercentage;
    private String currency;
    private String imageUrl;
    private List<String> imageUrls;
    private String category;
    private String brand;
    private Double rating;
    private Integer reviewCount;
    private Boolean inStock;
    private Integer stockQuantity;
    private String recommendationType;
    private String recommendationReason;
    private Double confidenceScore;
    private Integer priority;
    private String algorithm;
    private Boolean isPersonalized;
    private List<String> tags;
    private String url;
    private Boolean isFeatured;
    private Boolean isOnSale;
    private Boolean isNew;
    private Boolean isBestseller;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String size;
    private String color;
    private List<String> availableSizes;
    private List<String> availableColors;
    private BigDecimal weight;
    private String dimensions;
    private String manufacturer;
    private String warranty;
    private Boolean isFreeShipping;
    private Integer deliveryDays;
}