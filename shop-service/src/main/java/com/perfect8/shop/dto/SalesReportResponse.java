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
public class SalesReportResponse {

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String format;

    private BigDecimal totalRevenue;
    private Long totalOrders;
    private Integer totalCustomers;

    private List<SalesByPeriod> salesBreakdown;
    private List<ProductSales> topProducts;
    private List<CustomerSales> topCustomers;

    private SalesStatistics statistics;
    private LocalDateTime generatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalesByPeriod {
        private String period;
        private LocalDateTime date;
        private BigDecimal revenue;
        private Long orders;
        private BigDecimal averageOrderValue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductSales {
        private Long productId;
        private String productName;
        private String sku;
        private Integer unitsSold;
        private BigDecimal revenue;
        private String category;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerSales {
        private Long customerId;
        private String customerName;
        private String customerEmail;
        private BigDecimal totalRevenue;
        private Integer orderCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalesStatistics {
        private BigDecimal averageOrderValue;
        private Double conversionRate;
        private BigDecimal highestOrderValue;
        private BigDecimal lowestOrderValue;
        private Double repeatCustomerRate;
    }
}

 */