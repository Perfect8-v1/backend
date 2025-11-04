package com.perfect8.admin.dto;

public class SalesReportResponse {
    private String salesReport;

    public SalesReportResponse() {}
    public SalesReportResponse(String salesReport) { this.salesReport = salesReport; }

    public String getSalesReport() { return salesReport; }
    public void setSalesReport(String salesReport) { this.salesReport = salesReport; }
}
