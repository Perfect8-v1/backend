package com.perfect8.shop.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class StockAdjustmentDTO {

    @NotNull(message = "Quantity change is required")
    @Min(value = -999999, message = "Quantity change too negative")
    @Max(value = 999999, message = "Quantity change too large")
    private Integer quantityChange;

    @NotBlank(message = "Reason is required")
    @Size(min = 5, max = 500, message = "Reason must be between 5 and 500 characters")
    private String reason;

    @Pattern(regexp = "DAMAGE|THEFT|EXPIRED|FOUND|CORRECTION|RETURN|SUPPLIER_ADJUSTMENT|CYCLE_COUNT|OTHER",
            message = "Invalid adjustment type")
    private String adjustmentType;

    @Size(max = 100, message = "Reference ID too long")
    private String referenceId; // Order ID, Return ID, etc.

    @Size(max = 100, message = "Batch number too long")
    private String batchNumber;

    @DecimalMin(value = "0", message = "Cost per unit cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Cost per unit must be a valid monetary value")
    private BigDecimal costPerUnit;

    @Size(max = 1000, message = "Notes too long")
    private String notes;

    // For damaged/expired items
    @Size(max = 200, message = "Disposition too long")
    private String disposition; // DISCARD, RETURN_TO_VENDOR, REPAIR, DONATE

    @Size(max = 500, message = "Damage description too long")
    private String damageDescription;

    // For supplier adjustments
    @Size(max = 100, message = "Supplier name too long")
    private String supplierName;

    @Size(max = 100, message = "Purchase order number too long")
    private String purchaseOrderNumber;

    // For cycle count adjustments
    @Size(max = 100, message = "Count performed by field too long")
    private String countPerformedBy;

    private Integer expectedQuantity;
    private Integer actualQuantity;

    // Approval workflow
    private Boolean requiresApproval = false;

    @Size(max = 100, message = "Approved by field too long")
    private String approvedBy;

    private Boolean approved;

    @Size(max = 500, message = "Approval notes too long")
    private String approvalNotes;

    // Additional tracking
    @Size(max = 100, message = "Location too long")
    private String location; // Warehouse location, shelf, etc.

    private Boolean urgent = false;

    // Default constructor
    public StockAdjustmentDTO() {}

    // Constructor with required fields
    public StockAdjustmentDTO(Integer quantityChange, String reason, String adjustmentType) {
        this.quantityChange = quantityChange;
        this.reason = reason;
        this.adjustmentType = adjustmentType;
    }

    // Getters and Setters
    public Integer getQuantityChange() {
        return quantityChange;
    }

    public void setQuantityChange(Integer quantityChange) {
        this.quantityChange = quantityChange;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getAdjustmentType() {
        return adjustmentType;
    }

    public void setAdjustmentType(String adjustmentType) {
        this.adjustmentType = adjustmentType;
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

    public String getDisposition() {
        return disposition;
    }

    public void setDisposition(String disposition) {
        this.disposition = disposition;
    }

    public String getDamageDescription() {
        return damageDescription;
    }

    public void setDamageDescription(String damageDescription) {
        this.damageDescription = damageDescription;
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

    public String getCountPerformedBy() {
        return countPerformedBy;
    }

    public void setCountPerformedBy(String countPerformedBy) {
        this.countPerformedBy = countPerformedBy;
    }

    public Integer getExpectedQuantity() {
        return expectedQuantity;
    }

    public void setExpectedQuantity(Integer expectedQuantity) {
        this.expectedQuantity = expectedQuantity;
    }

    public Integer getActualQuantity() {
        return actualQuantity;
    }

    public void setActualQuantity(Integer actualQuantity) {
        this.actualQuantity = actualQuantity;
    }

    public Boolean getRequiresApproval() {
        return requiresApproval;
    }

    public void setRequiresApproval(Boolean requiresApproval) {
        this.requiresApproval = requiresApproval;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Boolean getUrgent() {
        return urgent;
    }

    public void setUrgent(Boolean urgent) {
        this.urgent = urgent;
    }

    // Utility methods
    public boolean isIncrease() {
        return quantityChange != null && quantityChange > 0;
    }

    public boolean isDecrease() {
        return quantityChange != null && quantityChange < 0;
    }

    public boolean isDamageAdjustment() {
        return "DAMAGE".equals(adjustmentType);
    }

    public boolean isTheftAdjustment() {
        return "THEFT".equals(adjustmentType);
    }

    public boolean isCycleCountAdjustment() {
        return "CYCLE_COUNT".equals(adjustmentType);
    }

    public boolean isSupplierAdjustment() {
        return "SUPPLIER_ADJUSTMENT".equals(adjustmentType);
    }

    public boolean isReturnAdjustment() {
        return "RETURN".equals(adjustmentType);
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
        return Boolean.TRUE.equals(urgent);
    }

    public boolean hasReference() {
        return referenceId != null && !referenceId.trim().isEmpty();
    }

    public boolean hasBatch() {
        return batchNumber != null && !batchNumber.trim().isEmpty();
    }

    public boolean hasCostImpact() {
        return costPerUnit != null && costPerUnit.compareTo(BigDecimal.ZERO) > 0;
    }

    public BigDecimal getTotalCostImpact() {
        if (costPerUnit == null || quantityChange == null) {
            return BigDecimal.ZERO;
        }
        return costPerUnit.multiply(BigDecimal.valueOf(Math.abs(quantityChange)));
    }

    public String getAdjustmentTypeDisplayText() {
        switch (adjustmentType) {
            case "DAMAGE": return "Damaged Items";
            case "THEFT": return "Theft/Loss";
            case "EXPIRED": return "Expired Items";
            case "FOUND": return "Found Items";
            case "CORRECTION": return "Inventory Correction";
            case "RETURN": return "Customer Return";
            case "SUPPLIER_ADJUSTMENT": return "Supplier Adjustment";
            case "CYCLE_COUNT": return "Cycle Count";
            case "OTHER": return "Other";
            default: return adjustmentType;
        }
    }

    public String getDispositionDisplayText() {
        if (disposition == null) {
            return null;
        }
        switch (disposition) {
            case "DISCARD": return "Discard/Destroy";
            case "RETURN_TO_VENDOR": return "Return to Vendor";
            case "REPAIR": return "Repair";
            case "DONATE": return "Donate";
            default: return disposition;
        }
    }

    public String getQuantityChangeText() {
        if (quantityChange == null) {
            return "0";
        }
        return quantityChange > 0 ? "+" + quantityChange : quantityChange.toString();
    }

    public String getAdjustmentSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(getAdjustmentTypeDisplayText());
        summary.append(": ").append(getQuantityChangeText()).append(" units");

        if (hasReference()) {
            summary.append(" (Ref: ").append(referenceId).append(")");
        }

        return summary.toString();
    }

    // Validation method for cycle count
    @AssertTrue(message = "Expected and actual quantities are required for cycle count adjustments")
    public boolean isValidCycleCount() {
        if (!"CYCLE_COUNT".equals(adjustmentType)) {
            return true; // Not a cycle count, skip validation
        }
        return expectedQuantity != null && actualQuantity != null &&
                quantityChange != null &&
                quantityChange.equals(actualQuantity - expectedQuantity);
    }

    // Validation method for damage adjustments
    @AssertTrue(message = "Disposition is required for damage adjustments")
    public boolean isValidDamageAdjustment() {
        if (!"DAMAGE".equals(adjustmentType)) {
            return true; // Not a damage adjustment, skip validation
        }
        return disposition != null && !disposition.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "StockAdjustmentDTO{" +
                "quantityChange=" + quantityChange +
                ", reason='" + reason + '\'' +
                ", adjustmentType='" + adjustmentType + '\'' +
                ", referenceId='" + referenceId + '\'' +
                ", requiresApproval=" + requiresApproval +
                ", urgent=" + urgent +
                '}';
    }
}