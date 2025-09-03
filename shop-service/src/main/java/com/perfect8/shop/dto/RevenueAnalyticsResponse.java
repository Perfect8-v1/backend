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
public class RevenueAnalyticsResponse {

    private BigDecimal totalRevenue;
    private BigDecimal netRevenue;
    private BigDecimal grossRevenue;

    private String period;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private BigDecimal averageOrderValue;
    private BigDecimal revenueGrowthRate;
    private BigDecimal previousPeriodRevenue;

    private List<RevenueByPeriod> dailyRevenue;
    private List<RevenueByPeriod> weeklyRevenue;
    private List<RevenueByPeriod> monthlyRevenue;

    private List<RevenueBySource> revenueBySource;
    private List<RevenueByCategory> revenueByCategory;

    private RevenueMetrics metrics;
    private LocalDateTime calculatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueByPeriod {
        private String period;
        private LocalDateTime date;
        private BigDecimal revenue;
        private Integer orderCount;
        private BigDecimal averageOrderValue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueBySource {
        private String source;
        private BigDecimal revenue;
        private Integer orderCount;
        private Double percentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueByCategory {
        private Long categoryId;
        private String categoryName;
        private BigDecimal revenue;
        private Integer unitsSold;
        private Double percentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueMetrics {
        private BigDecimal recurringRevenue;
        private BigDecimal oneTimeRevenue;
        private Double customerLifetimeValue;
        private BigDecimal revenuePerCustomer;
        private Double churnRate;
    }
}

 */