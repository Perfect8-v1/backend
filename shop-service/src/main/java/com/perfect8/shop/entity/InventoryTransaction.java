package com.perfect8.shop.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long inventoryTransactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "transaction_type", nullable = false, length = 20)
    private String transactionType; // STOCK_IN, STOCK_OUT, ADJUSTMENT, RESERVED, RELEASED, CYCLE_COUNT

    @Column(name = "transaction_date", nullable = false)
    @Builder.Default
    private LocalDateTime transactionDate = LocalDateTime.now();

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
    private BigDecimal costPerUnit;

    @Column(name = "total_cost", precision = 10, scale = 2)
    private BigDecimal totalCost;

    @Column(length = 1000)
    private String notes;

    @PrePersist
    protected void onCreate() {
        if (transactionDate == null) {
            transactionDate = LocalDateTime.now();
        }

        // Calculate total cost if cost per unit and quantity change are present
        if (costPerUnit != null && quantityChange != null) {
            totalCost = costPerUnit.multiply(BigDecimal.valueOf(Math.abs(quantityChange)));
        }
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

    // Helper method to set cost and auto-calculate total
    public void setCostPerUnitWithTotal(BigDecimal costPerUnit) {
        this.costPerUnit = costPerUnit;
        if (costPerUnit != null && quantityChange != null) {
            this.totalCost = costPerUnit.multiply(BigDecimal.valueOf(Math.abs(quantityChange)));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InventoryTransaction)) return false;
        InventoryTransaction that = (InventoryTransaction) o;
        return inventoryTransactionId != null && inventoryTransactionId.equals(that.inventoryTransactionId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "InventoryTransaction{" +
                "inventoryTransactionId=" + inventoryTransactionId +
                ", transactionType='" + transactionType + '\'' +
                ", transactionDate=" + transactionDate +
                ", quantityBefore=" + quantityBefore +
                ", quantityAfter=" + quantityAfter +
                ", quantityChange=" + quantityChange +
                ", reason='" + reason + '\'' +
                '}';
    }
}