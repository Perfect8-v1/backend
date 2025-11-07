package com.perfect8.shop.controller;

import com.perfect8.shop.entity.Category;
import com.perfect8.shop.entity.Product;
import com.perfect8.shop.service.CategoryService;
import com.perfect8.shop.dto.CategoryDTO;
import com.perfect8.shop.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Get all categories (Public endpoint)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Category>>> getAllCategories() {
        try {
            List<Category> categories = categoryService.getAllCategories();
            return ResponseEntity.ok(ApiResponse.success("Categories retrieved successfully", categories));
        } catch (Exception e) {
            log.error("Error retrieving categories: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve categories: " + e.getMessage()));
        }
    }

    /**
     * Get all active categories (Public endpoint)
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<Category>>> getActiveCategories() {
        try {
            List<Category> categories = categoryService.getActiveCategories();
            return ResponseEntity.ok(ApiResponse.success("Active categories retrieved successfully", categories));
        } catch (Exception e) {
            log.error("Error retrieving active categories: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve active categories: " + e.getMessage()));
        }
    }

    /**
     * Get category by ID (Public endpoint)
     */
    @GetMapping("/{customerEmailDTOId}")
    public ResponseEntity<ApiResponse<Category>> getCategoryById(@PathVariable Long id) {
        try {
            Category category = categoryService.getCategoryById(id);
            return ResponseEntity.ok(ApiResponse.success("Category retrieved successfully", category));
        } catch (Exception e) {
            log.error("Error retrieving category {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Failed to retrieve category: " + e.getMessage()));
        }
    }

    /**
     * Get category by slug (Public endpoint)
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<Category>> getCategoryBySlug(@PathVariable String slug) {
        try {
            Category category = categoryService.getCategoryBySlug(slug);
            return ResponseEntity.ok(ApiResponse.success("Category retrieved successfully", category));
        } catch (Exception e) {
            log.error("Error retrieving category by slug {}: {}", slug, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Failed to retrieve category: " + e.getMessage()));
        }
    }

    /**
     * Get parent categories (Public endpoint)
     */
    @GetMapping("/parents")
    public ResponseEntity<ApiResponse<List<Category>>> getParentCategories() {
        try {
            List<Category> parentCategories = categoryService.getParentCategories();
            return ResponseEntity.ok(ApiResponse.success("Parent categories retrieved successfully", parentCategories));
        } catch (Exception e) {
            log.error("Error retrieving parent categories: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve parent categories: " + e.getMessage()));
        }
    }

    /**
     * Get subcategories by parent ID (Public endpoint)
     */
    @GetMapping("/{parentId}/subcategories")
    public ResponseEntity<ApiResponse<List<Category>>> getSubcategories(@PathVariable Long parentId) {
        try {
            List<Category> subcategories = categoryService.getSubcategoriesByParentId(parentId);
            return ResponseEntity.ok(ApiResponse.success("Subcategories retrieved successfully", subcategories));
        } catch (Exception e) {
            log.error("Error retrieving subcategories for parent {}: {}", parentId, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve subcategories: " + e.getMessage()));
        }
    }

    /**
     * Get category tree (Public endpoint)
     */
    @GetMapping("/tree")
    public ResponseEntity<ApiResponse<List<Category>>> getCategoryTree() {
        try {
            List<Category> categoryTree = categoryService.getCategoryTree();
            return ResponseEntity.ok(ApiResponse.success("Category tree retrieved successfully", categoryTree));
        } catch (Exception e) {
            log.error("Error retrieving category tree: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve category tree: " + e.getMessage()));
        }
    }

    /**
     * Get products by category with pagination (Public endpoint)
     */
    @GetMapping("/{customerEmailDTOId}/products")
    public ResponseEntity<ApiResponse<Page<Product>>> getProductsByCategory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc")
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Product> products = categoryService.getProductsByCategory(id, pageable);
            return ResponseEntity.ok(ApiResponse.success("Products retrieved successfully", products));
        } catch (Exception e) {
            log.error("Error retrieving products for category {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve products: " + e.getMessage()));
        }
    }

    /**
     * Create new category (Admin only)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Category>> createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        try {
            Category newCategory = categoryService.createCategory(categoryDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Category created successfully", newCategory));
        } catch (Exception e) {
            log.error("Error creating category: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to create category: " + e.getMessage()));
        }
    }

    /**
     * Update category (Admin only)
     */
    @PutMapping("/{customerEmailDTOId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Category>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryDTO categoryDTO) {
        try {
            Category updatedCategory = categoryService.updateCategory(id, categoryDTO);
            return ResponseEntity.ok(ApiResponse.success("Category updated successfully", updatedCategory));
        } catch (Exception e) {
            log.error("Error updating category {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to update category: " + e.getMessage()));
        }
    }

    /**
     * Delete category (Admin only) - Soft delete
     */
    @DeleteMapping("/{customerEmailDTOId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteCategory(@PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.ok(ApiResponse.success("Category deleted successfully", "Category has been deactivated"));
        } catch (Exception e) {
            log.error("Error deleting category {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to delete category: " + e.getMessage()));
        }
    }

    /**
     * Restore deleted category (Admin only)
     */
    @PutMapping("/{customerEmailDTOId}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Category>> restoreCategory(@PathVariable Long id) {
        try {
            Category restoredCategory = categoryService.restoreCategory(id);
            return ResponseEntity.ok(ApiResponse.success("Category restored successfully", restoredCategory));
        } catch (Exception e) {
            log.error("Error restoring category {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to restore category: " + e.getMessage()));
        }
    }

    /**
     * Toggle category active status (Admin only)
     */
    @PutMapping("/{customerEmailDTOId}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Category>> toggleCategoryStatus(@PathVariable Long id) {
        try {
            Category category = categoryService.toggleCategoryStatus(id);
            String status = category.getIsActive() ? "activated" : "deactivated";
            return ResponseEntity.ok(ApiResponse.success("Category " + status + " successfully", category));
        } catch (Exception e) {
            log.error("Error toggling category {} status: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to toggle category status: " + e.getMessage()));
        }
    }

    /**
     * Reorder categories (Admin only)
     */
    @PutMapping("/reorder")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> reorderCategories(@RequestBody List<Long> categoryIds) {
        try {
            categoryService.reorderCategories(categoryIds);
            return ResponseEntity.ok(ApiResponse.success("Categories reordered successfully", "Display order updated"));
        } catch (Exception e) {
            log.error("Error reordering categories: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to reorder categories: " + e.getMessage()));
        }
    }

    /**
     * Search categories (Admin only)
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Category>>> searchCategories(@RequestParam String searchTerm) {
        try {
            List<Category> categories = categoryService.searchCategories(searchTerm);
            return ResponseEntity.ok(ApiResponse.success("Category search completed successfully", categories));
        } catch (Exception e) {
            log.error("Error searching categories: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to search categories: " + e.getMessage()));
        }
    }

    /**
     * Get category statistics (Admin only)
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> getCategoryStatistics() {
        try {
            Object statistics = categoryService.getCategoryStatistics();
            return ResponseEntity.ok(ApiResponse.success("Category statistics retrieved successfully", statistics));
        } catch (Exception e) {
            log.error("Error retrieving category statistics: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve category statistics: " + e.getMessage()));
        }
    }

    /**
     * Get categories with product count (Admin only)
     * FIXED: Changed return type to handle CategoryWithProductCount
     */
    @GetMapping("/with-product-count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<CategoryService.CategoryWithProductCount>>> getCategoriesWithProductCount() {
        try {
            // FIXED: Using the correct return type from CategoryService
            List<CategoryService.CategoryWithProductCount> categoriesWithCount = categoryService.getCategoriesWithProductCount();
            return ResponseEntity.ok(ApiResponse.success("Categories with product count retrieved successfully", categoriesWithCount));
        } catch (Exception e) {
            log.error("Error retrieving categories with product count: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve categories with product count: " + e.getMessage()));
        }
    }

    /**
     * Move category to different parent (Admin only)
     */
    @PutMapping("/{customerEmailDTOId}/move")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Category>> moveCategory(
            @PathVariable Long id,
            @RequestParam(required = false) Long newParentId) {
        try {
            Category movedCategory = categoryService.moveCategory(id, newParentId);
            return ResponseEntity.ok(ApiResponse.success("Category moved successfully", movedCategory));
        } catch (Exception e) {
            log.error("Error moving category {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to move category: " + e.getMessage()));
        }
    }

    /**
     * Get category breadcrumb (Public endpoint)
     */
    @GetMapping("/{customerEmailDTOId}/breadcrumb")
    public ResponseEntity<ApiResponse<List<Category>>> getCategoryBreadcrumb(@PathVariable Long id) {
        try {
            List<Category> breadcrumb = categoryService.getCategoryBreadcrumb(id);
            return ResponseEntity.ok(ApiResponse.success("Category breadcrumb retrieved successfully", breadcrumb));
        } catch (Exception e) {
            log.error("Error retrieving category breadcrumb for {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve category breadcrumb: " + e.getMessage()));
        }
    }

    /**
     * Check if category slug is available (Admin only)
     */
    @GetMapping("/check-slug")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> checkSlugAvailable(
            @RequestParam String slug,
            @RequestParam(required = false) Long excludeId) {
        try {
            boolean available = categoryService.isSlugAvailable(slug, excludeId);
            return ResponseEntity.ok(ApiResponse.success("Slug check completed", available));
        } catch (Exception e) {
            log.error("Error checking slug availability: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to check slug availability: " + e.getMessage()));
        }
    }

    /**
     * Get categories for dropdown/select (Admin only)
     * FIXED: Changed return type to handle CategoryDropdownItem
     */
    @GetMapping("/dropdown")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<CategoryService.CategoryDropdownItem>>> getCategoriesForDropdown() {
        try {
            // FIXED: Using the correct return type from CategoryService
            List<CategoryService.CategoryDropdownItem> dropdownCategories = categoryService.getCategoriesForDropdown();
            return ResponseEntity.ok(ApiResponse.success("Dropdown categories retrieved successfully", dropdownCategories));
        } catch (Exception e) {
            log.error("Error retrieving dropdown categories: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve dropdown categories: " + e.getMessage()));
        }
    }
}