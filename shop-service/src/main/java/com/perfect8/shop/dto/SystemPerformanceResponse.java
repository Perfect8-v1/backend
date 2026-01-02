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
public class SystemPerformanceResponse {

    private String systemName;
    private String version;
    private String environment;
    private LocalDateTime timestamp;
    private String status;
    private String healthStatus;
    private Double cpuUsage;
    private Double memoryUsage;
    private Double diskUsage;
    private Double networkUsage;
    private Long totalMemory;
    private Long availableMemory;
    private Long usedMemory;
    private Long totalDiskSpace;
    private Long availableDiskSpace;
    private Long usedDiskSpace;
    private Integer activeConnections;
    private Integer totalConnections;
    private Integer queueSize;
    private Integer threadCount;
    private Integer activeThreads;
    private Double responseTime;
    private Double averageResponseTime;
    private Double throughput;
    private Integer requestsPerSecond;
    private Integer errorRate;
    private Integer uptime;
    private LocalDateTime lastRestart;
    private String jvmVersion;
    private String jvmVendor;
    private Long heapSize;
    private Long nonHeapSize;
    private Integer garbageCollections;
    private Long gcTime;
    private List<ServiceHealth> services;
    private Map<String, Double> metrics;
    private List<Alert> alerts;
    private List<String> warnings;
    private String loadBalancerStatus;
    private Integer instanceCount;
    private String region;
    private String zone;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceHealth {
        private String serviceName;
        private String status;
        private Double responseTime;
        private LocalDateTime lastChecked;
        private String endpoint;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Alert {
        private String type;
        private String message;
        private String severity;
        private LocalDateTime timestamp;
        private String threshold;
        private String currentValue;
    }
}

 */