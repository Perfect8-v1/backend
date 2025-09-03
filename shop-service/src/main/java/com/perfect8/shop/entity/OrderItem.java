package com.perfect8.shop.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing an individual item within an order.
 * Version 1.0 - Core functionality only
 */
@Entity
@Table(name = "order_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"order", "product"})
@ToString(exclude = {"order", "product"})
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

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    // Tax amount for this item
    @Column(name = "tax_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    // Version 2.0 - Commented out for future implementation
    // @Column(name = "discount_amount", precision = 10, scale = 2)
    // @Builder.Default
    // private BigDecimal discountAmount = BigDecimal.ZERO;

    // @Column(name = "discount_percentage", precision = 5, scale = 2)
    // private BigDecimal discountPercentage;

    // @Column(name = "coupon_code", length = 50)
    // private String couponCode;

    // Product snapshot at time of order (in case product changes)
    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Column(name = "product_sku", length = 100)
    private String productSku;

    @Column(name = "product_description", columnDefinition = "TEXT")
    private String productDescription;

    // Status for individual item (for partial fulfillment)
    @Enumerated(EnumType.STRING)
    @Column(name = "item_status", length = 50)
    @Builder.Default
    private ItemStatus itemStatus = ItemStatus.PENDING;

    // Tracking for partial shipments
    @Column(name = "shipped_quantity")
    @Builder.Default
    private Integer shippedQuantity = 0;

    @Column(name = "returned_quantity")
    @Builder.Default
    private Integer returnedQuantity = 0;

    // Notes specific to this item
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Item status enum for tracking individual item fulfillment
     */
    public enum ItemStatus {
        PENDING,
        PROCESSING,
        PARTIALLY_SHIPPED,
        SHIPPED,
        DELIVERED,
        CANCELLED,
        RETURNED,
        REFUNDED
    }

    /**
     * Calculate and set subtotal based on quantity and unit price
     */
    @PrePersist
    @PreUpdate
    public void calculateSubtotal() {
        if (quantity != null && unitPrice != null) {
            this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));

            // Add tax if applicable
            if (taxAmount != null) {
                this.subtotal = this.subtotal.add(taxAmount);
            }
        }

        // Ensure product snapshot data is captured
        if (product != null && productName == null) {
            this.productName = product.getName();
            this.productSku = product.getSku();
            this.productDescription = product.getDescription();
        }

        // Initialize shipped quantity if null
        if (shippedQuantity == null) {
            shippedQuantity = 0;
        }

        // Initialize returned quantity if null
        if (returnedQuantity == null) {
            returnedQuantity = 0;
        }
    }

    /**
     * Check if item is fully shipped
     */
    public boolean isFullyShipped() {
        return shippedQuantity != null && shippedQuantity.equals(quantity);
    }

    /**
     * Check if item has been partially shipped
     */
    public boolean isPartiallyShipped() {
        return shippedQuantity != null && shippedQuantity > 0 && shippedQuantity < quantity;
    }

    /**
     * Get remaining quantity to ship
     */
    public Integer getRemainingToShip() {
        if (quantity == null || shippedQuantity == null) {
            return quantity;
        }
        return quantity - shippedQuantity;
    }

    /**
     * Check if item can be cancelled
     */
    public boolean isCancellable() {
        return itemStatus == ItemStatus.PENDING || itemStatus == ItemStatus.PROCESSING;
    }

    /**
     * Check if item can be returned
     */
    public boolean isReturnable() {
        return itemStatus == ItemStatus.DELIVERED &&
                (returnedQuantity == null || returnedQuantity < quantity);
    }
}