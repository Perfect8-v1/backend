package com.perfect8.shop.dto;

import java.time.LocalDateTime;

public class LowStockAlertResponse {
    private Long productId;
    private String productName;
    private String sku;
    private String category;
    private Integer currentStock;
    private Integer lowStockThreshold;
    private Integer reorderPoint;
    private String alertLevel; // LOW, CRITICAL, OUT_OF_STOCK
    private LocalDateTime lastSold;
    private Integer averageDailySales;
    private Integer daysUntilStockOut;

    public LowStockAlertResponse() {}

    // Getters and setters
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getCurrentStock() { return currentStock; }
    public void setCurrentStock(Integer currentStock) { this.currentStock = currentStock; }

    public Integer getLowStockThreshold() { return lowStockThreshold; }
    public void setLowStockThreshold(Integer lowStockThreshold) { this.lowStockThreshold = lowStockThreshold; }

    public Integer getReorderPoint() { return reorderPoint; }
    public void setReorderPoint(Integer reorderPoint) { this.reorderPoint = reorderPoint; }

    public String getAlertLevel() { return alertLevel; }
    public void setAlertLevel(String alertLevel) { this.alertLevel = alertLevel; }

    public LocalDateTime getLastSold() { return lastSold; }
    public void setLastSold(LocalDateTime lastSold) { this.lastSold = lastSold; }

    public Integer getAverageDailySales() { return averageDailySales; }
    public void setAverageDailySales(Integer averageDailySales) { this.averageDailySales = averageDailySales; }

    public Integer getDaysUntilStockOut() { return daysUntilStockOut; }
    public void setDaysUntilStockOut(Integer daysUntilStockOut) { this.daysUntilStockOut = daysUntilStockOut; }
}