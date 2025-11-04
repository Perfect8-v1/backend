package com.perfect8.admin.dto;

public class ExecutiveSummaryResponse {
    private String executiveSummary;

    public ExecutiveSummaryResponse() {}
    public ExecutiveSummaryResponse(String executiveSummary) { this.executiveSummary = executiveSummary; }

    public String getExecutiveSummary() { return executiveSummary; }
    public void setExecutiveSummary(String executiveSummary) { this.executiveSummary = executiveSummary; }
}
