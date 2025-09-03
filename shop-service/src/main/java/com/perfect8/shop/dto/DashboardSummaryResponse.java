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
public class DashboardSummaryResponse {

    private String period;
    private BigDecimal totalRevenue;
    private Long totalOrders;
    private Long totalCustomers;
    private Double conversionRate;

    private QuickStats quickStats;
    private List<TrendData> trends;
    private List<TopPerformer> topPerformers;

    private LocalDateTime generatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuickStats {
        private Integer todayOrders;
        private BigDecimal todayRevenue;
        private Integer activeCustomers;
        private Integer pendingOrders;
        private Integer lowStockProducts;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendData {
        private String metric;
        private Double changePercentage;
        private String direction;
        private String period;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopPerformer {
        private String type;
        private String name;
        private BigDecimal value;
        private String metric;
    }
}
*/