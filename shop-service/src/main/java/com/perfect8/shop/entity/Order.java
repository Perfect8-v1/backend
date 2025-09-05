package com.perfect8.shop.entity;

import com.perfect8.shop.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a customer order.
 * Version 1.0 - Core functionality only
 */
@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_order_number", columnList = "order_number", unique = true),
        @Index(name = "idx_customer_id", columnList = "customer_id"),
        @Index(name = "idx_order_status", columnList = "order_status"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"customer", "orderItems", "payment", "shipment"})
@ToString(exclude = {"customer", "orderItems", "payment", "shipment"})
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 50)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    // Order items
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    // Payment information
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Payment payment;

    // Shipment information
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Shipment shipment;

    // Pricing
    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "tax_amount", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "shipping_amount", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal shippingAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    // Currency for international support
    @Column(name = "currency", nullable = false, length = 3)
    @Builder.Default
    private String currency = "USD";

    // Shipping address (snapshot at time of order)
    @Column(name = "shipping_first_name", nullable = false, length = 100)
    private String shippingFirstName;

    @Column(name = "shipping_last_name", nullable = false, length = 100)
    private String shippingLastName;

    @Column(name = "shipping_email", nullable = false, length = 255)
    private String shippingEmail;

    @Column(name = "shipping_phone", length = 20)
    private String shippingPhone;

    @Column(name = "shipping_address_line1", nullable = false, length = 255)
    private String shippingAddressLine1;

    @Column(name = "shipping_address_line2", length = 255)
    private String shippingAddressLine2;

    @Column(name = "shipping_city", nullable = false, length = 100)
    private String shippingCity;

    @Column(name = "shipping_state", nullable = false, length = 100)
    private String shippingState;

    @Column(name = "shipping_postal_code", nullable = false, length = 20)
    private String shippingPostalCode;

    @Column(name = "shipping_country", nullable = false, length = 2)
    @Builder.Default
    private String shippingCountry = "US";

    // Billing address (if different from shipping)
    @Column(name = "billing_same_as_shipping")
    @Builder.Default
    private Boolean billingSameAsShipping = true;

    @Column(name = "billing_first_name", length = 100)
    private String billingFirstName;

    @Column(name = "billing_last_name", length = 100)
    private String billingLastName;

    @Column(name = "billing_address_line1", length = 255)
    private String billingAddressLine1;

    @Column(name = "billing_address_line2", length = 255)
    private String billingAddressLine2;

    @Column(name = "billing_city", length = 100)
    private String billingCity;

    @Column(name = "billing_state", length = 100)
    private String billingState;

    @Column(name = "billing_postal_code", length = 20)
    private String billingPostalCode;

    @Column(name = "billing_country", length = 2)
    private String billingCountry;

    // Customer notes
    @Column(name = "customer_notes", columnDefinition = "TEXT")
    private String customerNotes;

    // Internal notes (not visible to customer)
    @Column(name = "internal_notes", columnDefinition = "TEXT")
    private String internalNotes;

    // Important dates
    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "returned_at")
    private LocalDateTime returnedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ========================================
    // Backward compatibility methods
    // ========================================

    /**
     * Backward compatibility getter for orderId
     * @return the order id
     */
    public Long getOrderId() {
        return this.id;
    }

    /**
     * Backward compatibility setter for orderId
     * @param orderId the order id to set
     */
    public void setOrderId(Long orderId) {
        this.id = orderId;
    }

    /**
     * Backward compatibility getter for orderStatus
     * @return the order status
     */
    public OrderStatus getOrderStatus() {
        return this.status;
    }

    /**
     * Backward compatibility setter for orderStatus
     * @param orderStatus the order status to set
     */
    public void setOrderStatus(OrderStatus orderStatus) {
        this.status = orderStatus;
    }

    /**
     * Get total amount (alias for totalAmount)
     * @return the total amount
     */
    public BigDecimal getTotal() {
        return this.totalAmount;
    }

    /**
     * Set total amount (alias for totalAmount)
     * @param total the total amount to set
     */
    public void setTotal(BigDecimal total) {
        this.totalAmount = total;
    }

    /**
     * Get order date (alias for createdAt)
     * @return the order creation date
     */
    public LocalDateTime getOrderDate() {
        return this.createdAt;
    }

    /**
     * Set order date (alias for createdAt)
     * @param orderDate the order date to set
     */
    public void setOrderDate(LocalDateTime orderDate) {
        this.createdAt = orderDate;
    }

    /**
     * Set order items (alias for orderItems)
     * @param items the order items to set
     */
    public void setItems(List<OrderItem> items) {
        this.orderItems = items;
    }

    // ========================================
    // Business logic methods
    // ========================================

    /**
     * Generate order number if not set
     */
    @PrePersist
    public void generateOrderNumber() {
        if (orderNumber == null) {
            // Format: ORD-YYYYMMDD-XXXXXX (where X is random alphanumeric)
            String timestamp = LocalDateTime.now().toString()
                    .replaceAll("[^0-9]", "").substring(0, 14);
            String random = String.valueOf(System.nanoTime()).substring(7, 13);
            this.orderNumber = String.format("ORD-%s-%s", timestamp, random);
        }

        // Copy billing address from shipping if same
        if (Boolean.TRUE.equals(billingSameAsShipping)) {
            this.billingFirstName = this.shippingFirstName;
            this.billingLastName = this.shippingLastName;
            this.billingAddressLine1 = this.shippingAddressLine1;
            this.billingAddressLine2 = this.shippingAddressLine2;
            this.billingCity = this.shippingCity;
            this.billingState = this.shippingState;
            this.billingPostalCode = this.shippingPostalCode;
            this.billingCountry = this.shippingCountry;
        }
    }

    /**
     * Calculate order totals
     */
    public void calculateTotals() {
        // Calculate subtotal from items
        this.subtotal = orderItems.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate total
        this.totalAmount = subtotal
                .add(taxAmount != null ? taxAmount : BigDecimal.ZERO)
                .add(shippingAmount != null ? shippingAmount : BigDecimal.ZERO);
    }

    /**
     * Add an order item
     */
    public void addOrderItem(OrderItem item) {
        if (orderItems == null) {
            orderItems = new ArrayList<>();
        }
        orderItems.add(item);
        item.setOrder(this);
        calculateTotals();
    }

    /**
     * Remove an order item
     */
    public void removeOrderItem(OrderItem item) {
        if (orderItems != null) {
            orderItems.remove(item);
            item.setOrder(null);
            calculateTotals();
        }
    }

    /**
     * Check if order can be cancelled
     * Delegates to OrderStatus enum method
     */
    public boolean isCancellable() {
        return status != null && status.canBeCancelled();
    }

    /**
     * Check if order can be returned
     * Delegates to OrderStatus enum method
     */
    public boolean isReturnable() {
        return status != null && status.canBeReturned();
    }

    /**
     * Update order status with timestamp
     */
    public void updateStatus(OrderStatus newStatus) {
        this.status = newStatus;
        LocalDateTime now = LocalDateTime.now();

        switch (newStatus) {
            case CONFIRMED:
                this.confirmedAt = now;
                break;
            case SHIPPED:
                this.shippedAt = now;
                break;
            case DELIVERED:
                this.deliveredAt = now;
                break;
            case CANCELLED:
                this.cancelledAt = now;
                break;
            case RETURNED:
                this.returnedAt = now;
                break;
            default:
                // Other statuses don't need specific timestamp tracking
                break;
        }
    }

    /**
     * Get customer full name
     */
    public String getCustomerFullName() {
        return String.format("%s %s", shippingFirstName, shippingLastName);
    }

    /**
     * Check if order has been paid/confirmed
     */
    public boolean isPaid() {
        return confirmedAt != null && (
                status == OrderStatus.CONFIRMED ||
                        status == OrderStatus.PROCESSING ||
                        status == OrderStatus.SHIPPED ||
                        status == OrderStatus.DELIVERED
        );
    }

    /**
     * Check if order is in final state
     * Delegates to OrderStatus enum method
     */
    public boolean isFinalState() {
        return status != null && status.isFinalState();
    }

    /**
     * Get formatted shipping address
     */
    public String getFormattedShippingAddress() {
        StringBuilder address = new StringBuilder();
        address.append(shippingAddressLine1);
        if (shippingAddressLine2 != null && !shippingAddressLine2.isEmpty()) {
            address.append("\n").append(shippingAddressLine2);
        }
        address.append("\n").append(shippingCity).append(", ")
                .append(shippingState).append(" ")
                .append(shippingPostalCode).append("\n")
                .append(shippingCountry);
        return address.toString();
    }

    /**
     * Get formatted billing address
     */
    public String getFormattedBillingAddress() {
        if (Boolean.TRUE.equals(billingSameAsShipping)) {
            return getFormattedShippingAddress();
        }

        StringBuilder address = new StringBuilder();
        address.append(billingAddressLine1);
        if (billingAddressLine2 != null && !billingAddressLine2.isEmpty()) {
            address.append("\n").append(billingAddressLine2);
        }
        address.append("\n").append(billingCity).append(", ")
                .append(billingState).append(" ")
                .append(billingPostalCode).append("\n")
                .append(billingCountry);
        return address.toString();
    }
}