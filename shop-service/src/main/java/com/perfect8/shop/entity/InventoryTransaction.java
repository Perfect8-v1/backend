package com.perfect8.shop.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_transactions")
public class InventoryTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "transaction_type", nullable = false, length = 20)
    private String transactionType; // STOCK_IN, STOCK_OUT, ADJUSTMENT, RESERVED, RELEASED, CYCLE_COUNT

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column(name = "quantity_before", nullable = false)
    private Integer quantityBefore;

    @Column(name = "quantity_after", nullable = false)
    private Integer quantityAfter;

    @Column(name = "quantity_change", nullable = false)
    private Integer quantityChange;

    @Column(length = 500)
    private String reason;

    @Column(name = "reference_id", length = 100)
    private String referenceId; // Order ID, Purchase Order ID, etc.

    @Column(name = "user_id", length = 100)
    private String userId; // Who performed the transaction

    @Column(name = "batch_number", length = 100)
    private String batchNumber;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "cost_per_unit", precision = 10, scale = 2)
    private java.math.BigDecimal costPerUnit;

    @Column(name = "total_cost", precision = 10, scale = 2)
    private java.math.BigDecimal totalCost;

    @Column(length = 1000)
    private String notes;

    // Default constructor
    public InventoryTransaction() {}

    // Constructor
    public InventoryTransaction(Product product, String transactionType,
                                Integer quantityBefore, Integer quantityAfter,
                                Integer quantityChange, String reason, String userId) {
        this.product = product;
        this.transactionType = transactionType;
        this.transactionDate = LocalDateTime.now();
        this.quantityBefore = quantityBefore;
        this.quantityAfter = quantityAfter;
        this.quantityChange = quantityChange;
        this.reason = reason;
        this.userId = userId;
    }

    @PrePersist
    protected void onCreate() {
        if (transactionDate == null) {
            transactionDate = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public Integer getQuantityBefore() {
        return quantityBefore;
    }

    public void setQuantityBefore(Integer quantityBefore) {
        this.quantityBefore = quantityBefore;
    }

    public Integer getQuantityAfter() {
        return quantityAfter;
    }

    public void setQuantityAfter(Integer quantityAfter) {
        this.quantityAfter = quantityAfter;
    }

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

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public java.math.BigDecimal getCostPerUnit() {
        return costPerUnit;
    }

    public void setCostPerUnit(java.math.BigDecimal costPerUnit) {
        this.costPerUnit = costPerUnit;
        if (costPerUnit != null && quantityChange != null) {
            this.totalCost = costPerUnit.multiply(java.math.BigDecimal.valueOf(Math.abs(quantityChange)));
        }
    }

    public java.math.BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(java.math.BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // Business methods
    public boolean isStockIncrease() {
        return quantityChange != null && quantityChange > 0;
    }

    public boolean isStockDecrease() {
        return quantityChange != null && quantityChange < 0;
    }

    public boolean isAdjustment() {
        return "ADJUSTMENT".equals(transactionType);
    }

    public boolean isReservation() {
        return "RESERVED".equals(transactionType);
    }

    public boolean isRelease() {
        return "RELEASED".equals(transactionType);
    }

    public String getTransactionDescription() {
        StringBuilder description = new StringBuilder();
        description.append(transactionType);

        if (quantityChange != null) {
            if (quantityChange > 0) {
                description.append(": +").append(quantityChange);
            } else {
                description.append(": ").append(quantityChange);
            }
        }

        if (reason != null && !reason.trim().isEmpty()) {
            description.append(" (").append(reason).append(")");
        }

        return description.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InventoryTransaction)) return false;
        InventoryTransaction that = (InventoryTransaction) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "InventoryTransaction{" +
                "id=" + id +
                ", transactionType='" + transactionType + '\'' +
                ", transactionDate=" + transactionDate +
                ", quantityBefore=" + quantityBefore +
                ", quantityAfter=" + quantityAfter +
                ", quantityChange=" + quantityChange +
                ", reason='" + reason + '\'' +
                '}';
    }
}