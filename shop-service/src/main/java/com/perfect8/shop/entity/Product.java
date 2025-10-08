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
    private Long productId;  // CHANGED: id → productId (Magnum Opus)

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "discount_price", precision = 10, scale = 2)
    private BigDecimal discountPrice;

    @Column(name = "sku", unique = true, nullable = false, length = 100)
    private String sku;

    @Column(name = "stock_quantity", nullable = false)
    @Builder.Default
    private Integer stockQuantity = 0;

    @Column(name = "reorder_point")
    @Builder.Default
    private Integer reorderPoint = 10;

    @Column(name = "reorder_quantity")
    @Builder.Default
    private Integer reorderQuantity = 50;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url")
    @Builder.Default
    private List<String> additionalImages = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "is_featured", nullable = false)
    @Builder.Default
    private boolean isFeatured = false;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @Column(name = "weight", precision = 8, scale = 2)
    private BigDecimal weight;

    @Column(name = "dimensions", length = 100)
    private String dimensions;

    @Column(name = "meta_title", length = 200)
    private String metaTitle;

    @Column(name = "meta_description", length = 500)
    private String metaDescription;

    @Column(name = "meta_keywords", length = 500)
    private String metaKeywords;

    @ElementCollection
    @CollectionTable(name = "product_tags", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "tag")
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @Column(name = "views")
    @Builder.Default
    private Long views = 0L;

    @Column(name = "sales_count")
    @Builder.Default
    private Long salesCount = 0L;

    @Column(name = "rating", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(name = "review_count")
    @Builder.Default
    private Integer reviewCount = 0;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    @Builder.Default
    private List<CartItem> cartItems = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdDate;

    @Column(name = "updated_at")
    private LocalDateTime updatedDate;

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