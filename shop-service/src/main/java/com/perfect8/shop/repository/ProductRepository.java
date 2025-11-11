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
 * Follows Magnum Opus: productId not customerEmailDTOId, categoryId not customerEmailDTOId
 * FIXED: All category references use explicit queries with p.category.categoryId
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Find by SKU
    Optional<Product> findBySku(String sku);

    // Check if SKU exists
    boolean existsBySku(String sku);

    // Find all active products
    Page<Product> findByActiveTrue(Pageable pageable);

    // Find by category and active - FIXED: Use explicit query
    @Query("SELECT p FROM Product p WHERE p.category.categoryId = :categoryId AND p.active = true")
    Page<Product> findByCategoryIdAndActiveTrue(@Param("categoryId") Long categoryId, Pageable pageable);

    // Find featured and active products
    Page<Product> findByIsFeaturedTrueAndActiveTrue(Pageable pageable);

    // Find low stock products
    List<Product> findByStockQuantityLessThanAndActiveTrue(Integer threshold);

    // Search by name or description
    @Query("SELECT p FROM Product p WHERE p.active = true AND " +
            "(LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Product> searchByNameOrDescription(@Param("query") String query, Pageable pageable);

    // Find products with filters
    @Query("SELECT p FROM Product p WHERE p.active = true " +
            "AND (:categoryId IS NULL OR p.category.categoryId = :categoryId) " +
            "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
            "AND (:featured IS NULL OR p.isFeatured = :featured) " +
            "AND (:inStock IS NULL OR ((:inStock = true AND p.stockQuantity > 0) OR (:inStock = false AND p.stockQuantity = 0)))")
    Page<Product> findProductsWithFilters(
            @Param("categoryId") Long categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("featured") Boolean featured,
            @Param("inStock") Boolean inStock,
            Pageable pageable
    );

    // Find by price range
    Page<Product> findByPriceBetweenAndActiveTrue(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    // Find out of stock products
    Page<Product> findByStockQuantityAndActiveTrue(Integer stockQuantity, Pageable pageable);

    // Find by multiple categories - FIXED: Use explicit query
    @Query("SELECT p FROM Product p WHERE p.category.categoryId IN :categoryIds AND p.active = true")
    Page<Product> findByCategoryIdInAndActiveTrue(@Param("categoryIds") List<Long> categoryIds, Pageable pageable);

    // Count by category - FIXED: Use explicit query
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.categoryId = :categoryId AND p.active = true")
    long countByCategoryIdAndActiveTrue(@Param("categoryId") Long categoryId);

    // Find products needing reorder
    @Query("SELECT p FROM Product p WHERE p.active = true AND p.stockQuantity <= p.reorderPoint")
    List<Product> findProductsNeedingReorder();

    // Find best sellers (placeholder - would need order data)
    @Query("SELECT p FROM Product p WHERE p.active = true AND p.isFeatured = true")
    Page<Product> findBestSellers(Pageable pageable);

    // Find new arrivals - FIXED: createdDate
    @Query("SELECT p FROM Product p WHERE p.active = true ORDER BY p.createdDate DESC")
    Page<Product> findNewArrivals(Pageable pageable);

    // Find on sale products
    @Query("SELECT p FROM Product p WHERE p.active = true AND p.discountPrice IS NOT NULL AND p.discountPrice < p.price")
    Page<Product> findOnSaleProducts(Pageable pageable);

    // Search by tags
    @Query("SELECT DISTINCT p FROM Product p JOIN p.tags t WHERE p.active = true AND LOWER(t) IN :tags")
    Page<Product> findByTags(@Param("tags") List<String> tags, Pageable pageable);

    /* VERSION 2.0 - Product brand field not implemented in v1.0
    // Complex search with multiple criteria
    @Query("SELECT p FROM Product p WHERE p.active = true AND " +
            "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:sku IS NULL OR p.sku = :sku) AND " +
            "(:brand IS NULL OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :brand, '%'))) AND " +
            "(:categoryId IS NULL OR p.category.categoryId = :categoryId)")
    Page<Product> advancedSearch(
            @Param("name") String name,
            @Param("sku") String sku,
            @Param("brand") String brand,
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );
    */

    // Complex search with multiple criteria - v1.0 version without brand
    @Query("SELECT p FROM Product p WHERE p.active = true AND " +
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

    // Find products with no category
    List<Product> findByCategoryIsNullAndActiveTrue();

    // Find products by weight range
    Page<Product> findByWeightBetweenAndActiveTrue(BigDecimal minWeight, BigDecimal maxWeight, Pageable pageable);

    // Statistics queries
    @Query("SELECT AVG(p.price) FROM Product p WHERE p.a = true")
    BigDecimal findAveragePrice();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.active = true AND p.stockQuantity = 0")
    long countOutOfStockProducts();

    @Query("SELECT SUM(p.stockQuantity * p.price) FROM Product p WHERE p.active = true")
    BigDecimal calculateTotalInventoryValue();
}