package com.perfect8.admin.dto;

public class TrendAnalysisResponse {
    private String trendAnalysis;

    public TrendAnalysisResponse() {}
    public TrendAnalysisResponse(String trendAnalysis) { this.trendAnalysis = trendAnalysis; }

    public String getTrendAnalysis() { return trendAnalysis; }
    public void setTrendAnalysis(String trendAnalysis) { this.trendAnalysis = trendAnalysis; }
}
