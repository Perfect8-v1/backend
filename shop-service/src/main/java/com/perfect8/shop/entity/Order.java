package com.perfect8.shop.entity;

import com.perfect8.common.enums.OrderStatus;  // Från common!
import com.perfect8.shop.util.OrderStatusHelper;  // Lokal helper
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Order entity för shop-service
 * Använder OrderStatus från common-modulen
 * Version 1.0
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "order_number", unique = true, nullable = false, length = 50)
    private String orderNumber;

    /**
     * Order status från common-modulen
     * Sparas som STRING i databasen
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 20)
    @Builder.Default
    private OrderStatus orderStatus = OrderStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "order_date", nullable = false)
    @Builder.Default
    private LocalDateTime orderDate = LocalDateTime.now();

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "subtotal_amount", precision = 10, scale = 2)
    private BigDecimal subtotalAmount;

    @Column(name = "tax_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "shipping_cost", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal shippingCost = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "payment_status", length = 20)
    private String paymentStatus;

    @Column(name = "shipping_method", length = 50)
    private String shippingMethod;

    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "shipped_date")
    private LocalDateTime shippedDate;

    @Column(name = "delivered_date")
    private LocalDateTime deliveredDate;

    // Relations

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipping_address_id")
    private Address shippingAddress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billing_address_id")
    private Address billingAddress;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Payment payment;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Shipment shipment;

    // Lifecycle callbacks

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        orderDate = LocalDateTime.now();
        if (orderStatus == null) {
            orderStatus = OrderStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Business methods - använder OrderStatusHelper

    /**
     * Update order status with validation
     */
    public void updateStatus(OrderStatus newStatus) {
        // Använd helper för validering
        OrderStatusHelper.validateTransition(this.orderStatus, newStatus);

        this.orderStatus = newStatus;

        // Update related dates
        if (newStatus == OrderStatus.SHIPPED) {
            this.shippedDate = LocalDateTime.now();
        } else if (newStatus == OrderStatus.DELIVERED) {
            this.deliveredDate = LocalDateTime.now();
        }
    }

    /**
     * Check if order can be cancelled
     */
    public boolean canBeCancelled() {
        return OrderStatusHelper.canBeCancelled(this.orderStatus);
    }

    /**
     * Check if order can be returned
     */
    public boolean canBeReturned() {
        return OrderStatusHelper.canBeReturned(this.orderStatus);
    }

    /**
     * Check if order is in final state
     */
    public boolean isFinalState() {
        return this.orderStatus.isFinalState();
    }

    /**
     * Check if order requires payment
     */
    public boolean requiresPayment() {
        return OrderStatusHelper.requiresPayment(this.orderStatus);
    }

    /**
     * Get order progress percentage
     */
    public int getProgressPercentage() {
        return OrderStatusHelper.getProgressPercentage(this.orderStatus);
    }

    /**
     * Add order item
     */
    public void addOrderItem(OrderItem item) {
        orderItems.add(item);
        item.setOrder(this);
        recalculateTotals();
    }

    /**
     * Remove order item
     */
    public void removeOrderItem(OrderItem item) {
        orderItems.remove(item);
        item.setOrder(null);
        recalculateTotals();
    }

    /**
     * Recalculate order totals
     */
    private void recalculateTotals() {
        if (orderItems != null && !orderItems.isEmpty()) {
            this.subtotalAmount = orderItems.stream()
                    .map(OrderItem::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            this.totalAmount = subtotalAmount
                    .add(taxAmount)
                    .add(shippingCost)
                    .subtract(discountAmount);
        }
    }

    /**
     * Cancel order
     */
    public void cancel() {
        if (!canBeCancelled()) {
            throw new IllegalStateException("Order cannot be cancelled in status: " + orderStatus);
        }
        updateStatus(OrderStatus.CANCELLED);
    }
}