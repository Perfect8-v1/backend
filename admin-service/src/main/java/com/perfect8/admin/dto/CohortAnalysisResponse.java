package com.perfect8.admin.dto;

public class CohortAnalysisResponse {
    private String cohortAnalysis;

    public CohortAnalysisResponse() {}
    public CohortAnalysisResponse(String cohortAnalysis) { this.cohortAnalysis = cohortAnalysis; }

    public String getCohortAnalysis() { return cohortAnalysis; }
    public void setCohortAnalysis(String cohortAnalysis) { this.cohortAnalysis = cohortAnalysis; }
}
