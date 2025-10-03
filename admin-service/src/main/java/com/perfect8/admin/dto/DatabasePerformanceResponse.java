package com.perfect8.admin.dto;

public class DatabasePerformanceResponse {
    private String databasePerformance;

    public DatabasePerformanceResponse() {}
    public DatabasePerformanceResponse(String databasePerformance) { this.databasePerformance = databasePerformance; }

    public String getDatabasePerformance() { return databasePerformance; }
    public void setDatabasePerformance(String databasePerformance) { this.databasePerformance = databasePerformance; }
}
