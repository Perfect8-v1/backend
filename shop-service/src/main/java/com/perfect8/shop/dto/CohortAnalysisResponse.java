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
public class CohortAnalysisResponse {

    private Integer months;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private List<CohortData> customerCohorts;
    private List<RevenueCohortData> revenueCohorts;

    private CohortSummary summary;
    private LocalDateTime calculatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CohortData {
        private String cohortPeriod;
        private LocalDateTime cohortDate;
        private Integer initialCustomers;
        private List<RetentionPeriod> retentionData;
        private Double averageRetentionRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueCohortData {
        private String cohortPeriod;
        private LocalDateTime cohortDate;
        private BigDecimal initialRevenue;
        private List<RevenuePeriod> revenueData;
        private BigDecimal averageRevenuePerCustomer;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RetentionPeriod {
        private Integer periodNumber;
        private String periodName;
        private Integer activeCustomers;
        private Double retentionRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenuePeriod {
        private Integer periodNumber;
        private String periodName;
        private BigDecimal revenue;
        private BigDecimal revenuePerCustomer;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CohortSummary {
        private Integer totalCohorts;
        private Double overallRetentionRate;
        private BigDecimal averageLifetimeValue;
        private String bestPerformingCohort;
        private String worstPerformingCohort;
    }
}

 */