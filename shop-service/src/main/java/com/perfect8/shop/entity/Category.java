package com.perfect8.shop.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
 * 
 * FIXED (2025-11-12):
 * - @JoinColumn(name = "parent_id") → "parent_category_id" (matches SQL)
 * FIXED (2025-12-11):
 * - subcategories: FetchType.LAZY → EAGER (fix LazyInitializationException)
 * FIXED (2026-01-16):
 * - Added @JsonBackReference/@JsonManagedReference to prevent infinite recursion
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
    private Long categoryId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "slug", unique = true, nullable = false, length = 100)
    private String slug;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "meta_title", length = 200)
    private String metaTitle;

    @Column(name = "meta_description", length = 500)
    private String metaDescription;

    @Column(name = "meta_keywords", length = 500)
    private String metaKeywords;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    @JsonBackReference
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference
    @Builder.Default
    private List<Category> subcategories = new ArrayList<>();

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Product> products = new ArrayList<>();

    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
        if (active == null) {
            active = true;
        }
        if (sortOrder == null) {
            sortOrder = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }

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
