package com.perfect8.admin.dto;

public class GeographicAnalyticsResponse {
    private String geographicAnalytics;

    public GeographicAnalyticsResponse() {}
    public GeographicAnalyticsResponse(String geographicAnalytics) { this.geographicAnalytics = geographicAnalytics; }

    public String getGeographicAnalytics() { return geographicAnalytics; }
    public void setGeographicAnalytics(String geographicAnalytics) { this.geographicAnalytics = geographicAnalytics; }
}
