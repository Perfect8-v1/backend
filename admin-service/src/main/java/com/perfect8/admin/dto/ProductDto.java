package com.perfect8.admin.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ProductDto {
    private Long productDtoId;

    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name cannot exceed 255 characters")
    private String name;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Stock quantity is required")
    private Integer stockQuantity;

    private String sku;
    private Long categoryId;
    private String categoryName;
    private List<String> images;
    private String mainImage;
    private boolean active;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private String metaTitle;
    private String metaDescription;
    private List<String> tags;
    private BigDecimal weight;
    private String dimensions;

    public ProductDto() {}

    // Getters and Setters
    public Long getProductDtoId() { return productDtoId; }
    public void setProductDtoId(Long productDtoId) { this.productDtoId = productDtoId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
    public String getMainImage() { return mainImage; }
    public void setMainImage(String mainImage) { this.mainImage = mainImage; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }
    public String getMetaTitle() { return metaTitle; }
    public void setMetaTitle(String metaTitle) { this.metaTitle = metaTitle; }
    public String getMetaDescription() { return metaDescription; }
    public void setMetaDescription(String metaDescription) { this.metaDescription = metaDescription; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public BigDecimal getWeight() { return weight; }
    public void setWeight(BigDecimal weight) { this.weight = weight; }
    public String getDimensions() { return dimensions; }
    public void setDimensions(String dimensions) { this.dimensions = dimensions; }
}
