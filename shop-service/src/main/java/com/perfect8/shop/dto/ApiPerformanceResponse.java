/*
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
public class ApiPerformanceResponse {

    private String apiName;
    private String version;
    private LocalDateTime timestamp;
    private String timeRange;
    private Integer totalRequests;
    private Integer successfulRequests;
    private Integer failedRequests;
    private Double successRate;
    private Double errorRate;
    private Double averageResponseTime;
    private Double medianResponseTime;
    private Double p95ResponseTime;
    private Double p99ResponseTime;
    private Double minResponseTime;
    private Double maxResponseTime;
    private Integer requestsPerSecond;
    private Integer requestsPerMinute;
    private Integer requestsPerHour;
    private Double throughput;
    private Integer activeConnections;
    private Integer peakConcurrentUsers;
    private Long totalDataTransferred;
    private Long averagePayloadSize;
    private Map<String, Integer> statusCodeBreakdown;
    private Map<String, Double> endpointPerformance;
    private List<EndpointMetrics> topEndpoints;
    private List<ErrorDetails> topErrors;
    private Double cacheHitRate;
    private Double cacheMissRate;
    private Integer rateLimitViolations;
    private Integer timeoutErrors;
    private Integer connectionErrors;
    private String slowestEndpoint;
    private Double slowestResponseTime;
    private String mostUsedEndpoint;
    private Integer mostUsedEndpointCount;
    private List<String> alerts;
    private Map<String, Object> customMetrics;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EndpointMetrics {
        private String endpoint;
        private String method;
        private Integer requestCount;
        private Double averageResponseTime;
        private Double errorRate;
        private Double successRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorDetails {
        private String endpoint;
        private String errorType;
        private String errorMessage;
        private Integer count;
        private Double percentage;
        private LocalDateTime lastOccurrence;
    }
}

 */