package com.perfect8.shop.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Category Entity - Version 1.0
 * Magnum Opus Compliant: Descriptive field names (categoryId not id)
 * FIXED: Removed all @Column(name=...) - Hibernate handles camelCase → snake_case automatically
 */
@Entity
@Table(name = "categories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"products", "subcategories", "parent"})
@ToString(exclude = {"products", "subcategories", "parent"})
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;  // CHANGED: id → categoryId (Magnum Opus) → DB: category_id

    @Column(nullable = false, length = 100)
    private String name;  // → DB: name

    @Column(length = 500)
    private String description;  // → DB: description

    @Column(unique = true, nullable = false, length = 100)
    private String slug;  // → DB: slug

    @Column(length = 500)
    private String imageUrl;  // → DB: image_url

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;  // → DB: is_active

    @Column
    @Builder.Default
    private Integer sortOrder = 0;  // → DB: sort_order

    @Column(length = 200)
    private String metaTitle;  // → DB: meta_title

    @Column(length = 500)
    private String metaDescription;  // → DB: meta_description

    @Column(length = 500)
    private String metaKeywords;  // → DB: meta_keywords

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")  // KEEP: JoinColumn needs explicit name
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Category> subcategories = new ArrayList<>();

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Product> products = new ArrayList<>();

    private LocalDateTime createdDate;  // → DB: created_date

    private LocalDateTime updatedDate;  // → DB: updated_date

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
        if (sortOrder == null) {
            sortOrder = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }

    // ========== MAGNUM OPUS COMPLIANT ==========
    // Lombok generates: getCategoryId() / setCategoryId()
    // No alias methods - one method, one name

    // ========== Business methods ==========

    public boolean hasProducts() {
        return products != null && !products.isEmpty();
    }

    public boolean hasSubcategories() {
        return subcategories != null && !subcategories.isEmpty();
    }

    public boolean isRootCategory() {
        return parent == null;
    }

    public String getFullPath() {
        if (parent == null) {
            return "/" + slug;
        }

        List<String> pathElements = new ArrayList<>();
        Category current = this;

        while (current != null) {
            pathElements.add(0, current.getSlug());
            current = current.getParent();
        }

        return "/" + String.join("/", pathElements);
    }

    public List<Category> getBreadcrumb() {
        List<Category> breadcrumb = new ArrayList<>();
        Category current = this;

        while (current != null) {
            breadcrumb.add(0, current);
            current = current.getParent();
        }

        return breadcrumb;
    }

    public int getLevel() {
        int level = 0;
        Category current = parent;

        while (current != null) {
            level++;
            current = current.getParent();
        }

        return level;
    }

    public void addSubcategory(Category subcategory) {
        if (subcategories == null) {
            subcategories = new ArrayList<>();
        }
        subcategories.add(subcategory);
        subcategory.setParent(this);
    }

    public void removeSubcategory(Category subcategory) {
        if (subcategories != null) {
            subcategories.remove(subcategory);
            subcategory.setParent(null);
        }
    }

    public void addProduct(Product product) {
        if (products == null) {
            products = new ArrayList<>();
        }
        products.add(product);
        product.setCategory(this);
    }

    public void removeProduct(Product product) {
        if (products != null) {
            products.remove(product);
            product.setCategory(null);
        }
    }
}