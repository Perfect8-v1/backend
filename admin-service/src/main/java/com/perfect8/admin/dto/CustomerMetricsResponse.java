package com.perfect8.admin.dto;

/**
 * VERSION 2.0 - COMMENTED OUT FOR PHASE 1
 *
 * Customer metrics response DTO
 * Will be enabled in version 2.0 when analytics features are added
 */
public class CustomerMetricsResponse {

    /* VERSION 2.0 - CUSTOMER ANALYTICS

    private Long totalCustomers;
    private Long activeCustomers;
    private Long newCustomersThisMonth;
    private Double averageOrderValue;
    private Double customerLifetimeValue;
    private Double churnRate;
    private Map<String, Long> customersByCountry;
    private Map<String, Long> customersBySegment;
    private List<TopCustomerDto> topCustomers;
    private CustomerGrowthDto growthMetrics;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopCustomerDto {
        private Long customerId;
        private String customerName;
        private String email;
        private BigDecimal totalSpent;
        private Integer orderCount;
        private LocalDateTime lastOrderDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerGrowthDto {
        private Long customersLastMonth;
        private Long customersThisMonth;
        private Double growthPercentage;
        private List<DailyGrowth> dailyGrowth;

        @Data
        @Builder
        public static class DailyGrowth {
            private LocalDate date;
            private Long newCustomers;
            private Long totalCustomers;
        }
    }

    */

    // Temporary placeholder for compilation
    public String getVersion() {
        return "2.0 - Customer metrics disabled in v1.0";
    }
}