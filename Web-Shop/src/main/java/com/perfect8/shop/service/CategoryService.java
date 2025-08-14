package com.perfect8.shop.service;

import com.perfect8.shop.entity.Category;
import com.perfect8.shop.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * Get all categories
     */
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    /**
     * Get all active categories
     */
    public List<Category> getActiveCategories() {
        return categoryRepository.findByActiveTrue();
    }

    /**
     * Get category by ID
     */
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    /**
     * Get category by name
     */
    public Optional<Category> getCategoryByName(String name) {
        return categoryRepository.findByName(name);
    }

    /**
     * Get categories by parent category
     */
    public List<Category> getCategoriesByParent(Long parentId) {
        return categoryRepository.findByParentCategoryId(parentId);
    }

    /**
     * Get root categories (no parent)
     */
    public List<Category> getRootCategories() {
        return categoryRepository.findByParentCategoryIsNull();
    }

    /**
     * Create new category
     */
    public Category createCategory(Category category) {
        // Set creation timestamp
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());

        // Set default values
        if (category.getActive() == null) {
            category.setActive(true);
        }

        if (category.getDisplayOrder() == null) {
            category.setDisplayOrder(0);
        }

        // Validate parent category exists if specified
        if (category.getParentCategory() != null) {
            Optional<Category> parentCategory = categoryRepository.findById(category.getParentCategory().getId());
            if (parentCategory.isEmpty()) {
                throw new RuntimeException("Parent category not found with ID: " + category.getParentCategory().getId());
            }
        }

        return categoryRepository.save(category);
    }

    /**
     * Update existing category
     */
    public Category updateCategory(Long id, Category updatedCategory) {
        Optional<Category> existingCategoryOpt = categoryRepository.findById(id);

        if (existingCategoryOpt.isEmpty()) {
            throw new RuntimeException("Category not found with ID: " + id);
        }

        Category existingCategory = existingCategoryOpt.get();

        // Update fields
        existingCategory.setName(updatedCategory.getName());
        existingCategory.setDescription(updatedCategory.getDescription());
        existingCategory.setSlug(updatedCategory.getSlug());
        existingCategory.setImageUrl(updatedCategory.getImageUrl());
        existingCategory.setActive(updatedCategory.getActive());
        existingCategory.setDisplayOrder(updatedCategory.getDisplayOrder());
        existingCategory.setUpdatedAt(LocalDateTime.now());

        // Update parent category if specified
        if (updatedCategory.getParentCategory() != null) {
            // Validate parent category exists
            Optional<Category> parentCategory = categoryRepository.findById(updatedCategory.getParentCategory().getId());
            if (parentCategory.isEmpty()) {
                throw new RuntimeException("Parent category not found with ID: " + updatedCategory.getParentCategory().getId());
            }

            // Prevent circular reference
            if (isCircularReference(id, updatedCategory.getParentCategory().getId())) {
                throw new RuntimeException("Circular reference detected: Category cannot be its own parent or descendant");
            }

            existingCategory.setParentCategory(updatedCategory.getParentCategory());
        } else {
            existingCategory.setParentCategory(null);
        }

        return categoryRepository.save(existingCategory);
    }

    /**
     * Delete category (soft delete by setting active = false)
     */
    public void deleteCategory(Long id) {
        Optional<Category> categoryOpt = categoryRepository.findById(id);

        if (categoryOpt.isEmpty()) {
            throw new RuntimeException("Category not found with ID: " + id);
        }

        Category category = categoryOpt.get();

        // Check if category has child categories
        List<Category> childCategories = categoryRepository.findByParentCategoryId(id);
        if (!childCategories.isEmpty()) {
            throw new RuntimeException("Cannot delete category with child categories. Please delete or reassign child categories first.");
        }

        // Soft delete - set active to false
        category.setActive(false);
        category.setUpdatedAt(LocalDateTime.now());
        categoryRepository.save(category);
    }

    /**
     * Hard delete category (permanent deletion)
     */
    public void hardDeleteCategory(Long id) {
        Optional<Category> categoryOpt = categoryRepository.findById(id);

        if (categoryOpt.isEmpty()) {
            throw new RuntimeException("Category not found with ID: " + id);
        }

        // Check if category has child categories
        List<Category> childCategories = categoryRepository.findByParentCategoryId(id);
        if (!childCategories.isEmpty()) {
            throw new RuntimeException("Cannot delete category with child categories. Please delete or reassign child categories first.");
        }

        categoryRepository.deleteById(id);
    }

    /**
     * Get category hierarchy as a tree structure
     */
    public List<Category> getCategoryTree() {
        List<Category> rootCategories = getRootCategories();

        for (Category rootCategory : rootCategories) {
            loadChildCategories(rootCategory);
        }

        return rootCategories;
    }

    /**
     * Recursively load child categories
     */
    private void loadChildCategories(Category category) {
        List<Category> children = categoryRepository.findByParentCategoryId(category.getId());
        category.setSubCategories(children);

        for (Category child : children) {
            loadChildCategories(child);
        }
    }

    /**
     * Check for circular reference when setting parent category
     */
    private boolean isCircularReference(Long categoryId, Long parentId) {
        if (categoryId.equals(parentId)) {
            return true;
        }

        Optional<Category> parentCategory = categoryRepository.findById(parentId);
        if (parentCategory.isEmpty()) {
            return false;
        }

        Category parent = parentCategory.get();
        while (parent.getParentCategory() != null) {
            if (parent.getParentCategory().getId().equals(categoryId)) {
                return true;
            }
            parent = parent.getParentCategory();
        }

        return false;
    }

    /**
     * Count total categories
     */
    public long countAllCategories() {
        return categoryRepository.count();
    }

    /**
     * Count active categories
     */
    public long countActiveCategories() {
        return categoryRepository.countByActiveTrue();
    }

    /**
     * Search categories by name (case-insensitive)
     */
    public List<Category> searchCategoriesByName(String searchTerm) {
        return categoryRepository.findByNameContainingIgnoreCase(searchTerm);
    }

    /**
     * Toggle category active status
     */
    public Category toggleCategoryStatus(Long id) {
        Optional<Category> categoryOpt = categoryRepository.findById(id);

        if (categoryOpt.isEmpty()) {
            throw new RuntimeException("Category not found with ID: " + id);
        }

        Category category = categoryOpt.get();
        category.setActive(!category.getActive());
        category.setUpdatedAt(LocalDateTime.now());

        return categoryRepository.save(category);
    }

    /**
     * Update category display order
     */
    public Category updateDisplayOrder(Long id, Integer displayOrder) {
        Optional<Category> categoryOpt = categoryRepository.findById(id);

        if (categoryOpt.isEmpty()) {
            throw new RuntimeException("Category not found with ID: " + id);
        }

        Category category = categoryOpt.get();
        category.setDisplayOrder(displayOrder);
        category.setUpdatedAt(LocalDateTime.now());

        return categoryRepository.save(category);
    }
}