package com.perfect8.shop.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class InventoryUpdateDTO {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "New quantity is required")
    @Min(value = 0, message = "New quantity cannot be negative")
    @Max(value = 999999, message = "New quantity too large")
    private Integer newQuantity;

    @NotBlank(message = "Reason is required")
    @Size(min = 5, max = 500, message = "Reason must be between 5 and 500 characters")
    private String reason;

    @Pattern(regexp = "RESTOCK|ADJUSTMENT|DAMAGE|THEFT|EXPIRED|FOUND|CORRECTION|RETURN|CYCLE_COUNT|OTHER",
            message = "Invalid update type")
    private String updateType;

    @Size(max = 100, message = "Reference ID too long")
    private String referenceId; // Order ID, Purchase Order ID, etc.

    @Size(max = 100, message = "Batch number too long")
    private String batchNumber;

    @DecimalMin(value = "0", message = "Cost per unit cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Cost per unit must be a valid monetary value")
    private BigDecimal costPerUnit;

    @Size(max = 1000, message = "Notes too long")
    private String notes;

    // Product information (for validation/display)
    @Size(max = 100, message = "Product SKU too long")
    private String productSku;

    @Size(max = 255, message = "Product name too long")
    private String productName;

    // Location information
    @Size(max = 100, message = "Warehouse location too long")
    private String warehouseLocation;

    @Size(max = 100, message = "Shelf location too long")
    private String shelfLocation;

    // Supplier information
    @Size(max = 100, message = "Supplier name too long")
    private String supplierName;

    @Size(max = 100, message = "Purchase order number too long")
    private String purchaseOrderNumber;

    // Quality control
    @Pattern(regexp = "PENDING|PASSED|FAILED|NOT_REQUIRED", message = "Invalid quality check status")
    private String qualityCheckStatus = "NOT_REQUIRED";

    @Size(max = 500, message = "Quality notes too long")
    private String qualityNotes;

    // Expiry tracking
    private java.time.LocalDate expiryDate;

    private Boolean perishable = false;

    // Approval workflow
    private Boolean requiresApproval = false;

    @Size(max = 100, message = "Requested by field too long")
    private String requestedBy;

    @Size(max = 100, message = "Approved by field too long")
    private String approvedBy;

    private Boolean approved;

    @Size(max = 500, message = "Approval notes too long")
    private String approvalNotes;

    // Priority and urgency
    @Min(value = 1, message = "Priority must be between 1 and 5")
    @Max(value = 5, message = "Priority must be between 1 and 5")
    private Integer priority = 3; // 1 = Critical, 5 = Low

    private Boolean urgent = false;

    // Reorder information
    @Min(value = 0, message = "Reorder point cannot be negative")
    private Integer reorderPoint;

    @Min(value = 1, message = "Reorder quantity must be at least 1")
    private Integer reorderQuantity;

    // Default constructor
    public InventoryUpdateDTO() {}

    // Constructor with required fields
    public InventoryUpdateDTO(Long productId, Integer newQuantity, String reason) {
        this.productId = productId;
        this.newQuantity = newQuantity;
        this.reason = reason;
    }

    // Getters and Setters
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getNewQuantity() {
        return newQuantity;
    }

    public void setNewQuantity(Integer newQuantity) {
        this.newQuantity = newQuantity;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getUpdateType() {
        return updateType;
    }

    public void setUpdateType(String updateType) {
        this.updateType = updateType;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public BigDecimal getCostPerUnit() {
        return costPerUnit;
    }

    public void setCostPerUnit(BigDecimal costPerUnit) {
        this.costPerUnit = costPerUnit;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getProductSku() {
        return productSku;
    }

    public void setProductSku(String productSku) {
        this.productSku = productSku;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getWarehouseLocation() {
        return warehouseLocation;
    }

    public void setWarehouseLocation(String warehouseLocation) {
        this.warehouseLocation = warehouseLocation;
    }

    public String getShelfLocation() {
        return shelfLocation;
    }

    public void setShelfLocation(String shelfLocation) {
        this.shelfLocation = shelfLocation;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getPurchaseOrderNumber() {
        return purchaseOrderNumber;
    }

    public void setPurchaseOrderNumber(String purchaseOrderNumber) {
        this.purchaseOrderNumber = purchaseOrderNumber;
    }

    public String getQualityCheckStatus() {
        return qualityCheckStatus;
    }

    public void setQualityCheckStatus(String qualityCheckStatus) {
        this.qualityCheckStatus = qualityCheckStatus;
    }

    public String getQualityNotes() {
        return qualityNotes;
    }

    public void setQualityNotes(String qualityNotes) {
        this.qualityNotes = qualityNotes;
    }

    public java.time.LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(java.time.LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Boolean getPerishable() {
        return perishable;
    }

    public void setPerishable(Boolean perishable) {
        this.perishable = perishable;
    }

    public Boolean getRequiresApproval() {
        return requiresApproval;
    }

    public void setRequiresApproval(Boolean requiresApproval) {
        this.requiresApproval = requiresApproval;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(String requestedBy) {
        this.requestedBy = requestedBy;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public String getApprovalNotes() {
        return approvalNotes;
    }

    public void setApprovalNotes(String approvalNotes) {
        this.approvalNotes = approvalNotes;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Boolean getUrgent() {
        return urgent;
    }

    public void setUrgent(Boolean urgent) {
        this.urgent = urgent;
    }

    public Integer getReorderPoint() {
        return reorderPoint;
    }

    public void setReorderPoint(Integer reorderPoint) {
        this.reorderPoint = reorderPoint;
    }

    public Integer getReorderQuantity() {
        return reorderQuantity;
    }

    public void setReorderQuantity(Integer reorderQuantity) {
        this.reorderQuantity = reorderQuantity;
    }

    // Utility methods
    public boolean isRestock() {
        return "RESTOCK".equals(updateType);
    }

    public boolean isAdjustment() {
        return "ADJUSTMENT".equals(updateType);
    }

    public boolean isDamageUpdate() {
        return "DAMAGE".equals(updateType);
    }

    public boolean isCycleCountUpdate() {
        return "CYCLE_COUNT".equals(updateType);
    }

    public boolean needsApproval() {
        return Boolean.TRUE.equals(requiresApproval);
    }

    public boolean isApproved() {
        return Boolean.TRUE.equals(approved);
    }

    public boolean isPendingApproval() {
        return needsApproval() && approved == null;
    }

    public boolean isRejected() {
        return needsApproval() && Boolean.FALSE.equals(approved);
    }

    public boolean isUrgent() {
        return Boolean.TRUE.equals(urgent) || (priority != null && priority <= 2);
    }

    public boolean isPerishable() {
        return Boolean.TRUE.equals(perishable);
    }

    public boolean hasExpiry() {
        return expiryDate != null;
    }

    public boolean isExpired() {
        return hasExpiry() && expiryDate.isBefore(java.time.LocalDate.now());
    }

    public boolean isNearExpiry(int daysThreshold) {
        return hasExpiry() && expiryDate.isBefore(java.time.LocalDate.now().plusDays(daysThreshold));
    }

    public boolean requiresQualityCheck() {
        return !"NOT_REQUIRED".equals(qualityCheckStatus);
    }

    public boolean passedQualityCheck() {
        return "PASSED".equals(qualityCheckStatus);
    }

    public boolean failedQualityCheck() {
        return "FAILED".equals(qualityCheckStatus);
    }

    public boolean hasCostImpact() {
        return costPerUnit != null && costPerUnit.compareTo(BigDecimal.ZERO) > 0;
    }

    public BigDecimal getTotalCostImpact() {
        if (costPerUnit == null || newQuantity == null) {
            return BigDecimal.ZERO;
        }
        return costPerUnit.multiply(BigDecimal.valueOf(newQuantity));
    }

    public String getUpdateTypeDisplayText() {
        switch (updateType) {
            case "RESTOCK": return "Restock";
            case "ADJUSTMENT": return "Adjustment";
            case "DAMAGE": return "Damage";
            case "THEFT": return "Theft/Loss";
            case "EXPIRED": return "Expired";
            case "FOUND": return "Found Items";
            case "CORRECTION": return "Correction";
            case "RETURN": return "Return";
            case "CYCLE_COUNT": return "Cycle Count";
            case "OTHER": return "Other";
            default: return updateType;
        }
    }

    public String getPriorityText() {
        if (priority == null) {
            return "Normal";
        }
        switch (priority) {
            case 1: return "Critical";
            case 2: return "High";
            case 3: return "Normal";
            case 4: return "Low";
            case 5: return "Very Low";
            default: return "Normal";
        }
    }

    public String getQualityStatusDisplayText() {
        switch (qualityCheckStatus) {
            case "PENDING": return "Quality Check Pending";
            case "PASSED": return "Quality Check Passed";
            case "FAILED": return "Quality Check Failed";
            case "NOT_REQUIRED": return "No Quality Check Required";
            default: return qualityCheckStatus;
        }
    }

    public String getFullLocation() {
        StringBuilder location = new StringBuilder();
        if (warehouseLocation != null && !warehouseLocation.trim().isEmpty()) {
            location.append(warehouseLocation);
        }
        if (shelfLocation != null && !shelfLocation.trim().isEmpty()) {
            if (location.length() > 0) location.append(" - ");
            location.append(shelfLocation);
        }
        return location.toString();
    }

    public String getProductDisplay() {
        if (productName != null && !productName.trim().isEmpty()) {
            return productSku != null ? productName + " (" + productSku + ")" : productName;
        }
        return productSku != null ? productSku : "Product ID: " + productId;
    }

    @Override
    public String toString() {
        return "InventoryUpdateDTO{" +
                "productId=" + productId +
                ", newQuantity=" + newQuantity +
                ", reason='" + reason + '\'' +
                ", updateType='" + updateType + '\'' +
                ", requiresApproval=" + requiresApproval +
                ", urgent=" + urgent +
                '}';
    }
}