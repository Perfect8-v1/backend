package com.perfect8.admin.dto;

public class SystemPerformanceResponse {
    private String systemPerformance;

    public SystemPerformanceResponse() {}
    public SystemPerformanceResponse(String systemPerformance) { this.systemPerformance = systemPerformance; }

    public String getSystemPerformance() { return systemPerformance; }
    public void setSystemPerformance(String systemPerformance) { this.systemPerformance = systemPerformance; }
}
