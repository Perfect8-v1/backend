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
 * Order entity representing a customer order.
 * Version 1.0 - Core functionality with descriptive field names
 */
@Entity
@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"orderItems", "payment", "shipment", "customer"})
@ToString(exclude = {"orderItems", "payment", "shipment", "customer"})
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "order_number", unique = true, nullable = false)
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    private OrderStatus orderStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Payment payment;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Shipment shipment;

    // Financial fields with descriptive names
    @Column(name = "subtotal", precision = 10, scale = 2, nullable = false)
    private BigDecimal subtotal;

    @Column(name = "tax_amount", precision = 10, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "shipping_amount", precision = 10, scale = 2)
    private BigDecimal shippingAmount;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "total_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "currency", length = 3)
    private String currency = "USD";

    // Shipping address fields with descriptive names
    @Column(name = "shipping_first_name", length = 100)
    private String shippingFirstName;

    @Column(name = "shipping_last_name", length = 100)
    private String shippingLastName;

    @Column(name = "shipping_email", length = 150)
    private String shippingEmail;

    @Column(name = "shipping_phone", length = 20)
    private String shippingPhone;

    @Column(name = "shipping_address_line1", length = 255)
    private String shippingAddressLine1;

    @Column(name = "shipping_address_line2", length = 255)
    private String shippingAddressLine2;

    @Column(name = "shipping_city", length = 100)
    private String shippingCity;

    @Column(name = "shipping_state", length = 100)
    private String shippingState;

    @Column(name = "shipping_postal_code", length = 20)
    private String shippingPostalCode;

    @Column(name = "shipping_country", length = 100)
    private String shippingCountry = "USA";

    // Billing address fields with descriptive names
    @Column(name = "billing_same_as_shipping")
    private Boolean billingSameAsShipping = true;

    @Column(name = "billing_first_name", length = 100)
    private String billingFirstName;

    @Column(name = "billing_last_name", length = 100)
    private String billingLastName;

    @Column(name = "billing_email", length = 150)
    private String billingEmail;

    @Column(name = "billing_phone", length = 20)
    private String billingPhone;

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

    @Column(name = "billing_country", length = 100)
    private String billingCountry = "USA";

    // Notes fields with descriptive names
    @Column(name = "customer_notes", columnDefinition = "TEXT")
    private String customerNotes;

    @Column(name = "internal_notes", columnDefinition = "TEXT")
    private String internalNotes;

    @Column(name = "gift_message", columnDefinition = "TEXT")
    private String giftMessage;

    // Timestamps with descriptive names
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    // Additional fields for v1.0
    @Column(name = "is_gift")
    private Boolean isGift = false;

    @Column(name = "requires_shipping")
    private Boolean requiresShipping = true;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    // Lifecycle hooks
    @PrePersist
    protected void onCreate() {
        if (orderNumber == null) {
            // Generate order number: ORD-timestamp
            orderNumber = "ORD-" + System.currentTimeMillis();
        }
        if (orderStatus == null) {
            orderStatus = OrderStatus.PENDING;
        }
        if (currency == null) {
            currency = "USD";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        // Update status-specific timestamps
        if (orderStatus == OrderStatus.PAID && paidAt == null) {
            paidAt = LocalDateTime.now();
        }
        if (orderStatus == OrderStatus.SHIPPED && shippedAt == null) {
            shippedAt = LocalDateTime.now();
        }
        if (orderStatus == OrderStatus.DELIVERED && deliveredAt == null) {
            deliveredAt = LocalDateTime.now();
        }
        if (orderStatus == OrderStatus.CANCELLED && cancelledAt == null) {
            cancelledAt = LocalDateTime.now();
        }
    }

    // ========== Helper methods for EmailService ==========

    /**
     * Get customer full name for emails - required by EmailService
     * Uses shipping name if available, otherwise customer name
     */
    public String getCustomerFullName() {
        if (shippingFirstName != null && shippingLastName != null) {
            return shippingFirstName + " " + shippingLastName;
        }
        if (customer != null) {
            return customer.getCustomerFullName();
        }
        return "Valued Customer";
    }

    /**
     * Get formatted shipping address for emails - required by EmailService
     */
    public String getFormattedShippingAddress() {
        StringBuilder address = new StringBuilder();

        if (shippingFirstName != null && shippingLastName != null) {
            address.append(shippingFirstName).append(" ").append(shippingLastName).append("\n");
        }

        if (shippingAddressLine1 != null) {
            address.append(shippingAddressLine1).append("\n");
        }

        if (shippingAddressLine2 != null && !shippingAddressLine2.trim().isEmpty()) {
            address.append(shippingAddressLine2).append("\n");
        }

        if (shippingCity != null) {
            address.append(shippingCity);
        }

        if (shippingState != null && !shippingState.trim().isEmpty()) {
            address.append(", ").append(shippingState);
        }

        if (shippingPostalCode != null) {
            address.append(" ").append(shippingPostalCode);
        }

        address.append("\n");

        if (shippingCountry != null) {
            address.append(shippingCountry);
        }

        if (shippingPhone != null && !shippingPhone.trim().isEmpty()) {
            address.append("\nPhone: ").append(shippingPhone);
        }

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

        if (billingFirstName != null && billingLastName != null) {
            address.append(billingFirstName).append(" ").append(billingLastName).append("\n");
        }

        if (billingAddressLine1 != null) {
            address.append(billingAddressLine1).append("\n");
        }

        if (billingAddressLine2 != null && !billingAddressLine2.trim().isEmpty()) {
            address.append(billingAddressLine2).append("\n");
        }

        if (billingCity != null) {
            address.append(billingCity);
        }

        if (billingState != null && !billingState.trim().isEmpty()) {
            address.append(", ").append(billingState);
        }

        if (billingPostalCode != null) {
            address.append(" ").append(billingPostalCode);
        }

        address.append("\n");

        if (billingCountry != null) {
            address.append(billingCountry);
        }

        if (billingPhone != null && !billingPhone.trim().isEmpty()) {
            address.append("\nPhone: ").append(billingPhone);
        }

        return address.toString();
    }

    // ========== Original helper methods ==========

    /**
     * @deprecated Use getCustomerFullName() instead for consistency
     */
    @Deprecated
    public String getFullShippingName() {
        return shippingFirstName + " " + shippingLastName;
    }

    /**
     * @deprecated Use getFormattedBillingAddress() instead
     */
    @Deprecated
    public String getFullBillingName() {
        if (Boolean.TRUE.equals(billingSameAsShipping)) {
            return getFullShippingName();
        }
        return billingFirstName + " " + billingLastName;
    }

    // ========== Business logic methods ==========

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void removeOrderItem(OrderItem orderItem) {
        orderItems.remove(orderItem);
        orderItem.setOrder(null);
    }

    public BigDecimal calculateSubtotal() {
        return orderItems.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculateTotalAmount() {
        BigDecimal total = subtotal != null ? subtotal : BigDecimal.ZERO;
        if (taxAmount != null) {
            total = total.add(taxAmount);
        }
        if (shippingAmount != null) {
            total = total.add(shippingAmount);
        }
        if (discountAmount != null) {
            total = total.subtract(discountAmount);
        }
        return total;
    }

    public void updateTotals() {
        this.subtotal = calculateSubtotal();
        this.totalAmount = calculateTotalAmount();
    }

    // ========== Status check methods ==========

    public boolean isPaid() {
        return payment != null &&
                (orderStatus == OrderStatus.PAID ||
                        orderStatus == OrderStatus.PROCESSING ||
                        orderStatus == OrderStatus.SHIPPED ||
                        orderStatus == OrderStatus.DELIVERED);
    }

    public boolean canBeCancelled() {
        return orderStatus == OrderStatus.PENDING ||
                orderStatus == OrderStatus.PAID ||
                orderStatus == OrderStatus.PROCESSING;
    }

    public boolean canBeShipped() {
        return orderStatus == OrderStatus.PAID ||
                orderStatus == OrderStatus.PROCESSING;
    }

    public boolean isShipped() {
        return shippedAt != null ||
                orderStatus == OrderStatus.SHIPPED ||
                orderStatus == OrderStatus.DELIVERED;
    }

    public boolean isDelivered() {
        return deliveredAt != null ||
                orderStatus == OrderStatus.DELIVERED;
    }

    public boolean isCancelled() {
        return cancelledAt != null ||
                orderStatus == OrderStatus.CANCELLED;
    }

    public boolean canBeRefunded() {
        return isPaid() && !isCancelled();
    }

    // ========== State transition methods ==========

    public void markAsPaid() {
        if (orderStatus == OrderStatus.PENDING) {
            this.orderStatus = OrderStatus.PAID;
            this.paidAt = LocalDateTime.now();
        }
    }

    public void markAsProcessing() {
        if (isPaid()) {
            this.orderStatus = OrderStatus.PROCESSING;
        }
    }

    public void markAsShipped() {
        if (canBeShipped()) {
            this.orderStatus = OrderStatus.SHIPPED;
            this.shippedAt = LocalDateTime.now();
        }
    }

    public void markAsDelivered() {
        if (isShipped()) {
            this.orderStatus = OrderStatus.DELIVERED;
            this.deliveredAt = LocalDateTime.now();
        }
    }

    public void cancel(String cancellationReason) {
        if (canBeCancelled()) {
            this.orderStatus = OrderStatus.CANCELLED;
            this.cancelledAt = LocalDateTime.now();
            this.internalNotes = (this.internalNotes != null ? this.internalNotes + "\n" : "")
                    + "Cancellation reason: " + cancellationReason;
        }
    }

    /* VERSION 2.0 FEATURES (kommenterat bort för v1.0):
     * - Coupons och rabattkoder
     * - Gift cards
     * - Loyalty points
     * - Multi-currency support med växelkurser
     * - Partial refunds
     * - Split payments
     * - Recurring orders/subscriptions
     * - Order templates
     * - Wishlists
     * - Gift wrapping options
     * - Special delivery instructions
     * - Estimated delivery windows
     * - Order insurance
     * - Express shipping options
     * - Tax exemptions
     * - B2B pricing
     * - Volume discounts
     */
}