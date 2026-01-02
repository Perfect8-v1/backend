package com.perfect8.admin.dto;

public class CustomerReportResponse {
    private String customerReport;

    public CustomerReportResponse() {}
    public CustomerReportResponse(String customerReport) { this.customerReport = customerReport; }

    public String getCustomerReport() { return customerReport; }
    public void setCustomerReport(String customerReport) { this.customerReport = customerReport; }
}