package com.perfect8.shop.dto;

import java.time.LocalDateTime;
import java.util.List;

public class CategoryResponse {
    private Long categoryResponseId;
    private String name;
    private String description;
    private String slug;
    private String imageUrl;
    private Boolean active;
    private Integer displayOrder;
    private Long parentCategoryId;
    private String parentCategoryName;
    private List<CategoryResponse> subCategories;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    // Constructors
    public CategoryResponse() {}

    // Getters and Setters
    public Long getCategoryResponseId() { return categoryResponseId; }
    public void setCategoryResponseId(Long categoryResponseId) { this.categoryResponseId = categoryResponseId; }

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

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }
}
