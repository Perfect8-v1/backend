package com.perfect8.shop.entity;

import com.perfect8.common.enums.OrderStatus;
import com.perfect8.common.utils.OrderStatusHelper;
import com.perfect8.shop.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Order Entity - Version 1.0
 * Magnum Opus Compliant: No alias methods, consistent naming
 * 
 * FIXED (2025-11-12):
 * - Shipment relation: @ManyToOne + @JoinColumn → @OneToOne(mappedBy)
 * - Shipment owns the FK (order_id), not Order!
 */
@Entity
@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"customer", "orderItems", "payments", "shipment"})
@EqualsAndHashCode(exclude = {"customer", "orderItems", "payments", "shipment"})
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @Column(unique = true, nullable = false, length = 50)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrderStatus orderStatus = OrderStatus.PENDING;

    @Column(nullable = false)
    private LocalDateTime orderDate;

    // Pricing fields
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal shippingAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(length = 3)
    @Builder.Default
    private String currency = "SEK";

    // Shipping address fields
    @Column(nullable = false)
    private String shippingFirstName;

    @Column(nullable = false)
    private String shippingLastName;

    @Column(nullable = false)
    private String shippingEmail;

    @Column(length = 20)
    private String shippingPhone;

    @Column(nullable = false)
    private String shippingAddressLine1;

    private String shippingAddressLine2;

    @Column(nullable = false)
    private String shippingCity;

    private String shippingState;

    @Column(nullable = false)
    private String shippingPostalCode;

    @Column(nullable = false, length = 2)
    @Builder.Default
    private String shippingCountry = "SE";

    // Billing address fields
    @Builder.Default
    private Boolean billingSameAsShipping = true;

    private String billingAddressLine1;
    private String billingAddressLine2;
    private String billingCity;
    private String billingState;
    private String billingPostalCode;
    private String billingCountry;

    // ========== RELATIONSHIPS ==========

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();

    @OneToOne(mappedBy = "order", fetch = FetchType.LAZY)  // FIXED: Shipment owns FK (order_id)
    private Shipment shipment;

    // Tracking fields
    @Column(columnDefinition = "TEXT")
    private String customerNotes;

    @Column(columnDefinition = "TEXT")
    private String internalNotes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(nullable = false)
    private LocalDateTime updatedDate;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
        if (orderDate == null) {
            orderDate = LocalDateTime.now();
        }
        if (orderStatus == null) {
            orderStatus = OrderStatus.PENDING;
        }
        if (orderNumber == null) {
            // Generate order number: ORD-YYYYMMDD-RANDOM
            orderNumber = "ORD-" + java.time.LocalDate.now().toString().replace("-", "") +
                          "-" + String.format("%06d", (int)(Math.random() * 1000000));
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }

    // ========== PAYMENT HELPER METHODS ==========

    public Payment getPayment() {
        if (payments == null || payments.isEmpty()) {
            return null;
        }

        return payments.stream()
                .filter(p -> PaymentStatus.COMPLETED.equals(p.getPaymentStatus()))
                .findFirst()
                .orElse(payments.get(payments.size() - 1));
    }

    public void setPayment(Payment payment) {
        if (payment != null) {
            payment.setOrder(this);
            if (this.payments == null) {
                this.payments = new ArrayList<>();
            }
            this.payments.add(payment);
        }
    }

    public void addPayment(Payment payment) {
        if (payment != null) {
            payment.setOrder(this);
            if (this.payments == null) {
                this.payments = new ArrayList<>();
            }
            this.payments.add(payment);
        }
    }

    public boolean hasCompletedPayment() {
        return payments != null && payments.stream()
                .anyMatch(p -> PaymentStatus.COMPLETED.equals(p.getPaymentStatus()));
    }

    public BigDecimal getTotalPaidAmount() {
        if (payments == null || payments.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return payments.stream()
                .filter(p -> PaymentStatus.COMPLETED.equals(p.getPaymentStatus()))
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ========== Business logic methods using OrderStatusHelper ==========

    public boolean canTransitionTo(OrderStatus newStatus) {
        return OrderStatusHelper.canTransition(this.orderStatus, newStatus);
    }

    public void transitionTo(OrderStatus newStatus) {
        if (!canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                    String.format("Cannot transition from %s to %s",
                            this.orderStatus, newStatus)
            );
        }

        this.orderStatus = newStatus;
        OrderStatusHelper.logTransition(this.orderId, this.orderStatus, newStatus);
    }

    public boolean isEditable() {
        return OrderStatusHelper.isEditableState(this.orderStatus);
    }

    public boolean isCancellable() {
        return OrderStatusHelper.isCancellableState(this.orderStatus);
    }

    public boolean isFinalState() {
        return orderStatus == OrderStatus.COMPLETED ||
                orderStatus == OrderStatus.CANCELLED ||
                orderStatus == OrderStatus.REFUNDED;
    }

    public String getNextPossibleStatuses() {
        List<OrderStatus> nextStatuses = OrderStatusHelper.getNextPossibleStatuses(this.orderStatus);
        if (nextStatuses == null || nextStatuses.isEmpty()) {
            return "No transitions available";
        }
        return nextStatuses.stream()
                .map(OrderStatus::name)
                .collect(Collectors.joining(", "));
    }

    public String getRequiredActionsForTransition(OrderStatus targetStatus) {
        List<String> actions = OrderStatusHelper.getRequiredActions(this.orderStatus, targetStatus);
        if (actions == null || actions.isEmpty()) {
            return "No actions required";
        }
        return String.join("; ", actions);
    }

    // ========== Helper methods ==========

    public void addOrderItem(OrderItem item) {
        orderItems.add(item);
        item.setOrder(this);
    }

    public void removeOrderItem(OrderItem item) {
        orderItems.remove(item);
        item.setOrder(null);
    }

    public void calculateTotals() {
        BigDecimal calculatedSubtotal = orderItems.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.subtotal = calculatedSubtotal;
        this.totalAmount = subtotal.add(taxAmount).add(shippingAmount);
    }

    public String getCustomerFullName() {
        if (customer != null) {
            return customer.getFirstName() + " " + customer.getLastName();
        }
        return shippingFirstName + " " + shippingLastName;
    }

    public String getShippingFullName() {
        return shippingFirstName + " " + shippingLastName;
    }
}
