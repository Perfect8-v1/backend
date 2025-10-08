package com.perfect8.shop.service;

import com.perfect8.shop.entity.Category;
import com.perfect8.shop.entity.Product;
import com.perfect8.shop.dto.CategoryDTO;
import com.perfect8.shop.repository.CategoryRepository;
import com.perfect8.shop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Category Service - Version 1.0
 * Core category management functionality
 *
 * Magnum Opus Principles:
 * - Readable variable names (categoryId not id)
 * - NO backward compatibility - built right from start
 * - Use createdDate/updatedDate (consistent with Customer entity)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public List<Category> getActiveCategories() {
        return categoryRepository.findByIsActiveTrue();
    }

    public Category getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + categoryId));
    }

    public Category getCategoryBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Category not found with slug: " + slug));
    }

    public List<Category> getParentCategories() {
        return categoryRepository.findByParentIsNull();
    }

    public List<Category> getSubcategoriesByParentId(Long parentId) {
        return categoryRepository.findByParentId(parentId);
    }

    public List<Category> getCategoryTree() {
        List<Category> parentCategories = getParentCategories();
        for (Category parentCategory : parentCategories) {
            parentCategory.setSubcategories(getSubcategoriesByParentId(parentCategory.getCategoryId()));
        }
        return parentCategories;
    }

    public Page<Product> getProductsByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryIdAndIsActiveTrue(categoryId, pageable);
    }

    public Category createCategory(CategoryDTO categoryDTO) {
        Category category = Category.builder()
                .name(categoryDTO.getName())
                .description(categoryDTO.getDescription())
                .slug(categoryDTO.getSlug())
                .isActive(true)
                .createdDate(LocalDateTime.now())
                .build();

        if (categoryDTO.getParentId() != null) {
            Category parentCategory = getCategoryById(categoryDTO.getParentId());
            category.setParent(parentCategory);
        }

        return categoryRepository.save(category);
    }

    public Category updateCategory(Long categoryId, CategoryDTO categoryDTO) {
        Category category = getCategoryById(categoryId);

        category.setName(categoryDTO.getName());
        category.setDescription(categoryDTO.getDescription());
        category.setSlug(categoryDTO.getSlug());
        category.setUpdatedDate(LocalDateTime.now());

        if (categoryDTO.getParentId() != null) {
            Category parentCategory = getCategoryById(categoryDTO.getParentId());
            category.setParent(parentCategory);
        }

        return categoryRepository.save(category);
    }

    public void deleteCategory(Long categoryId) {
        Category category = getCategoryById(categoryId);
        category.setIsActive(false);
        category.setUpdatedDate(LocalDateTime.now());
        categoryRepository.save(category);
    }

    public Category restoreCategory(Long categoryId) {
        Category category = getCategoryById(categoryId);
        category.setIsActive(true);
        category.setUpdatedDate(LocalDateTime.now());
        return categoryRepository.save(category);
    }

    public Category toggleCategoryStatus(Long categoryId) {
        Category category = getCategoryById(categoryId);
        category.setIsActive(!category.getIsActive());
        category.setUpdatedDate(LocalDateTime.now());
        return categoryRepository.save(category);
    }

    public void reorderCategories(List<Long> categoryIds) {
        for (int i = 0; i < categoryIds.size(); i++) {
            Category category = getCategoryById(categoryIds.get(i));
            category.setSortOrder(i + 1);
            categoryRepository.save(category);
        }
    }

    public List<Category> searchCategories(String searchTerm) {
        return categoryRepository.findByNameContainingIgnoreCase(searchTerm);
    }

    public Object getCategoryStatistics() {
        long totalCategories = categoryRepository.count();
        long activeCategories = categoryRepository.countByIsActiveTrue();

        return new CategoryStatistics(totalCategories, activeCategories);
    }

    public List<CategoryWithProductCount> getCategoriesWithProductCount() {
        List<Object[]> rawResults = categoryRepository.findCategoriesWithProductCount();
        List<CategoryWithProductCount> results = new ArrayList<>();

        for (Object[] row : rawResults) {
            // row[0] = Category entity, row[1] = product count
            if (row.length >= 2 && row[0] instanceof Category) {
                Category category = (Category) row[0];
                Long productCount = row[1] != null ? ((Number) row[1]).longValue() : 0L;
                results.add(new CategoryWithProductCount(category, productCount));
            }
        }

        return results;
    }

    public Category moveCategory(Long categoryId, Long newParentId) {
        Category category = getCategoryById(categoryId);

        if (newParentId != null) {
            Category newParentCategory = getCategoryById(newParentId);
            category.setParent(newParentCategory);
        } else {
            category.setParent(null);
        }

        category.setUpdatedDate(LocalDateTime.now());
        return categoryRepository.save(category);
    }

    public List<Category> getCategoryBreadcrumb(Long categoryId) {
        List<Category> breadcrumb = new ArrayList<>();
        Category category = getCategoryById(categoryId);

        while (category != null) {
            breadcrumb.add(0, category);
            category = category.getParent();
        }

        return breadcrumb;
    }

    public boolean isSlugAvailable(String slug, Long excludeCategoryId) {
        if (excludeCategoryId != null) {
            return !categoryRepository.existsBySlugAndCategoryIdNot(slug, excludeCategoryId);
        }
        return !categoryRepository.existsBySlug(slug);
    }

    public List<CategoryDropdownItem> getCategoriesForDropdown() {
        List<Category> categories = categoryRepository.findByIsActiveTrueOrderByName();
        List<CategoryDropdownItem> dropdown = new ArrayList<>();

        for (Category category : categories) {
            dropdown.add(new CategoryDropdownItem(category.getCategoryId(), category.getName()));
        }

        return dropdown;
    }

    // Helper classes
    public static class CategoryStatistics {
        public final long totalCategories;
        public final long activeCategories;

        public CategoryStatistics(long totalCategories, long activeCategories) {
            this.totalCategories = totalCategories;
            this.activeCategories = activeCategories;
        }
    }

    public static class CategoryDropdownItem {
        public final Long categoryId;
        public final String name;

        public CategoryDropdownItem(Long categoryId, String name) {
            this.categoryId = categoryId;
            this.name = name;
        }
    }

    public static class CategoryWithProductCount {
        public final Category category;
        public final Long productCount;

        public CategoryWithProductCount(Category category, Long productCount) {
            this.category = category;
            this.productCount = productCount;
        }

        public Category getCategory() {
            return category;
        }

        public Long getProductCount() {
            return productCount;
        }

        // Helper method to convert to a simple map (for JSON serialization)
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("categoryId", category.getCategoryId());
            map.put("categoryName", category.getName());
            map.put("categorySlug", category.getSlug());
            map.put("productCount", productCount);
            map.put("isActive", category.getIsActive());
            return map;
        }
    }
}