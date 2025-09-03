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
public class DatabasePerformanceResponse {

    private Long totalConnections;
    private Long activeConnections;
    private Long idleConnections;
    private Long maxConnections;

    private Double connectionPoolUsagePercent;
    private Long avgConnectionTime;
    private Long maxConnectionTime;

    private Long totalQueries;
    private Long slowQueries;
    private Double avgQueryTime;
    private Long maxQueryTime;

    private Long totalTransactions;
    private Long commitedTransactions;
    private Long rolledBackTransactions;
    private Double transactionSuccessRate;

    private Long cacheHits;
    private Long cacheMisses;
    private Double cacheHitRatio;
    private Long cacheSize;

    private Long diskReads;
    private Long diskWrites;
    private Long diskSpace;
    private Long diskSpaceUsed;
    private Double diskUsagePercent;

    private Long memoryUsage;
    private Long memoryTotal;
    private Double memoryUsagePercent;

    private Double cpuUsage;
    private Integer activeProcesses;
    private Integer totalProcesses;

    private LocalDateTime timestamp;
    private String databaseVersion;
    private String status;

    private List<SlowQueryInfo> slowQueryDetails;
    private List<ConnectionInfo> connectionDetails;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SlowQueryInfo {
        private String query;
        private Long executionTime;
        private LocalDateTime timestamp;
        private String database;
        private String user;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConnectionInfo {
        private String connectionId;
        private String user;
        private String host;
        private String database;
        private String command;
        private Long time;
        private String state;
    }
}

 */