/*
package com.perfect8.shop.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

public class InventoryMetricsResponse {
    private Integer totalProducts;
    private Integer activeProducts;
    private Integer lowStockProducts;
    private Integer outOfStockProducts;
    private BigDecimal totalInventoryValue;
    private List<LowStockProduct> lowStockItems = new ArrayList<>();
    private List<CategoryInventory> inventoryByCategory = new ArrayList<>();
    private List<InventoryMovement> recentMovements = new ArrayList<>();

    public InventoryMetricsResponse() {}

    // Getters and setters
    public Integer getTotalProducts() { return totalProducts; }
    public void setTotalProducts(Integer totalProducts) { this.totalProducts = totalProducts; }

    public Integer getActiveProducts() { return activeProducts; }
    public void setActiveProducts(Integer activeProducts) { this.activeProducts = activeProducts; }

    public Integer getLowStockProducts() { return lowStockProducts; }
    public void setLowStockProducts(Integer lowStockProducts) { this.lowStockProducts = lowStockProducts; }

    public Integer getOutOfStockProducts() { return outOfStockProducts; }
    public void setOutOfStockProducts(Integer outOfStockProducts) { this.outOfStockProducts = outOfStockProducts; }

    public BigDecimal getTotalInventoryValue() { return totalInventoryValue; }
    public void setTotalInventoryValue(BigDecimal totalInventoryValue) { this.totalInventoryValue = totalInventoryValue; }

    public List<LowStockProduct> getLowStockItems() { return lowStockItems; }
    public void setLowStockItems(List<LowStockProduct> lowStockItems) { this.lowStockItems = lowStockItems; }

    public List<CategoryInventory> getInventoryByCategory() { return inventoryByCategory; }
    public void setInventoryByCategory(List<CategoryInventory> inventoryByCategory) { this.inventoryByCategory = inventoryByCategory; }

    public List<InventoryMovement> getRecentMovements() { return recentMovements; }
    public void setRecentMovements(List<InventoryMovement> recentMovements) { this.recentMovements = recentMovements; }

    public static class LowStockProduct {
        private Long productId;
        private String productName;
        private String sku;
        private Integer currentStock;
        private Integer threshold;
        private String status;

        public LowStockProduct() {}

        // Getters and setters
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }

        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }

        public Integer getCurrentStock() { return currentStock; }
        public void setCurrentStock(Integer currentStock) { this.currentStock = currentStock; }

        public Integer getThreshold() { return threshold; }
        public void setThreshold(Integer threshold) { this.threshold = threshold; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class CategoryInventory {
        private String categoryName;
        private Integer productCount;
        private Integer totalStock;
        private BigDecimal totalValue;

        public CategoryInventory() {}

        // Getters and setters
        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

        public Integer getProductCount() { return productCount; }
        public void setProductCount(Integer productCount) { this.productCount = productCount; }

        public Integer getTotalStock() { return totalStock; }
        public void setTotalStock(Integer totalStock) { this.totalStock = totalStock; }

        public BigDecimal getTotalValue() { return totalValue; }
        public void setTotalValue(BigDecimal totalValue) { this.totalValue = totalValue; }
    }

    public static class InventoryMovement {
        private String productName;
        private String movementType;
        private Integer quantity;
        private String reason;
        private LocalDateTime timestamp;

        public InventoryMovement() {}

        // Getters and setters
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }

        public String getMovementType() { return movementType; }
        public void setMovementType(String movementType) { this.movementType = movementType; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
}

 */