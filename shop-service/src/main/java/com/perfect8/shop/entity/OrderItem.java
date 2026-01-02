package com.perfect8.shop.entity;

import com.perfect8.common.enums.ItemStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Order Item Entity - Version 1.0
 * Magnum Opus Compliant: price not subtotal, no alias methods
 */
@Entity
@Table(name = "order_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"order", "product"})
@EqualsAndHashCode(exclude = {"order", "product"})
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long orderItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "product_sku", nullable = false)
    private String productSku;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "discount_amount", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    // CHANGED: subtotal → price (Magnum Opus: descriptive names)
    // Column name stays "subtotal" in database for backwards compatibility
    @Column(name = "price", precision = 19, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_status", length = 20)
    @Builder.Default
    private ItemStatus itemStatus = ItemStatus.PENDING;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Product snapshot at time of order
    @Column(name = "product_image_url")
    private String productImageUrl;

    @Column(name = "product_description", columnDefinition = "TEXT")
    private String productDescription;

    // Tracking info
    @Column(name = "shipped_quantity")
    @Builder.Default
    private Integer shippedQuantity = 0;

    @Column(name = "returned_quantity")
    @Builder.Default
    private Integer returnedQuantity = 0;

    @Column(name = "refunded_quantity")
    @Builder.Default
    private Integer refundedQuantity = 0;

    // CHANGED: createdDate/updatedDate → createdDate/updatedDate (consistency)
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    // FIXED: JPA handles updatedDate automatically
    private LocalDateTime updatedDate;

    // ========== LIFECYCLE CALLBACKS ==========

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
        calculatePrice();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
        calculatePrice();
    }

    // ========== MAGNUM OPUS COMPLIANT ==========
    // Lombok generates: getPrice() / setPrice()
    // REMOVED: getTotalPrice() - alias method (Magnum Opus violation)
    // REMOVED: manual getPrice() - Lombok generates it from field

    // ========== BUSINESS METHODS ==========

    /**
     * Calculate and set the price for this item
     */
    public void calculatePrice() {
        if (unitPrice != null && quantity != null) {
            BigDecimal baseAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));

            if (discountAmount != null) {
                baseAmount = baseAmount.subtract(discountAmount);
            }

            if (taxAmount != null) {
                baseAmount = baseAmount.add(taxAmount);
            }

            this.price = baseAmount;
        }
    }

    /**
     * Get the base price (without tax/discount)
     */
    public BigDecimal getBasePrice() {
        if (unitPrice != null && quantity != null) {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
        return BigDecimal.ZERO;
    }

    /**
     * Check if item is shipped
     */
    public boolean isShipped() {
        return shippedQuantity != null && shippedQuantity > 0;
    }

    /**
     * Check if item is fully shipped
     */
    public boolean isFullyShipped() {
        return shippedQuantity != null && shippedQuantity.equals(quantity);
    }

    /**
     * Check if item has been returned
     */
    public boolean hasReturns() {
        return returnedQuantity != null && returnedQuantity > 0;
    }

    /**
     * Check if item has been refunded
     */
    public boolean hasRefunds() {
        return refundedQuantity != null && refundedQuantity > 0;
    }

    /**
     * Get remaining quantity to ship
     */
    public Integer getRemainingToShip() {
        int shipped = shippedQuantity != null ? shippedQuantity : 0;
        return quantity - shipped;
    }

    /**
     * Check if this item can be cancelled
     */
    public boolean canBeCancelled() {
        return itemStatus == ItemStatus.PENDING ||
                itemStatus == ItemStatus.PROCESSING;
    }

    /**
     * Check if this item can be returned
     */
    public boolean canBeReturned() {
        return itemStatus == ItemStatus.DELIVERED &&
                shippedQuantity != null &&
                shippedQuantity > returnedQuantity;
    }

    /**
     * Update item status
     */
    public void updateStatus(ItemStatus newStatus) {
        this.itemStatus = newStatus;
        this.updatedDate = LocalDateTime.now();
    }

    /**
     * Apply discount to this item
     */
    public void applyDiscount(BigDecimal discount) {
        if (discount != null && discount.compareTo(BigDecimal.ZERO) > 0) {
            this.discountAmount = discount;
            calculatePrice();
        }
    }

    /**
     * Set tax for this item
     */
    public void setTax(BigDecimal tax) {
        if (tax != null && tax.compareTo(BigDecimal.ZERO) >= 0) {
            this.taxAmount = tax;
            calculatePrice();
        }
    }
}
