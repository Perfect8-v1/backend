package com.perfect8.admin.dto;

public class DashboardSummaryResponse {
    private String summary;

    public DashboardSummaryResponse() {}
    public DashboardSummaryResponse(String summary) { this.summary = summary; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
}
