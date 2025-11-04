package com.perfect8.admin.dto;

public class InventoryReportResponse {
    private String inventoryReport;

    public InventoryReportResponse() {}
    public InventoryReportResponse(String inventoryReport) { this.inventoryReport = inventoryReport; }

    public String getInventoryReport() { return inventoryReport; }
    public void setInventoryReport(String inventoryReport) { this.inventoryReport = inventoryReport; }
}
