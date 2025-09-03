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
public class TrendAnalysisResponse {

    private String metric;
    private String period;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private TrendDirection trendDirection;
    private Double trendStrength;
    private String trendDescription;

    private List<TrendDataPoint> dataPoints;
    private TrendStatistics statistics;

    private List<TrendPrediction> predictions;
    private LocalDateTime calculatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendDataPoint {
        private LocalDateTime timestamp;
        private BigDecimal value;
        private String period;
        private Double changeFromPrevious;
        private Double changePercentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendStatistics {
        private BigDecimal average;
        private BigDecimal median;
        private BigDecimal minimum;
        private BigDecimal maximum;
        private Double volatility;
        private Double correlation;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendPrediction {
        private LocalDateTime predictedDate;
        private BigDecimal predictedValue;
        private Double confidenceLevel;
        private String predictionModel;
    }

    public enum TrendDirection {
        UPWARD,
        DOWNWARD,
        STABLE,
        VOLATILE
    }
}

 */