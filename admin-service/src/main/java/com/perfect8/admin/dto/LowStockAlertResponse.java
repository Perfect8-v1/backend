package com.perfect8.admin.dto;

public class LowStockAlertResponse {
    private String lowStockAlert;

    public LowStockAlertResponse() {}
    public LowStockAlertResponse(String lowStockAlert) { this.lowStockAlert = lowStockAlert; }

    public String getLowStockAlert() { return lowStockAlert; }
    public void setLowStockAlert(String lowStockAlert) { this.lowStockAlert = lowStockAlert; }
}
