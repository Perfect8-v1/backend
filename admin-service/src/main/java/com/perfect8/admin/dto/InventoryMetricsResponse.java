package com.perfect8.admin.dto;

public class InventoryMetricsResponse {
    private String inventoryMetrics;

    public InventoryMetricsResponse() {}
    public InventoryMetricsResponse(String inventoryMetrics) { this.inventoryMetrics = inventoryMetrics; }

    public String getInventoryMetrics() { return inventoryMetrics; }
    public void setInventoryMetrics(String inventoryMetrics) { this.inventoryMetrics = inventoryMetrics; }
}
