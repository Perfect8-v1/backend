package com.perfect8.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerReportResponse {

    private String reportType;
    private String reportPeriod;
    private LocalDateTime generatedAt;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer totalCustomers;
    private Integer newCustomers;
    private Integer activeCustomers;
    private Integer churmedCustomers;
    private Integer returningCustomers;
    private Double customerGrowthRate;
    private Double customerRetentionRate;
    private Double customerChurnRate;
    private BigDecimal averageCustomerValue;
    private BigDecimal customerLifetimeValue;
    private BigDecimal customerAcquisitionCost;
    private Integer averageOrdersPerCustomer;
    private BigDecimal averageOrderValue;
    private Double repeatPurchaseRate;
    private Integer daysBetweenPurchases;
    private List<CustomerSegment> customerSegments;
    private List<AcquisitionChannel> acquisitionChannels;
    private List<GeographicData> geographicBreakdown;
    private List<AgeGroup> ageGroups;
    private Map<String, Integer> customersByCountry;
    private Map<String, Integer> customersByState;
    private Map<String, BigDecimal> revenueBySegment;
    private List<TopCustomer> topCustomers;
    private List<String> insights;
    private List<Recommendation> recommendations;
    private Map<String, Object> customMetrics;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerSegment {
        private String segmentName;
        private Integer customerCount;
        private Double percentage;
        private BigDecimal averageValue;
        private BigDecimal totalRevenue;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AcquisitionChannel {
        private String channelName;
        private Integer newCustomers;
        private Double percentage;
        private BigDecimal acquisitionCost;
        private BigDecimal lifetimeValue;
        private Double conversionRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeographicData {
        private String location;
        private String type;
        private Integer customerCount;
        private BigDecimal revenue;
        private Double percentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgeGroup {
        private String ageRange;
        private Integer customerCount;
        private Double percentage;
        private BigDecimal averageSpending;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopCustomer {
        private Long customerId;
        private String customerName;
        private String email;
        private BigDecimal totalSpent;
        private Integer orderCount;
        private LocalDateTime lastOrder;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Recommendation {
        private String category;
        private String title;
        private String description;
        private String priority;
        private String actionType;
    }
}