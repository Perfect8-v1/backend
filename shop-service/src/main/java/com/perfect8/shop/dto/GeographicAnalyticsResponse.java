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
public class GeographicAnalyticsResponse {

    private String period;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private List<CountryMetrics> topCountries;
    private List<RegionMetrics> topRegions;
    private List<CityMetrics> topCities;

    private GeographicSummary summary;
    private LocalDateTime calculatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CountryMetrics {
        private String countryCode;
        private String countryName;
        private Integer customerCount;
        private Integer orderCount;
        private BigDecimal revenue;
        private Double conversionRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegionMetrics {
        private String region;
        private String country;
        private Integer customerCount;
        private Integer orderCount;
        private BigDecimal revenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CityMetrics {
        private String city;
        private String region;
        private String country;
        private Integer customerCount;
        private Integer orderCount;
        private BigDecimal revenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeographicSummary {
        private Integer totalCountries;
        private Integer totalRegions;
        private Integer totalCities;
        private String topMarket;
        private BigDecimal topMarketRevenue;
    }
}

 */