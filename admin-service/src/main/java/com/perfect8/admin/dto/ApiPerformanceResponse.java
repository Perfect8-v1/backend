package com.perfect8.admin.dto;

public class ApiPerformanceResponse {
    private String apiPerformance;

    public ApiPerformanceResponse() {}
    public ApiPerformanceResponse(String apiPerformance) { this.apiPerformance = apiPerformance; }

    public String getApiPerformance() { return apiPerformance; }
    public void setApiPerformance(String apiPerformance) { this.apiPerformance = apiPerformance; }
}
