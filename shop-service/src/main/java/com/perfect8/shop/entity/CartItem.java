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

@Entity
@Table(name = "cart_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"cart", "product"})
@ToString(exclude = {"cart", "product"})
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 1;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "is_saved_for_later")
    @Builder.Default
    private Boolean isSavedForLater = false;

    @Column(name = "added_at")
    private LocalDateTime addedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        addedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (quantity == null || quantity < 1) {
            quantity = 1;
        }
        if (discountAmount == null) {
            discountAmount = BigDecimal.ZERO;
        }
        if (isSavedForLater == null) {
            isSavedForLater = false;
        }
        // Set unit price from product if not set
        if (unitPrice == null && product != null) {
            unitPrice = product.getEffectivePrice();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Business methods
    public BigDecimal getSubtotal() {
        if (unitPrice == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));

        if (discountAmount != null && discountAmount.compareTo(BigDecimal.ZERO) > 0) {
            subtotal = subtotal.subtract(discountAmount);
            if (subtotal.compareTo(BigDecimal.ZERO) < 0) {
                subtotal = BigDecimal.ZERO;
            }
        }

        return subtotal;
    }

    public BigDecimal getTotalPrice() {
        return getSubtotal();
    }

    public boolean isAvailable() {
        return product != null &&
                product.isActive() &&
                product.getStockQuantity() >= quantity;
    }

    public boolean isInStock() {
        return product != null && product.isInStock();
    }

    public int getAvailableQuantity() {
        if (product == null) {
            return 0;
        }
        return product.getStockQuantity();
    }

    public void incrementQuantity() {
        incrementQuantity(1);
    }

    public void incrementQuantity(int amount) {
        if (quantity == null) {
            quantity = amount;
        } else {
            quantity += amount;
        }
        updatedAt = LocalDateTime.now();
    }

    public void decrementQuantity() {
        decrementQuantity(1);
    }

    public void decrementQuantity(int amount) {
        if (quantity == null) {
            quantity = 0;
        } else {
            quantity -= amount;
            if (quantity < 0) {
                quantity = 0;
            }
        }
        updatedAt = LocalDateTime.now();
    }

    public void updateQuantity(int newQuantity) {
        if (newQuantity < 0) {
            newQuantity = 0;
        }
        this.quantity = newQuantity;
        updatedAt = LocalDateTime.now();
    }

    public void updatePriceFromProduct() {
        if (product != null) {
            this.unitPrice = product.getEffectivePrice();
            updatedAt = LocalDateTime.now();
        }
    }

    public boolean hasDiscount() {
        return discountAmount != null && discountAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    public BigDecimal getEffectiveUnitPrice() {
        if (unitPrice == null) {
            return BigDecimal.ZERO;
        }

        if (hasDiscount() && quantity != null && quantity > 0) {
            BigDecimal discountPerUnit = discountAmount.divide(BigDecimal.valueOf(quantity), 2, BigDecimal.ROUND_HALF_UP);
            BigDecimal effectivePrice = unitPrice.subtract(discountPerUnit);
            return effectivePrice.compareTo(BigDecimal.ZERO) > 0 ? effectivePrice : BigDecimal.ZERO;
        }

        return unitPrice;
    }

    public boolean exceedsStockLimit() {
        return product != null && quantity > product.getStockQuantity();
    }

    public void saveForLater() {
        this.isSavedForLater = true;
        updatedAt = LocalDateTime.now();
    }

    public void moveToCart() {
        this.isSavedForLater = false;
        updatedAt = LocalDateTime.now();
    }

    public boolean isPriceChanged() {
        if (product == null || unitPrice == null) {
            return false;
        }
        return !unitPrice.equals(product.getEffectivePrice());
    }

    public BigDecimal getPriceDifference() {
        if (product == null || unitPrice == null) {
            return BigDecimal.ZERO;
        }
        return product.getEffectivePrice().subtract(unitPrice);
    }
}