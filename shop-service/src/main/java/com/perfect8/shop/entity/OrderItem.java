package com.perfect8.shop.entity;

import com.perfect8.common.enums.ItemStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Order Item Entity
 * Represents individual items within an order
 * Version 1.0 - Core functionality
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

    @Column(name = "subtotal", precision = 19, scale = 2)
    private BigDecimal subtotal;

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

    // Timestamps
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    // ================ HELPER METHODS ================

    /**
     * Calculate and set the subtotal for this item
     */
    @PrePersist
    @PreUpdate
    public void calculateSubtotal() {
        if (unitPrice != null && quantity != null) {
            BigDecimal baseAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));

            if (discountAmount != null) {
                baseAmount = baseAmount.subtract(discountAmount);
            }

            if (taxAmount != null) {
                baseAmount = baseAmount.add(taxAmount);
            }

            this.subtotal = baseAmount;
        }
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Get the price for this order item (subtotal)
     * This is the method shop-service expects
     *
     * @return the subtotal price for this item
     */
    public BigDecimal getPrice() {
        if (subtotal == null) {
            calculateSubtotal();
        }
        return subtotal != null ? subtotal : BigDecimal.ZERO;
    }

    /**
     * Get the total price (alias for getPrice)
     */
    public BigDecimal getTotalPrice() {
        return getPrice();
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
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Apply discount to this item
     */
    public void applyDiscount(BigDecimal discount) {
        if (discount != null && discount.compareTo(BigDecimal.ZERO) > 0) {
            this.discountAmount = discount;
            calculateSubtotal();
        }
    }

    /**
     * Set tax for this item
     */
    public void setTax(BigDecimal tax) {
        if (tax != null && tax.compareTo(BigDecimal.ZERO) >= 0) {
            this.taxAmount = tax;
            calculateSubtotal();
        }
    }
}