package com.perfect8.shop.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Cart Item Entity - Represents individual items in shopping cart
 * Version 1.0 - Core shopping cart functionality
 * FIXED: Removed all @Column(name=...) - Hibernate handles camelCase → snake_case
 * FIXED: addedAt/updatedAt → addedDate/updatedDate (Magnum Opus)
 */
@Entity
@Table(name = "cart_items", indexes = {
        @Index(name = "idx_cart_item_cart", columnList = "cart_id"),
        @Index(name = "idx_cart_item_product", columnList = "product_id"),
        @Index(name = "idx_cart_item_cart_product", columnList = "cart_id, product_id", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"cart", "product"})
@ToString(exclude = {"cart", "product"})
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartItemId;  // → DB: cart_item_id

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 1;  // → DB: quantity

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;  // → DB: unit_price

    @Column(precision = 10, scale = 2)
    private BigDecimal discountPrice;  // → DB: discount_price

    @Column(precision = 10, scale = 2)
    private BigDecimal subtotal;  // → DB: subtotal

    // Product snapshot at time of adding to cart
    @Column(nullable = false)
    private String productName;  // → DB: product_name

    @Column(nullable = false)
    private String productSku;  // → DB: product_sku

    @Column
    private String productImageUrl;  // → DB: product_image_url

    // Options and customization (for future)
    @Column(columnDefinition = "TEXT")
    private String selectedOptions;  // → DB: selected_options

    @Column(length = 500)
    private String customMessage;  // → DB: custom_message

    // Status flags
    @Column
    @Builder.Default
    private Boolean isSavedForLater = false;  // → DB: is_saved_for_later

    @Column
    @Builder.Default
    private Boolean isGift = false;  // → DB: is_gift

    @Column(length = 500)
    private String giftMessage;  // → DB: gift_message

    // Stock validation
    private LocalDateTime stockCheckedAt;  // → DB: stock_checked_at

    private Boolean stockAvailable;  // → DB: stock_available

    private Integer requestedQuantity;  // → DB: requested_quantity

    // Notes
    @Column(columnDefinition = "TEXT")
    private String notes;  // → DB: notes

    // Timestamps (Magnum Opus: use Date suffix)
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime addedDate;  // → DB: added_date

    @UpdateTimestamp
    private LocalDateTime updatedDate;  // → DB: updated_date

    // ========================================
    // Business methods
    // ========================================

    /**
     * Calculate subtotal based on quantity and price
     */
    public void calculateSubtotal() {
        BigDecimal effectivePrice = getEffectivePrice();
        if (effectivePrice != null && quantity != null) {
            this.subtotal = effectivePrice.multiply(BigDecimal.valueOf(quantity));
        } else {
            this.subtotal = BigDecimal.ZERO;
        }
    }

    /**
     * Get effective price (discount price if available, otherwise unit price)
     */
    public BigDecimal getEffectivePrice() {
        if (discountPrice != null && discountPrice.compareTo(BigDecimal.ZERO) > 0
                && discountPrice.compareTo(unitPrice) < 0) {
            return discountPrice;
        }
        return unitPrice;
    }

    /**
     * Get total price for this item
     */
    public BigDecimal getTotalPrice() {
        if (subtotal == null) {
            calculateSubtotal();
        }
        return subtotal != null ? subtotal : BigDecimal.ZERO;
    }

    /**
     * Get savings amount if item is on sale
     */
    public BigDecimal getSavingsAmount() {
        if (discountPrice != null && discountPrice.compareTo(BigDecimal.ZERO) > 0
                && discountPrice.compareTo(unitPrice) < 0) {
            return unitPrice.subtract(discountPrice).multiply(BigDecimal.valueOf(quantity));
        }
        return BigDecimal.ZERO;
    }

    /**
     * Check if item is on sale
     */
    public boolean isOnSale() {
        return discountPrice != null
                && discountPrice.compareTo(BigDecimal.ZERO) > 0
                && discountPrice.compareTo(unitPrice) < 0;
    }

    /**
     * Update quantity
     */
    public void updateQuantity(Integer newQuantity) {
        if (newQuantity == null || newQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.quantity = newQuantity;
        calculateSubtotal();
    }

    /**
     * Increment quantity
     */
    public void incrementQuantity() {
        incrementQuantity(1);
    }

    /**
     * Increment quantity by specified amount
     */
    public void incrementQuantity(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Increment amount must be positive");
        }
        this.quantity = (quantity != null ? quantity : 0) + amount;
        calculateSubtotal();
    }

    /**
     * Decrement quantity
     */
    public void decrementQuantity() {
        decrementQuantity(1);
    }

    /**
     * Decrement quantity by specified amount
     */
    public void decrementQuantity(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Decrement amount must be positive");
        }
        if (quantity == null || quantity <= amount) {
            throw new IllegalStateException("Cannot decrement below 1");
        }
        this.quantity -= amount;
        calculateSubtotal();
    }

    /**
     * Update price from product
     */
    public void updatePricesFromProduct() {
        if (product != null) {
            this.unitPrice = product.getPrice();
            this.discountPrice = product.getDiscountPrice();
            this.productName = product.getName();
            this.productSku = product.getSku();
            this.productImageUrl = product.getImageUrl();
            calculateSubtotal();
        }
    }

    /**
     * Check if stock is sufficient
     */
    public boolean hasStockAvailable() {
        if (product == null) {
            return false;
        }
        return product.hasEnoughStock(quantity);
    }

    /**
     * Validate stock
     */
    public void validateStock() {
        this.stockCheckedAt = LocalDateTime.now();
        if (product != null) {
            this.stockAvailable = product.hasEnoughStock(quantity);
            if (!stockAvailable) {
                this.requestedQuantity = quantity;
                // Adjust quantity to available stock
                if (product.getStockQuantity() > 0) {
                    this.quantity = product.getStockQuantity();
                    calculateSubtotal();
                }
            }
        } else {
            this.stockAvailable = false;
        }
    }

    /**
     * Check if stock validation is needed (older than 30 minutes)
     */
    public boolean needsStockValidation() {
        return stockCheckedAt == null ||
                stockCheckedAt.isBefore(LocalDateTime.now().minusMinutes(30));
    }

    /**
     * Toggle save for later
     */
    public void toggleSaveForLater() {
        this.isSavedForLater = !Boolean.TRUE.equals(isSavedForLater);
    }

    /**
     * Mark as gift
     */
    public void markAsGift(String message) {
        this.isGift = true;
        this.giftMessage = message;
    }

    /**
     * Remove gift marking
     */
    public void removeGiftMarking() {
        this.isGift = false;
        this.giftMessage = null;
    }

    /**
     * Get display status
     */
    public String getDisplayStatus() {
        if (Boolean.TRUE.equals(isSavedForLater)) {
            return "Saved for Later";
        }
        if (Boolean.FALSE.equals(stockAvailable)) {
            return "Out of Stock";
        }
        if (product != null && product.isLowStock()) {
            return "Low Stock";
        }
        return "In Cart";
    }

    @PrePersist
    public void prePersist() {
        if (quantity == null || quantity <= 0) {
            quantity = 1;
        }
        if (isSavedForLater == null) {
            isSavedForLater = false;
        }
        if (isGift == null) {
            isGift = false;
        }

        // Set product details if not already set
        if (product != null) {
            if (productName == null) {
                productName = product.getName();
            }
            if (productSku == null) {
                productSku = product.getSku();
            }
            if (productImageUrl == null) {
                productImageUrl = product.getImageUrl();
            }
            if (unitPrice == null) {
                unitPrice = product.getPrice();
            }
            if (product.getDiscountPrice() != null) {
                discountPrice = product.getDiscountPrice();
            }
        }

        calculateSubtotal();
    }

    @PreUpdate
    public void preUpdate() {
        calculateSubtotal();
    }

    /**
     * Check if can checkout
     */
    public boolean canCheckout() {
        return !Boolean.TRUE.equals(isSavedForLater) &&
                Boolean.TRUE.equals(stockAvailable) &&
                quantity != null && quantity > 0;
    }

    /**
     * Get formatted price display
     */
    public String getFormattedPrice() {
        BigDecimal price = getEffectivePrice();
        return price != null ? "SEK " + price.toString() : "SEK 0.00";
    }

    /**
     * Get formatted total display
     */
    public String getFormattedTotal() {
        BigDecimal total = getTotalPrice();
        return "SEK " + total.toString();
    }
}