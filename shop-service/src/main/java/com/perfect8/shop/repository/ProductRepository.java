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
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Find by SKU
    Optional<Product> findBySku(String sku);

    // Check if SKU exists
    boolean existsBySku(String sku);

    // Find all active products
    Page<Product> findByIsActiveTrue(Pageable pageable);

    // Find by category and active
    Page<Product> findByCategoryIdAndIsActiveTrue(Long categoryId, Pageable pageable);

    // Find featured and active products
    Page<Product> findByIsFeaturedTrueAndIsActiveTrue(Pageable pageable);

    // Find low stock products
    List<Product> findByStockQuantityLessThanAndIsActiveTrue(Integer threshold);

    // Search by name or description
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND " +
            "(LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Product> searchByNameOrDescription(@Param("query") String query, Pageable pageable);

    // Find products with filters
    @Query("SELECT p FROM Product p WHERE p.isActive = true " +
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
    Page<Product> findByPriceBetweenAndIsActiveTrue(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    // Find out of stock products
    Page<Product> findByStockQuantityAndIsActiveTrue(Integer stockQuantity, Pageable pageable);

    // Find by multiple categories
    Page<Product> findByCategoryIdInAndIsActiveTrue(List<Long> categoryIds, Pageable pageable);

    // Count by category
    long countByCategoryIdAndIsActiveTrue(Long categoryId);

    // Find products needing reorder
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.stockQuantity <= p.reorderPoint")
    List<Product> findProductsNeedingReorder();

    // Find best sellers (placeholder - would need order data)
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.isFeatured = true")
    Page<Product> findBestSellers(Pageable pageable);

    // Find new arrivals
    @Query("SELECT p FROM Product p WHERE p.isActive = true ORDER BY p.createdAt DESC")
    Page<Product> findNewArrivals(Pageable pageable);

    // Find on sale products
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.discountPrice IS NOT NULL AND p.discountPrice < p.price")
    Page<Product> findOnSaleProducts(Pageable pageable);

    // Search by tags
    @Query("SELECT DISTINCT p FROM Product p JOIN p.tags t WHERE p.isActive = true AND LOWER(t) IN :tags")
    Page<Product> findByTags(@Param("tags") List<String> tags, Pageable pageable);

    // Complex search with multiple criteria
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND " +
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

    // Update stock quantity
    @Query("UPDATE Product p SET p.stockQuantity = :quantity WHERE p.productId = :productId")
    void updateStockQuantity(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    // Bulk update prices
    @Query("UPDATE Product p SET p.price = p.price * :multiplier WHERE p.category.categoryId = :categoryId")
    void bulkUpdatePricesByCategory(@Param("categoryId") Long categoryId, @Param("multiplier") BigDecimal multiplier);

    // Find products with no category
    List<Product> findByCategoryIsNullAndIsActiveTrue();

    // Find products by weight range
    Page<Product> findByWeightBetweenAndIsActiveTrue(BigDecimal minWeight, BigDecimal maxWeight, Pageable pageable);

    // Statistics queries
    @Query("SELECT AVG(p.price) FROM Product p WHERE p.isActive = true")
    BigDecimal findAveragePrice();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.isActive = true AND p.stockQuantity = 0")
    long countOutOfStockProducts();

    @Query("SELECT SUM(p.stockQuantity * p.price) FROM Product p WHERE p.isActive = true")
    BigDecimal calculateTotalInventoryValue();
}