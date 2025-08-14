package com.perfect8.shop.controller;

import com.perfect8.shop.model.Category;
import com.perfect8.shop.service.CategoryService;
import com.perfect8.shop.dto.CategoryCreateRequest;
import com.perfect8.shop.dto.CategoryUpdateRequest;
import com.perfect8.shop.dto.CategoryResponse;
import com.perfect8.shop.dto.CategoryTreeResponse;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Category management
 *
 * Endpoints:
 * GET    /api/shop/categories           - Get all categories
 * GET    /api/shop/categories/tree      - Get category tree (hierarchical)
 * GET    /api/shop/categories/root      - Get root categories only
 * GET    /api/shop/categories/{id}      - Get category by ID
 * GET    /api/shop/categories/{id}/children - Get subcategories
 * GET    /api/shop/categories/slug/{slug} - Get category by slug
 * POST   /api/shop/categories           - Create new category (ADMIN)
 * PUT    /api/shop/categories/{id}      - Update category (ADMIN)
 * DELETE /api/shop/categories/{id}      - Delete category (ADMIN)
 * PATCH  /api/shop/categories/{id}/move - Move category to new parent (ADMIN)
 */
@RestController
@RequestMapping("/api/shop/categories")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080", "http://localhost:8081"})
public class CategoryController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * Get all categories with pagination
     *
     * @param pageable Pagination parameters
     * @param active Optional active status filter
     * @param parentId Optional parent category filter
     * @return Paginated list of categories
     */
    @GetMapping
    public ResponseEntity<Page<CategoryResponse>> getAllCategories(
            @PageableDefault(size = 50, sort = "displayOrder") Pageable pageable,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Long parentId) {

        Page<CategoryResponse> categories = categoryService.findCategories(pageable, active, parentId);
        return ResponseEntity.ok(categories);
    }

    /**
     * Get category tree (hierarchical structure)
     *
     * @param activeOnly Whether to include only active categories
     * @param maxDepth Maximum depth of the tree (default: unlimited)
     * @return Hierarchical category tree
     */
    @GetMapping("/tree")
    public ResponseEntity<List<CategoryTreeResponse>> getCategoryTree(
            @RequestParam(defaultValue = "true") Boolean activeOnly,
            @RequestParam(required = false) Integer maxDepth) {

        List<CategoryTreeResponse> categoryTree = categoryService.getCategoryTree(activeOnly, maxDepth);
        return ResponseEntity.ok(categoryTree);
    }

    /**
     * Get root categories only (top-level categories)
     *
     * @param activeOnly Whether to include only active categories
     * @return List of root categories
     */
    @GetMapping("/root")
    public ResponseEntity<List<CategoryResponse>> getRootCategories(
            @RequestParam(defaultValue = "true") Boolean activeOnly) {

        List<CategoryResponse> rootCategories = categoryService.findRootCategories(activeOnly);
        return ResponseEntity.ok(rootCategories);
    }

    /**
     * Get category by ID
     *
     * @param id Category ID
     * @param includeProducts Whether to include product count
     * @return Category details
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") Boolean includeProducts) {

        CategoryResponse category = categoryService.findById(id, includeProducts);
        return ResponseEntity.ok(category);
    }

    /**
     * Get category by slug
     *
     * @param slug Category slug
     * @param includeProducts Whether to include product count
     * @return Category details
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<CategoryResponse> getCategoryBySlug(
            @PathVariable String slug,
            @RequestParam(defaultValue = "true") Boolean includeProducts) {

        CategoryResponse category = categoryService.findBySlug(slug, includeProducts);
        return ResponseEntity.ok(category);
    }

    /**
     * Get subcategories of a category
     *
     * @param id Parent category ID
     * @param activeOnly Whether to include only active subcategories
     * @return List of subcategories
     */
    @GetMapping("/{id}/children")
    public ResponseEntity<List<CategoryResponse>> getSubcategories(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") Boolean activeOnly) {

        List<CategoryResponse> subcategories = categoryService.findSubcategories(id, activeOnly);
        return ResponseEntity.ok(subcategories);
    }

    /**
     * Get category path (breadcrumb) from root to specified category
     *
     * @param id Category ID
     * @return List of categories from root to specified category
     */
    @GetMapping("/{id}/path")
    public ResponseEntity<List<CategoryResponse>> getCategoryPath(@PathVariable Long id) {
        List<CategoryResponse> path = categoryService.getCategoryPath(id);
        return ResponseEntity.ok(path);
    }

    /**
     * Search categories by name
     *
     * @param query Search query
     * @param pageable Pagination parameters
     * @return Paginated search results
     */
    @GetMapping("/search")
    public ResponseEntity<Page<CategoryResponse>> searchCategories(
            @RequestParam String query,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<CategoryResponse> categories = categoryService.searchCategories(query, pageable);
        return ResponseEntity.ok(categories);
    }

    /**
     * Get popular categories (by product count)
     *
     * @param limit Maximum number of categories to return
     * @return List of popular categories
     */
    @GetMapping("/popular")
    public ResponseEntity<List<CategoryStatsResponse>> getPopularCategories(
            @RequestParam(defaultValue = "10") int limit) {

        List<CategoryStatsResponse> categories = categoryService.getPopularCategories(limit);
        return ResponseEntity.ok(categories);
    }

    /**
     * Create new category (Admin only)
     *
     * @param request Category creation request
     * @return Created category
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryCreateRequest request) {
        CategoryResponse category = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }

    /**
     * Update existing category (Admin only)
     *
     * @param id Category ID
     * @param request Category update request
     * @return Updated category
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryUpdateRequest request) {

        CategoryResponse category = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(category);
    }

    /**
     * Move category to new parent (Admin only)
     *
     * @param id Category ID to move
     * @param newParentId New parent category ID (null for root)
     * @return Updated category
     */
    @PatchMapping("/{id}/move")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> moveCategory(
            @PathVariable Long id,
            @RequestParam(required = false) Long newParentId) {

        CategoryResponse category = categoryService.moveCategory(id, newParentId);
        return ResponseEntity.ok(category);
    }

    /**
     * Update category display order (Admin only)
     *
     * @param id Category ID
     * @param displayOrder New display order
     * @return Updated category
     */
    @PatchMapping("/{id}/order")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> updateDisplayOrder(
            @PathVariable Long id,
            @RequestParam Integer displayOrder) {

        CategoryResponse category = categoryService.updateDisplayOrder(id, displayOrder);
        return ResponseEntity.ok(category);
    }

    /**
     * Toggle category active status (Admin only)
     *
     * @param id Category ID
     * @return Updated category
     */
    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> toggleActiveStatus(@PathVariable Long id) {
        CategoryResponse category = categoryService.toggleActiveStatus(id);
        return ResponseEntity.ok(category);
    }

    /**
     * Delete category (Admin only)
     * Will fail if category has products or subcategories
     *
     * @param id Category ID
     * @param force Whether to force delete (move products to parent category)
     * @return No content response
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") Boolean force) {

        categoryService.deleteCategory(id, force);
        return ResponseEntity.noContent().build();
    }

    /**
     * Bulk update category order (Admin only)
     *
     * @param updates List of category order updates
     * @return Updated categories
     */
    @PatchMapping("/bulk-order")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CategoryResponse>> bulkUpdateOrder(
            @Valid @RequestBody List<CategoryOrderUpdate> updates) {

        List<CategoryResponse> categories = categoryService.bulkUpdateOrder(updates);
        return ResponseEntity.ok(categories);
    }

    /**
     * Get category statistics (Admin only)
     *
     * @param id Category ID
     * @return Category statistics
     */
    @GetMapping("/{id}/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryStatsResponse> getCategoryStats(@PathVariable Long id) {
        CategoryStatsResponse stats = categoryService.getCategoryStats(id);
        return ResponseEntity.ok(stats);
    }

    // DTO Classes for responses
    public static class CategoryStatsResponse {
        private Long categoryId;
        private String categoryName;
        private Integer productCount;
        private Integer activeProductCount;
        private Integer subcategoryCount;
        private Double averageProductPrice;
        private Integer totalViews;

        // Constructors, getters, setters
        public CategoryStatsResponse() {}

        public CategoryStatsResponse(Long categoryId, String categoryName, Integer productCount,
                                     Integer activeProductCount, Integer subcategoryCount,
                                     Double averageProductPrice, Integer totalViews) {
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.productCount = productCount;
            this.activeProductCount = activeProductCount;
            this.subcategoryCount = subcategoryCount;
            this.averageProductPrice = averageProductPrice;
            this.totalViews = totalViews;
        }

        // Getters and setters
        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

        public Integer getProductCount() { return productCount; }
        public void setProductCount(Integer productCount) { this.productCount = productCount; }

        public Integer getActiveProductCount() { return activeProductCount; }
        public void setActiveProductCount(Integer activeProductCount) { this.activeProductCount = activeProductCount; }

        public Integer getSubcategoryCount() { return subcategoryCount; }
        public void setSubcategoryCount(Integer subcategoryCount) { this.subcategoryCount = subcategoryCount; }

        public Double getAverageProductPrice() { return averageProductPrice; }
        public void setAverageProductPrice(Double averageProductPrice) { this.averageProductPrice = averageProductPrice; }

        public Integer getTotalViews() { return totalViews; }
        public void setTotalViews(Integer totalViews) { this.totalViews = totalViews; }
    }

    public static class CategoryOrderUpdate {
        private Long categoryId;
        private Integer displayOrder;

        // Constructors, getters, setters
        public CategoryOrderUpdate() {}

        public CategoryOrderUpdate(Long categoryId, Integer displayOrder) {
            this.categoryId = categoryId;
            this.displayOrder = displayOrder;
        }

        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

        public Integer getDisplayOrder() { return displayOrder; }
        public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
    }
}