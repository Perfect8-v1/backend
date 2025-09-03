package com.perfect8.shop.dto;

import java.time.LocalDateTime;
import java.util.List;

public class CategoryResponse {
    private Long id;
    private String name;
    private String description;
    private String slug;
    private String imageUrl;
    private Boolean active;
    private Integer displayOrder;
    private Long parentCategoryId;
    private String parentCategoryName;
    private List<CategoryResponse> subCategories;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public CategoryResponse() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public Long getParentCategoryId() { return parentCategoryId; }
    public void setParentCategoryId(Long parentCategoryId) { this.parentCategoryId = parentCategoryId; }

    public String getParentCategoryName() { return parentCategoryName; }
    public void setParentCategoryName(String parentCategoryName) { this.parentCategoryName = parentCategoryName; }

    public List<CategoryResponse> getSubCategories() { return subCategories; }
    public void setSubCategories(List<CategoryResponse> subCategories) { this.subCategories = subCategories; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
