package com.perfect8.admin.dto;

public class SalesMetricsResponse {
    private String metrics;

    public SalesMetricsResponse() {}
    public SalesMetricsResponse(String metrics) { this.metrics = metrics; }

    public String getMetrics() { return metrics; }
    public void setMetrics(String metrics) { this.metrics = metrics; }
}
