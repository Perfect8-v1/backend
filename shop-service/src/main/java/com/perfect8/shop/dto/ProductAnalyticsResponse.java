/*
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
public class ProductAnalyticsResponse {

    private Integer totalProducts;
    private Integer activeProducts;
    private Integer inactiveProducts;

    private String period;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private Integer lowStockProducts;
    private Integer outOfStockProducts;
    private BigDecimal totalInventoryValue;

    private List<TopProduct> topSellingProducts;
    private List<TopProduct> worstPerformingProducts;
    private List<CategoryPerformance> categoryPerformance;

    private ProductMetrics metrics;
    private LocalDateTime calculatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopProduct {
        private Long productId;
        private String productName;
        private String sku;
        private Integer unitsSold;
        private BigDecimal revenue;
        private String category;
        private Integer viewCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryPerformance {
        private Long categoryId;
        private String categoryName;
        private Integer productCount;
        private Integer unitsSold;
        private BigDecimal revenue;
        private Double averageRating;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductMetrics {
        private BigDecimal averagePrice;
        private BigDecimal averageRating;
        private Double averageViewsPerProduct;
        private Double averageSalesPerProduct;
        private Double inventoryTurnoverRate;
    }
}

 */