/*
package com.perfect8.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerMetricsResponse {

    private Integer totalCustomers;
    private Integer activeCustomers;
    private Integer newCustomers;
    private Integer inactiveCustomers;

    private String period;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private Double customerGrowthRate;
    private Double customerRetentionRate;
    private Double customerChurnRate;

    private Integer verifiedCustomers;
    private Integer unverifiedCustomers;

    private List<CustomerSegment> customerSegments;
    private List<CustomerGrowthData> growthData;

    private LocalDateTime calculatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerSegment {
        private String segmentName;
        private Integer customerCount;
        private Double percentage;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerGrowthData {
        private String period;
        private LocalDateTime date;
        private Integer newCustomers;
        private Integer totalCustomers;
        private Double growthRate;
    }
}

 */