package com.perfect8.admin.repository;

// import com.perfect8.shop.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * VERSION 2.0 - COMMENTED OUT FOR PHASE 1
 *
 * Product repository for admin operations
 * Will be enabled in version 2.0 when product management is added to admin-service
 *
 * For now, product management remains in shop-service only
 */
@Repository
public interface ProductRepository { // extends JpaRepository<Product, Long> {

    /* VERSION 2.0 - PRODUCT MANAGEMENT

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    List<Product> findByCategoryId(Long categoryId);

    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    List<Product> findByStockQuantityLessThan(Integer threshold);

    List<Product> findByActiveTrue();

    List<Product> findByActiveFalse();

    List<Product> findByFeaturedTrue();

    List<Product> findByCreatedAtAfter(LocalDateTime date);

    List<Product> findTop10ByOrderByCreatedAtDesc();

    @Query("SELECT p FROM Product p WHERE p.stockQuantity < :threshold AND p.active = true")
    List<Product> findLowStockProducts(@Param("threshold") Integer threshold);

    @Query("SELECT p FROM Product p WHERE p.discountPercentage > 0 AND p.active = true")
    List<Product> findDiscountedProducts();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.active = true")
    long countActiveProducts();

    @Query("SELECT AVG(p.price) FROM Product p WHERE p.categoryId = :categoryId")
    BigDecimal getAveragePriceByCategory(@Param("categoryId") Long categoryId);

    @Query("SELECT p FROM Product p WHERE p.lastModified > :date ORDER BY p.lastModified DESC")
    List<Product> findRecentlyModifiedProducts(@Param("date") LocalDateTime date);

    */

    // Temporary placeholder method for compilation
    default String getVersion() {
        return "2.0 - Product management disabled in v1.0";
    }
}