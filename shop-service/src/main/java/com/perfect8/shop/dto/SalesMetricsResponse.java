/*package com.perfect8.shop.dto;

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
public class SalesMetricsResponse {

    // Fixed: Changed to Long to match AnalyticsService expectations
    private Long totalOrders;
    private Long totalCustomers;
    private Long totalProducts;
    private BigDecimal totalRevenue;

    // Fixed: Changed to Long for consistency
    private Long ordersToday;
    private Long ordersThisWeek;
    private Long ordersThisMonth;

    private BigDecimal revenueToday;
    private BigDecimal revenueThisWeek;
    private BigDecimal revenueThisMonth;

    private BigDecimal averageOrderValue;
    private Double conversionRate;

    // Added fields that AnalyticsService expects
    private String period;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private List<TopProduct> topProducts;
    private List<SalesMetric> salesByPeriod;

    private LocalDateTime calculatedAt;
    private String currency;

    // Explicit setter methods for backward compatibility
    public void setTotalCustomers(long totalCustomers) {
        this.totalCustomers = totalCustomers;
    }

    public void setTotalProducts(long totalProducts) {
        this.totalProducts = totalProducts;
    }

    // Fixed: Add setter that accepts Long directly
    public void setTotalOrders(Long totalOrders) {
        this.totalOrders = totalOrders;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopProduct {
        private Long productId;
        private String productName;
        private Long unitsSold; // Changed to Long for consistency
        private BigDecimal revenue;
        private String category;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalesMetric {
        private String period;
        private LocalDateTime date;
        private Long orders; // Changed to Long for consistency
        private BigDecimal revenue;
        private Long customers; // Changed to Long for consistency
    }
}

*/