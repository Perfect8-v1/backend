package com.perfect8.shop.repository;

import com.perfect8.shop.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Product entity - Version 1.0
 * Follows Magnum Opus: productId not id, categoryId not id
 * 
 * FIXED (2025-12-10):
 * - Added JOIN FETCH p.category to prevent LazyInitializationException
 * - All queries that return Product now eager-load Category
 * 
 * FIXED (2026-01-23):
 * - Added findByIdWithCategory for single product lookup with eager loading
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Find by ID with category eager loaded - FIXED: Prevents LazyInitializationException
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.productId = :productId")
    Optional<Product> findByIdWithCategory(@Param("productId") Long productId);

    // Find by SKU
    Optional<Product> findBySku(String sku);

    // Check if SKU exists
    boolean existsBySku(String sku);

    // Find all active products - FIXED: Added JOIN FETCH
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.active = true")
    Page<Product> findByActiveTrue(Pageable pageable);

    // Find by category and active - FIXED: Added JOIN FETCH
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.category.categoryId = :categoryId AND p.active = true")
    Page<Product> findByCategoryIdAndActiveTrue(@Param("categoryId") Long categoryId, Pageable pageable);

    // Find featured and active products - FIXED: Added JOIN FETCH
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.featured = true AND p.active = true")
    Page<Product> findByFeaturedTrueAndActiveTrue(Pageable pageable);

    // Find low stock products - FIXED: Added JOIN FETCH
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.stockQuantity < :threshold AND p.active = true")
    List<Product> findByStockQuantityLessThanAndActiveTrue(@Param("threshold") Integer threshold);

    // Search by name or description - FIXED: Added JOIN FETCH
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.active = true AND " +
            "(LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Product> searchByNameOrDescription(@Param("query") String query, Pageable pageable);

    // Find products with filters - FIXED: Added JOIN FETCH
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.active = true " +
            "AND (:categoryId IS NULL OR p.category.categoryId = :categoryId) " +
            "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
            "AND (:featured IS NULL OR p.featured = :featured) " +
            "AND (:inStock IS NULL OR ((:inStock = true AND p.stockQuantity > 0) OR (:inStock = false AND p.stockQuantity = 0)))")
    Page<Product> findProductsWithFilters(
            @Param("categoryId") Long categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("featured") Boolean featured,
            @Param("inStock") Boolean inStock,
            Pageable pageable
    );

    // Find by price range - FIXED: Added JOIN FETCH
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.price BETWEEN :minPrice AND :maxPrice AND p.active = true")
    Page<Product> findByPriceBetweenAndActiveTrue(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice, Pageable pageable);

    // Find out of stock products - FIXED: Added JOIN FETCH
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.stockQuantity = :stockQuantity AND p.active = true")
    Page<Product> findByStockQuantityAndActiveTrue(@Param("stockQuantity") Integer stockQuantity, Pageable pageable);

    // Find by multiple categories - FIXED: Added JOIN FETCH
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.category.categoryId IN :categoryIds AND p.active = true")
    Page<Product> findByCategoryIdInAndActiveTrue(@Param("categoryIds") List<Long> categoryIds, Pageable pageable);

    // Count by category
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.categoryId = :categoryId AND p.active = true")
    long countByCategoryIdAndActiveTrue(@Param("categoryId") Long categoryId);

    // Find products needing reorder - FIXED: Added JOIN FETCH
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.active = true AND p.stockQuantity <= p.reorderPoint")
    List<Product> findProductsNeedingReorder();

    // Find best sellers - FIXED: Added JOIN FETCH
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.active = true AND p.featured = true")
    Page<Product> findBestSellers(Pageable pageable);

    // Find new arrivals - FIXED: Added JOIN FETCH
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.active = true ORDER BY p.createdDate DESC")
    Page<Product> findNewArrivals(Pageable pageable);

    // Find on sale products - FIXED: Added JOIN FETCH
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.active = true AND p.discountPrice IS NOT NULL AND p.discountPrice < p.price")
    Page<Product> findOnSaleProducts(Pageable pageable);

    // Search by tags - FIXED: Added JOIN FETCH
    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.category JOIN p.tags t WHERE p.active = true AND LOWER(t) IN :tags")
    Page<Product> findByTags(@Param("tags") List<String> tags, Pageable pageable);

    // Complex search with multiple criteria - v1.0 version without brand - FIXED: Added JOIN FETCH
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.active = true AND " +
            "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:sku IS NULL OR p.sku = :sku) AND " +
            "(:categoryId IS NULL OR p.category.categoryId = :categoryId)")
    Page<Product> advancedSearch(
            @Param("name") String name,
            @Param("sku") String sku,
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );

    // Update stock quantity
    @Query("UPDATE Product p SET p.stockQuantity = :quantity WHERE p.productId = :productId")
    void updateStockQuantity(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    // Bulk update prices
    @Query("UPDATE Product p SET p.price = p.price * :multiplier WHERE p.category.categoryId = :categoryId")
    void bulkUpdatePricesByCategory(@Param("categoryId") Long categoryId, @Param("multiplier") BigDecimal multiplier);

    // Find products with no category - FIXED: Added LEFT JOIN FETCH (though category will be null)
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.category IS NULL AND p.active = true")
    List<Product> findByCategoryIsNullAndActiveTrue();

    // Find products by weight range - FIXED: Added JOIN FETCH
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.weight BETWEEN :minWeight AND :maxWeight AND p.active = true")
    Page<Product> findByWeightBetweenAndActiveTrue(@Param("minWeight") BigDecimal minWeight, @Param("maxWeight") BigDecimal maxWeight, Pageable pageable);

    // Statistics queries (no JOIN FETCH needed - returns scalar values)
    @Query("SELECT AVG(p.price) FROM Product p WHERE p.active = true")
    BigDecimal findAveragePrice();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.active = true AND p.stockQuantity = 0")
    long countOutOfStockProducts();

    @Query("SELECT SUM(p.stockQuantity * p.price) FROM Product p WHERE p.active = true")
    BigDecimal calculateTotalInventoryValue();
}
