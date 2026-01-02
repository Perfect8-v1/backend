package com.perfect8.shop.repository;

import com.perfect8.shop.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Category Repository - Version 1.0
 * Magnum Opus Compliant: Uses createdDate/updatedDate (not createdDate/updatedDate)
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Find by slug
    Optional<Category> findBySlug(String slug);

    // Find all active categories
    List<Category> findByActiveTrue();

    // Find all active categories ordered by name
    List<Category> findByActiveTrueOrderByName();

    // Find all active categories ordered by sort order
    List<Category> findByActiveTrueOrderBySortOrder();

    // Find by parent category
    @Query("SELECT c FROM Category c WHERE c.parent = :parent")
    List<Category> findByParent(@Param("parent") Category parent);

    // Find by parent ID
    @Query("SELECT c FROM Category c WHERE c.parent.categoryId = :parentId")
    List<Category> findByParentId(@Param("parentId") Long parentId);

    // Find categories without parent (top-level categories)
    List<Category> findByParentIsNull();

    // Find active categories without parent
    List<Category> findByParentIsNullAndActiveTrue();

    // Search categories by name
    List<Category> findByNameContainingIgnoreCase(String name);

    // Check if slug exists
    boolean existsBySlug(String slug);

    // Check if slug exists excluding a specific ID
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Category c WHERE c.slug = :slug AND c.categoryId != :categoryId")
    boolean existsBySlugAndCategoryIdNot(@Param("slug") String slug, @Param("categoryId") Long categoryId);

    // Count active categories
    long countByActiveTrue();

    // Find categories with product count - FIXED: createdDate, updatedDate
    @Query("SELECT c, COUNT(p) FROM Category c LEFT JOIN Product p ON p.category = c " +
            "WHERE c.active = true " +
            "GROUP BY c.categoryId, c.name, c.description, c.slug, c.parent, c.sortOrder, " +
            "c.active, c.imageUrl, c.metaTitle, c.metaDescription, c.metaKeywords, " +
            "c.createdDate, c.updatedDate " +
            "ORDER BY c.sortOrder, c.name")
    List<Object[]> findCategoriesWithProductCount();

    // Find active subcategories by parent
    @Query("SELECT c FROM Category c WHERE c.parent.categoryId = :parentId AND c.active = true ORDER BY c.sortOrder, c.name")
    List<Category> findActiveSubcategories(@Param("parentId") Long parentId);

    // Find category hierarchy (parent and all children)
    @Query("SELECT c FROM Category c WHERE c.categoryId = :categoryId OR c.parent.categoryId = :categoryId ORDER BY c.sortOrder")
    List<Category> findCategoryHierarchy(@Param("categoryId") Long categoryId);

    // Find all ancestors of a category (breadcrumb)
    @Query(value = "WITH RECURSIVE ancestors AS (" +
            "  SELECT * FROM categories WHERE category_id = :categoryId " +
            "  UNION ALL " +
            "  SELECT c.* FROM categories c " +
            "  INNER JOIN ancestors a ON c.category_id = a.parent_id" +
            ") SELECT * FROM ancestors ORDER BY category_id",
            nativeQuery = true)
    List<Category> findAncestors(@Param("categoryId") Long categoryId);

    // Find all descendants of a category
    @Query(value = "WITH RECURSIVE descendants AS (" +
            "  SELECT * FROM categories WHERE category_id = :categoryId " +
            "  UNION ALL " +
            "  SELECT c.* FROM categories c " +
            "  INNER JOIN descendants d ON c.parent_id = d.category_id" +
            ") SELECT * FROM descendants WHERE category_id != :categoryId ORDER BY sort_order, name",
            nativeQuery = true)
    List<Category> findDescendants(@Param("categoryId") Long categoryId);

    // Count products in category
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.categoryId = :categoryId AND p.active = true")
    Long countProductsInCategory(@Param("categoryId") Long categoryId);

    // Count products in category and its subcategories
    @Query(value = "WITH RECURSIVE category_tree AS (" +
            "  SELECT category_id FROM categories WHERE category_id = :categoryId " +
            "  UNION ALL " +
            "  SELECT c.category_id FROM categories c " +
            "  INNER JOIN category_tree ct ON c.parent_id = ct.category_id" +
            ") SELECT COUNT(DISTINCT p.product_id) FROM products p " +
            "WHERE p.category_id IN (SELECT category_id FROM category_tree) AND p.active = true",
            nativeQuery = true)
    Long countProductsInCategoryTree(@Param("categoryId") Long categoryId);

    // Find categories with at least one product
    @Query("SELECT DISTINCT c FROM Category c INNER JOIN Product p ON p.category = c " +
            "WHERE c.active = true AND p.active = true " +
            "ORDER BY c.sortOrder, c.name")
    List<Category> findCategoriesWithProducts();

    // Find empty categories (no products)
    @Query("SELECT c FROM Category c WHERE c.active = true AND " +
            "NOT EXISTS (SELECT p FROM Product p WHERE p.category = c) " +
            "ORDER BY c.sortOrder, c.name")
    List<Category> findEmptyCategories();

    // Update sort order for multiple categories
    @Query("UPDATE Category c SET c.sortOrder = :sortOrder WHERE c.categoryId = :categoryId")
    void updateSortOrder(@Param("categoryId") Long categoryId, @Param("sortOrder") Integer sortOrder);

    // Find featured categories (you might add an isFeatured field later)
    @Query("SELECT c FROM Category c WHERE c.active = true " +
            "AND EXISTS (SELECT p FROM Product p WHERE p.category = c AND p.featured = true) " +
            "ORDER BY c.sortOrder, c.name")
    List<Category> findFeaturedCategories();

    // Get category statistics
    @Query("SELECT new map(" +
            "COUNT(DISTINCT c.categoryId) as totalCategories, " +
            "COUNT(DISTINCT CASE WHEN c.active = true THEN c.categoryId END) as activeCategories, " +
            "COUNT(DISTINCT CASE WHEN c.parent IS NULL THEN c.categoryId END) as parentCategories, " +
            "COUNT(DISTINCT CASE WHEN c.parent IS NOT NULL THEN c.categoryId END) as subCategories) " +
            "FROM Category c")
    Object getCategoryStatistics();

    // Find categories by multiple IDs
    @Query("SELECT c FROM Category c WHERE c.categoryId IN :categoryIds AND c.active = true")
    List<Category> findByCategoryIdInAndActiveTrue(@Param("categoryIds") List<Long> categoryIds);

    // Find recently added categories - FIXED: createdDate
    List<Category> findTop10ByActiveTrueOrderByCreatedDateDesc();

    // Find recently updated categories - FIXED: updatedDate
    List<Category> findTop10ByActiveTrueOrderByUpdatedDateDesc();
}