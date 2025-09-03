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
public class ExecutiveSummaryResponse {

    private String period;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private BigDecimal totalRevenue;
    private Long totalOrders;
    private Long totalCustomers;

    private PerformanceMetrics performance;
    private List<KeyInsight> keyInsights;
    private List<RecommendedAction> recommendations;

    private ComparisonMetrics comparison;
    private LocalDateTime generatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerformanceMetrics {
        private BigDecimal revenueGrowth;
        private Double customerGrowth;
        private Double orderGrowth;
        private BigDecimal averageOrderValue;
        private Double conversionRate;
        private Double customerRetentionRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KeyInsight {
        private String title;
        private String description;
        private String category;
        private String impact;
        private BigDecimal value;
        private String trend;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendedAction {
        private String title;
        private String description;
        private String priority;
        private String category;
        private BigDecimal estimatedImpact;
        private Integer effortLevel;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComparisonMetrics {
        private BigDecimal previousPeriodRevenue;
        private Long previousPeriodOrders;
        private Long previousPeriodCustomers;
        private Double revenueChangePercent;
        private Double ordersChangePercent;
        private Double customersChangePercent;
    }
}

 */