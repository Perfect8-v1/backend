package com.perfect8.shop.dto;

import java.util.List;

public class CategoryTreeResponse {
    private List<CategoryResponse> categories;
    private Integer totalCategories;
    private Integer activeCategories;

    // Constructors
    public CategoryTreeResponse() {}

    public CategoryTreeResponse(List<CategoryResponse> categories) {
        this.categories = categories;
        this.totalCategories = categories != null ? categories.size() : 0;
    }

    // Getters and Setters
    public List<CategoryResponse> getCategories() { return categories; }
    public void setCategories(List<CategoryResponse> categories) {
        this.categories = categories;
        this.totalCategories = categories != null ? categories.size() : 0;
    }

    public Integer getTotalCategories() { return totalCategories; }
    public void setTotalCategories(Integer totalCategories) { this.totalCategories = totalCategories; }

    public Integer getActiveCategories() { return activeCategories; }
    public void setActiveCategories(Integer activeCategories) { this.activeCategories = activeCategories; }
}