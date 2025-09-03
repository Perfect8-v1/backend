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
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    @Column(name = "total_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "item_count")
    @Builder.Default
    private Integer itemCount = 0;

    @Column(name = "coupon_code", length = 50)
    private String couponCode;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "is_saved")
    @Builder.Default
    private Boolean isSaved = false;

    @Column(name = "saved_name", length = 100)
    private String savedName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
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
        if (!isSaved && expiresAt == null) {
            expiresAt = LocalDateTime.now().plusDays(30);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Business methods
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
                        productId.equals(item.getProduct().getId()));
    }

    public CartItem findItemByProductId(Long productId) {
        if (items == null || items.isEmpty()) {
            return null;
        }
        return items.stream()
                .filter(item -> item.getProduct() != null &&
                        productId.equals(item.getProduct().getId()))
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
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
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