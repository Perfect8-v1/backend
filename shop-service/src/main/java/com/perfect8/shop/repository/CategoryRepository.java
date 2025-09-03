package com.perfect8.shop.repository;

import com.perfect8.shop.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Find by slug
    Optional<Category> findBySlug(String slug);

    // Find all active categories
    List<Category> findByIsActiveTrue();

    // Find all active categories ordered by name
    List<Category> findByIsActiveTrueOrderByName();

    // Find all active categories ordered by sort order
    List<Category> findByIsActiveTrueOrderBySortOrder();

    // Find by parent category
    List<Category> findByParent(Category parent);

    // Find by parent ID
    List<Category> findByParentId(Long parentId);

    // Find categories without parent (top-level categories)
    List<Category> findByParentIsNull();

    // Find active categories without parent
    List<Category> findByParentIsNullAndIsActiveTrue();

    // Search categories by name
    List<Category> findByNameContainingIgnoreCase(String name);

    // Check if slug exists
    boolean existsBySlug(String slug);

    // Check if slug exists excluding a specific ID
    boolean existsBySlugAndIdNot(String slug, Long id);

    // Count active categories
    long countByIsActiveTrue();

    // Find categories with product count - FIXED: Returns List<Object[]> not List<Object>
    @Query("SELECT c, COUNT(p) FROM Category c LEFT JOIN Product p ON p.category = c " +
            "WHERE c.isActive = true " +
            "GROUP BY c.id, c.name, c.description, c.slug, c.parent, c.sortOrder, " +
            "c.isActive, c.imageUrl, c.metaTitle, c.metaDescription, c.metaKeywords, " +
            "c.createdAt, c.updatedAt " +
            "ORDER BY c.sortOrder, c.name")
    List<Object[]> findCategoriesWithProductCount();

    // Find active subcategories by parent
    @Query("SELECT c FROM Category c WHERE c.parent.id = :parentId AND c.isActive = true ORDER BY c.sortOrder, c.name")
    List<Category> findActiveSubcategories(@Param("parentId") Long parentId);

    // Find category hierarchy (parent and all children)
    @Query("SELECT c FROM Category c WHERE c.id = :categoryId OR c.parent.id = :categoryId ORDER BY c.sortOrder")
    List<Category> findCategoryHierarchy(@Param("categoryId") Long categoryId);

    // Find all ancestors of a category (breadcrumb)
    @Query(value = "WITH RECURSIVE ancestors AS (" +
            "  SELECT * FROM categories WHERE id = :categoryId " +
            "  UNION ALL " +
            "  SELECT c.* FROM categories c " +
            "  INNER JOIN ancestors a ON c.id = a.parent_id" +
            ") SELECT * FROM ancestors ORDER BY id",
            nativeQuery = true)
    List<Category> findAncestors(@Param("categoryId") Long categoryId);

    // Find all descendants of a category
    @Query(value = "WITH RECURSIVE descendants AS (" +
            "  SELECT * FROM categories WHERE id = :categoryId " +
            "  UNION ALL " +
            "  SELECT c.* FROM categories c " +
            "  INNER JOIN descendants d ON c.parent_id = d.id" +
            ") SELECT * FROM descendants WHERE id != :categoryId ORDER BY sort_order, name",
            nativeQuery = true)
    List<Category> findDescendants(@Param("categoryId") Long categoryId);

    // Count products in category
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId AND p.isActive = true")
    Long countProductsInCategory(@Param("categoryId") Long categoryId);

    // Count products in category and its subcategories
    @Query(value = "WITH RECURSIVE category_tree AS (" +
            "  SELECT id FROM categories WHERE id = :categoryId " +
            "  UNION ALL " +
            "  SELECT c.id FROM categories c " +
            "  INNER JOIN category_tree ct ON c.parent_id = ct.id" +
            ") SELECT COUNT(DISTINCT p.id) FROM products p " +
            "WHERE p.category_id IN (SELECT id FROM category_tree) AND p.is_active = true",
            nativeQuery = true)
    Long countProductsInCategoryTree(@Param("categoryId") Long categoryId);

    // Find categories with at least one product
    @Query("SELECT DISTINCT c FROM Category c INNER JOIN Product p ON p.category = c " +
            "WHERE c.isActive = true AND p.isActive = true " +
            "ORDER BY c.sortOrder, c.name")
    List<Category> findCategoriesWithProducts();

    // Find empty categories (no products)
    @Query("SELECT c FROM Category c WHERE c.isActive = true AND " +
            "NOT EXISTS (SELECT p FROM Product p WHERE p.category = c) " +
            "ORDER BY c.sortOrder, c.name")
    List<Category> findEmptyCategories();

    // Update sort order for multiple categories
    @Query("UPDATE Category c SET c.sortOrder = :sortOrder WHERE c.id = :categoryId")
    void updateSortOrder(@Param("categoryId") Long categoryId, @Param("sortOrder") Integer sortOrder);

    // Find featured categories (you might add an isFeatured field later)
    @Query("SELECT c FROM Category c WHERE c.isActive = true " +
            "AND EXISTS (SELECT p FROM Product p WHERE p.category = c AND p.isFeatured = true) " +
            "ORDER BY c.sortOrder, c.name")
    List<Category> findFeaturedCategories();

    // Get category statistics
    @Query("SELECT new map(" +
            "COUNT(DISTINCT c.id) as totalCategories, " +
            "COUNT(DISTINCT CASE WHEN c.isActive = true THEN c.id END) as activeCategories, " +
            "COUNT(DISTINCT CASE WHEN c.parent IS NULL THEN c.id END) as parentCategories, " +
            "COUNT(DISTINCT CASE WHEN c.parent IS NOT NULL THEN c.id END) as subCategories) " +
            "FROM Category c")
    Object getCategoryStatistics();

    // Find categories by multiple IDs
    List<Category> findByIdInAndIsActiveTrue(List<Long> ids);

    // Find recently added categories
    List<Category> findTop10ByIsActiveTrueOrderByCreatedAtDesc();

    // Find recently updated categories
    List<Category> findTop10ByIsActiveTrueOrderByUpdatedAtDesc();
}