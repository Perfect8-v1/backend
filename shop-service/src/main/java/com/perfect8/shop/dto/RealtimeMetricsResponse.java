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
public class RealtimeMetricsResponse {

    private LocalDateTime timestamp;

    // Current activity metrics
    private Long activeUsers;
    private Integer todayOrders;
    private BigDecimal todayRevenue;

    // Real-time counters
    private Integer onlineCustomers;
    private Integer pendingOrders;
    private Integer processingOrders;
    private Integer lowStockProducts;

    // Performance metrics
    private Double systemLoad;
    private Integer responseTimeMs;
    private Double errorRate;

    // Recent activity
    private List<RecentActivity> recentOrders;
    private List<RecentActivity> recentCustomers;
    private List<SystemAlert> systemAlerts;

    // Live statistics
    private LiveStatistics liveStats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentActivity {
        private Long customerEmailDTOId;
        private String type;
        private String description;
        private LocalDateTime timestamp;
        private BigDecimal amount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SystemAlert {
        private String type;
        private String message;
        private String severity;
        private LocalDateTime timestamp;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LiveStatistics {
        private BigDecimal averageOrderValue;
        private Double conversionRate;
        private Integer cartAbandonmentCount;
        private Integer searchQueries;
        private List<String> topSearchTerms;
    }
}

 */