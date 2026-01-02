package com.perfect8.shop.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

public class InventoryReportResponse {
    private String reportPeriod;
    private LocalDateTime generatedDate;
    private Integer totalProducts;
    private Integer lowStockProducts;
    private Integer outOfStockProducts;
    private BigDecimal totalInventoryValue;
    private List<InventoryItem> items = new ArrayList<>();
    private List<LowStockItem> lowStockItems = new ArrayList<>();
    private List<MovementSummary> movements = new ArrayList<>();

    public InventoryReportResponse() {}

    // Getters and setters
    public String getReportPeriod() { return reportPeriod; }
    public void setReportPeriod(String reportPeriod) { this.reportPeriod = reportPeriod; }

    public LocalDateTime getGeneratedDate() { return generatedDate; }
    public void setGeneratedDate(LocalDateTime generatedDate) { this.generatedDate = generatedDate; }

    public Integer getTotalProducts() { return totalProducts; }
    public void setTotalProducts(Integer totalProducts) { this.totalProducts = totalProducts; }

    public Integer getLowStockProducts() { return lowStockProducts; }
    public void setLowStockProducts(Integer lowStockProducts) { this.lowStockProducts = lowStockProducts; }

    public Integer getOutOfStockProducts() { return outOfStockProducts; }
    public void setOutOfStockProducts(Integer outOfStockProducts) { this.outOfStockProducts = outOfStockProducts; }

    public BigDecimal getTotalInventoryValue() { return totalInventoryValue; }
    public void setTotalInventoryValue(BigDecimal totalInventoryValue) { this.totalInventoryValue = totalInventoryValue; }

    public List<InventoryItem> getItems() { return items; }
    public void setItems(List<InventoryItem> items) { this.items = items; }

    public List<LowStockItem> getLowStockItems() { return lowStockItems; }
    public void setLowStockItems(List<LowStockItem> lowStockItems) { this.lowStockItems = lowStockItems; }

    public List<MovementSummary> getMovements() { return movements; }
    public void setMovements(List<MovementSummary> movements) { this.movements = movements; }

    public static class InventoryItem {
        private String productName;
        private String sku;
        private Integer currentStock;
        private BigDecimal unitPrice;
        private BigDecimal totalValue;
        private String status;

        public InventoryItem() {}

        // Getters and setters
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }

        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }

        public Integer getCurrentStock() { return currentStock; }
        public void setCurrentStock(Integer currentStock) { this.currentStock = currentStock; }

        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

        public BigDecimal getTotalValue() { return totalValue; }
        public void setTotalValue(BigDecimal totalValue) { this.totalValue = totalValue; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class LowStockItem {
        private String productName;
        private String sku;
        private Integer currentStock;
        private Integer threshold;
        private Integer recommended;

        public LowStockItem() {}

        // Getters and setters
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }

        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }

        public Integer getCurrentStock() { return currentStock; }
        public void setCurrentStock(Integer currentStock) { this.currentStock = currentStock; }

        public Integer getThreshold() { return threshold; }
        public void setThreshold(Integer threshold) { this.threshold = threshold; }

        public Integer getRecommended() { return recommended; }
        public void setRecommended(Integer recommended) { this.recommended = recommended; }
    }

    public static class MovementSummary {
        private String movementType;
        private Integer quantity;
        private String reason;
        private LocalDateTime movementDate;

        public MovementSummary() {}

        // Getters and setters
        public String getMovementType() { return movementType; }
        public void setMovementType(String movementType) { this.movementType = movementType; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }

        public LocalDateTime getMovementDate() { return movementDate; }
        public void setMovementDate(LocalDateTime movementDate) { this.movementDate = movementDate; }
    }
}