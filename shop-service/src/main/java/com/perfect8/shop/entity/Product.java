package com.perfect8.shop.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Product Entity - Version 1.0
 * Magnum Opus Compliant: Descriptive field names (productId not id)
 * FIXED: Removed all @Column(name=...) - Hibernate handles camelCase → snake_case
 */
@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"category", "orderItems", "cartItems"})
@ToString(exclude = {"category", "orderItems", "cartItems"})
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;  // → DB: product_id (Magnum Opus)

    @Column(nullable = false, length = 200)
    private String name;  // → DB: name

    @Column(columnDefinition = "TEXT")
    private String description;  // → DB: description

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;  // → DB: price

    @Column(precision = 10, scale = 2)
    private BigDecimal discountPrice;  // → DB: discount_price

    @Column(unique = true, nullable = false, length = 100)
    private String sku;  // → DB: sku

    @Column(nullable = false)
    @Builder.Default
    private Integer stockQuantity = 0;  // → DB: stock_quantity

    @Column
    @Builder.Default
    private Integer reorderPoint = 10;  // → DB: reorder_point

    @Column
    @Builder.Default
    private Integer reorderQuantity = 50;  // → DB: reorder_quantity

    @Column(length = 500)
    private String imageUrl;  // → DB: image_url

    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url")
    @Builder.Default
    private List<String> additionalImages = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false)
    @Builder.Default
    private boolean isFeatured = false;  // → DB: is_featured

    @Column(nullable = false)
    @Builder.Default
    private boolean isActive = true;  // → DB: is_active

    @Column(precision = 8, scale = 2)
    private BigDecimal weight;  // → DB: weight

    @Column(length = 100)
    private String dimensions;  // → DB: dimensions

    @Column(length = 200)
    private String metaTitle;  // → DB: meta_title

    @Column(length = 500)
    private String metaDescription;  // → DB: meta_description

    @Column(length = 500)
    private String metaKeywords;  // → DB: meta_keywords

    @ElementCollection
    @CollectionTable(name = "product_tags", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "tag")
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @Column
    @Builder.Default
    private Long views = 0L;  // → DB: views

    @Column
    @Builder.Default
    private Long salesCount = 0L;  // → DB: sales_count

    @Column(precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal rating = BigDecimal.ZERO;  // → DB: rating

    @Column
    @Builder.Default
    private Integer reviewCount = 0;  // → DB: review_count

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    @Builder.Default
    private List<CartItem> cartItems = new ArrayList<>();

    private LocalDateTime createdDate;  // → DB: created_date (Magnum Opus)

    private LocalDateTime updatedDate;  // → DB: updated_date (Magnum Opus)

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
        if (stockQuantity == null) {
            stockQuantity = 0;
        }
        if (views == null) {
            views = 0L;
        }
        if (salesCount == null) {
            salesCount = 0L;
        }
        if (reviewCount == null) {
            reviewCount = 0;
        }
        if (rating == null) {
            rating = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }

    // ========== MAGNUM OPUS COMPLIANT ==========
    // Lombok generates: getProductId() / setProductId()
    // No alias methods - one method, one name

    // ========== Business methods ==========

    public boolean isInStock() {
        return stockQuantity != null && stockQuantity > 0;
    }

    public boolean isOutOfStock() {
        return stockQuantity == null || stockQuantity <= 0;
    }

    public boolean isLowStock() {
        return stockQuantity != null && reorderPoint != null && stockQuantity <= reorderPoint;
    }

    public boolean isOnSale() {
        return discountPrice != null && discountPrice.compareTo(price) < 0;
    }

    public BigDecimal getEffectivePrice() {
        if (isOnSale()) {
            return discountPrice;
        }
        return price;
    }

    public BigDecimal getDiscountPercentage() {
        if (!isOnSale()) {
            return BigDecimal.ZERO;
        }
        BigDecimal discount = price.subtract(discountPrice);
        return discount.divide(price, 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }

    public BigDecimal getDiscountAmount() {
        if (!isOnSale()) {
            return BigDecimal.ZERO;
        }
        return price.subtract(discountPrice);
    }

    public void incrementViews() {
        if (views == null) {
            views = 1L;
        } else {
            views++;
        }
    }

    public void incrementSalesCount() {
        incrementSalesCount(1);
    }

    public void incrementSalesCount(int quantity) {
        if (salesCount == null) {
            salesCount = (long) quantity;
        } else {
            salesCount += quantity;
        }
    }

    public void decreaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (stockQuantity == null) {
            stockQuantity = 0;
        }
        if (stockQuantity < quantity) {
            throw new IllegalStateException("Insufficient stock. Available: " + stockQuantity + ", Requested: " + quantity);
        }
        stockQuantity -= quantity;
    }

    public void increaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (stockQuantity == null) {
            stockQuantity = quantity;
        } else {
            stockQuantity += quantity;
        }
    }

    public boolean hasEnoughStock(int quantity) {
        return stockQuantity != null && stockQuantity >= quantity;
    }

    public boolean needsReorder() {
        return isLowStock() && isActive;
    }

    public void addTag(String tag) {
        if (tags == null) {
            tags = new ArrayList<>();
        }
        if (!tags.contains(tag)) {
            tags.add(tag);
        }
    }

    public void removeTag(String tag) {
        if (tags != null) {
            tags.remove(tag);
        }
    }

    public boolean hasTag(String tag) {
        return tags != null && tags.contains(tag);
    }

    public void addImage(String imageUrl) {
        if (additionalImages == null) {
            additionalImages = new ArrayList<>();
        }
        if (!additionalImages.contains(imageUrl)) {
            additionalImages.add(imageUrl);
        }
    }

    public void removeImage(String imageUrl) {
        if (additionalImages != null) {
            additionalImages.remove(imageUrl);
        }
    }

    public List<String> getAllImages() {
        List<String> allImages = new ArrayList<>();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            allImages.add(imageUrl);
        }
        if (additionalImages != null) {
            allImages.addAll(additionalImages);
        }
        return allImages;
    }

    public String getPrimaryImage() {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            return imageUrl;
        }
        if (additionalImages != null && !additionalImages.isEmpty()) {
            return additionalImages.get(0);
        }
        return null;
    }

    public void updateRating(BigDecimal newRating, int newReviewCount) {
        this.rating = newRating;
        this.reviewCount = newReviewCount;
    }

    public boolean hasReviews() {
        return reviewCount != null && reviewCount > 0;
    }

    public boolean isHighlyRated() {
        return rating != null && rating.compareTo(new BigDecimal("4.0")) >= 0;
    }

    public String getAvailabilityStatus() {
        if (!isActive) {
            return "Unavailable";
        }
        if (isOutOfStock()) {
            return "Out of Stock";
        }
        if (isLowStock()) {
            return "Low Stock";
        }
        return "In Stock";
    }

    public boolean canBePurchased() {
        return isActive && isInStock();
    }

    public boolean canBePurchased(int quantity) {
        return isActive && hasEnoughStock(quantity);
    }
}