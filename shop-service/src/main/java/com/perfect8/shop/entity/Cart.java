package com.perfect8.shop.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Cart Entity - Version 1.0
 * Magnum Opus Compliant: cartId not id, consistent naming with Date suffix
 * FIXED: Removed all @Column(name=...) - Hibernate handles camelCase → snake_case
 */
@Entity
@Table(name = "carts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"customer", "items"})
@ToString(exclude = {"customer", "items"})
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartId;  // → DB: cart_id

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)  // KEEP: explicit foreign key
    private Customer customer;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;  // → DB: total_amount

    @Column
    @Builder.Default
    private Integer itemCount = 0;  // → DB: item_count

    @Column(length = 50)
    private String couponCode;  // → DB: coupon_code

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;  // → DB: discount_amount

    @Column
    @Builder.Default
    private Boolean isSaved = false;  // → DB: is_saved

    @Column(length = 100)
    private String savedName;  // → DB: saved_name

    private LocalDateTime createdDate;  // → DB: created_date (Magnum Opus)

    private LocalDateTime updatedDate;  // → DB: updated_date (Magnum Opus)

    private LocalDateTime expiresDate;  // → DB: expires_date (Magnum Opus)

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
        if (totalAmount == null) {
            totalAmount = BigDecimal.ZERO;
        }
        if (itemCount == null) {
            itemCount = 0;
        }
        if (discountAmount == null) {
            discountAmount = BigDecimal.ZERO;
        }
        if (isSaved == null) {
            isSaved = false;
        }
        // Set cart expiration to 30 days from now if not saved
        if (!isSaved && expiresDate == null) {
            expiresDate = LocalDateTime.now().plusDays(30);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }

    // ========== MAGNUM OPUS COMPLIANT ==========
    // Lombok generates: getCartId() / setCartId()
    // Lombok generates: getCreatedDate() / setCreatedDate()
    // Lombok generates: getUpdatedDate() / setUpdatedDate()
    // Lombok generates: getExpiresDate() / setExpiresDate()
    // No alias methods - one method, one name

    // ========== BUSINESS METHODS ==========

    public void addItem(CartItem item) {
        if (items == null) {
            items = new ArrayList<>();
        }
        items.add(item);
        item.setCart(this);
        recalculateTotals();
    }

    public void removeItem(CartItem item) {
        if (items != null) {
            items.remove(item);
            item.setCart(null);
            recalculateTotals();
        }
    }

    public int getTotalQuantity() {
        if (items == null || items.isEmpty()) {
            return 0;
        }
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    public void recalculateTotals() {
        if (items == null || items.isEmpty()) {
            totalAmount = BigDecimal.ZERO;
            itemCount = 0;
        } else {
            totalAmount = items.stream()
                    .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            itemCount = items.size();
        }

        // Apply discount if present
        if (discountAmount != null && discountAmount.compareTo(BigDecimal.ZERO) > 0) {
            totalAmount = totalAmount.subtract(discountAmount);
            if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
                totalAmount = BigDecimal.ZERO;
            }
        }
    }

    public BigDecimal getSubtotal() {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return items.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }

    public boolean hasItem(Long productId) {
        if (items == null || items.isEmpty()) {
            return false;
        }
        return items.stream()
                .anyMatch(item -> item.getProduct() != null &&
                        productId.equals(item.getProduct().getProductId()));
    }

    public CartItem findItemByProductId(Long productId) {
        if (items == null || items.isEmpty()) {
            return null;
        }
        return items.stream()
                .filter(item -> item.getProduct() != null &&
                        productId.equals(item.getProduct().getProductId()))
                .findFirst()
                .orElse(null);
    }

    public void clearItems() {
        if (items != null) {
            items.clear();
        }
        totalAmount = BigDecimal.ZERO;
        itemCount = 0;
        discountAmount = BigDecimal.ZERO;
        couponCode = null;
    }

    public boolean isExpired() {
        return expiresDate != null && LocalDateTime.now().isAfter(expiresDate);
    }

    public void applyCoupon(String code, BigDecimal discount) {
        this.couponCode = code;
        this.discountAmount = discount;
        recalculateTotals();
    }

    public void removeCoupon() {
        this.couponCode = null;
        this.discountAmount = BigDecimal.ZERO;
        recalculateTotals();
    }

    public BigDecimal getFinalAmount() {
        return totalAmount;
    }
}