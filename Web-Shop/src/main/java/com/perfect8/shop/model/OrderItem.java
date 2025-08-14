package com.perfect8.shop.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_items")
@EntityListeners(AuditingEntityListener.class)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Order is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @NotNull(message = "Product is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 9999, message = "Quantity cannot exceed 9999")
    @Column(nullable = false)
    private Integer quantity;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Unit price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Unit price format invalid")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @NotNull(message = "Total price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Total price format invalid")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;

    // Store product details at time of order (for historical accuracy)
    @NotBlank(message = "Product name snapshot is required")
    @Size(max = 100, message = "Product name cannot exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String productNameSnapshot;

    @Size(max = 50, message = "Product SKU cannot exceed 50 characters")
    @Column(length = 50)
    private String productSkuSnapshot;

    @Size(max = 255, message = "Product image URL cannot exceed 255 characters")
    @Column(length = 255)
    private String productImageSnapshot;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public OrderItem() {}

    public OrderItem(Order order, Product product, Integer quantity) {
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = product.getPrice();
        this.totalPrice = calculateTotalPrice();

        // Create snapshots
        this.productNameSnapshot = product.getName();
        this.productSkuSnapshot = product.getSku();
        this.productImageSnapshot = product.getImageUrl();
    }

    public OrderItem(Product product, Integer quantity, BigDecimal unitPrice) {
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = calculateTotalPrice();

        // Create snapshots
        this.productNameSnapshot = product.getName();
        this.productSkuSnapshot = product.getSku();
        this.productImageSnapshot = product.getImageUrl();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        this.totalPrice = calculateTotalPrice();
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        this.totalPrice = calculateTotalPrice();
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getProductNameSnapshot() {
        return productNameSnapshot;
    }

    public void setProductNameSnapshot(String productNameSnapshot) {
        this.productNameSnapshot = productNameSnapshot;
    }

    public String getProductSkuSnapshot() {
        return productSkuSnapshot;
    }

    public void setProductSkuSnapshot(String productSkuSnapshot) {
        this.productSkuSnapshot = productSkuSnapshot;
    }

    public String getProductImageSnapshot() {
        return productImageSnapshot;
    }

    public void setProductImageSnapshot(String productImageSnapshot) {
        this.productImageSnapshot = productImageSnapshot;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Business Methods
    public BigDecimal calculateTotalPrice() {
        if (unitPrice == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public void updateTotalPrice() {
        this.totalPrice = calculateTotalPrice();
    }

    public boolean isPriceChanged() {
        if (product == null || unitPrice == null) {
            return false;
        }
        return !unitPrice.equals(product.getPrice());
    }

    public BigDecimal getPriceDifference() {
        if (product == null || unitPrice == null) {
            return BigDecimal.ZERO;
        }
        return product.getPrice().subtract(unitPrice);
    }

    public void updateSnapshots() {
        if (product != null) {
            this.productNameSnapshot = product.getName();
            this.productSkuSnapshot = product.getSku();
            this.productImageSnapshot = product.getImageUrl();
        }
    }

    public boolean isProductStillAvailable() {
        return product != null && product.getActive() && product.isInStock(quantity);
    }

    public BigDecimal getSavingsPerUnit() {
        if (product == null || unitPrice == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal currentPrice = product.getPrice();
        if (currentPrice.compareTo(unitPrice) > 0) {
            return currentPrice.subtract(unitPrice);
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getTotalSavings() {
        BigDecimal savingsPerUnit = getSavingsPerUnit();
        if (savingsPerUnit.compareTo(BigDecimal.ZERO) > 0) {
            return savingsPerUnit.multiply(BigDecimal.valueOf(quantity));
        }
        return BigDecimal.ZERO;
    }

    @PrePersist
    @PreUpdate
    private void calculateAndSetTotalPrice() {
        this.totalPrice = calculateTotalPrice();
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "id=" + id +
                ", productName='" + productNameSnapshot + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", totalPrice=" + totalPrice +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem orderItem = (OrderItem) o;
        return id != null && id.equals(orderItem.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}