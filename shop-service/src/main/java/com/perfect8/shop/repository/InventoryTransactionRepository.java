package com.perfect8.shop.repository;

import com.perfect8.shop.entity.InventoryTransaction;
import com.perfect8.common.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for InventoryTransaction entity
 * Provides CRUD operations and custom queries for inventory transaction management
 */
@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {

    /**
     * Find all transactions for a specific product
     */
    @Query("SELECT it FROM InventoryTransaction it WHERE it.productId = :productId ORDER BY it.transactionDate DESC")
    List<InventoryTransaction> findByProductIdOrderByTransactionDateDesc(@Param("productId") Long productId);

    /**
     * Find all transactions for a specific product with pagination
     */
    @Query("SELECT it FROM InventoryTransaction it WHERE it.productId = :productId ORDER BY it.transactionDate DESC")
    Page<InventoryTransaction> findByProductIdOrderByTransactionDateDesc(@Param("productId") Long productId, Pageable pageable);

    /**
     * Find transactions by type
     */
    @Query("SELECT it FROM InventoryTransaction it WHERE it.transactionType = :transactionType ORDER BY it.transactionDate DESC")
    List<InventoryTransaction> findByTransactionTypeOrderByTransactionDateDesc(@Param("transactionType") TransactionType transactionType);

    /**
     * Find transactions by type with pagination
     */
    @Query("SELECT it FROM InventoryTransaction it WHERE it.transactionType = :transactionType ORDER BY it.transactionDate DESC")
    Page<InventoryTransaction> findByTransactionTypeOrderByTransactionDateDesc(@Param("transactionType") TransactionType transactionType, Pageable pageable);

    /**
     * Find transactions by product and type
     */
    @Query("SELECT it FROM InventoryTransaction it WHERE it.productId = :productId AND it.transactionType = :transactionType ORDER BY it.transactionDate DESC")
    List<InventoryTransaction> findByProductIdAndTransactionTypeOrderByTransactionDateDesc(
            @Param("productId") Long productId,
            @Param("transactionType") TransactionType transactionType);

    /**
     * Find transactions within date range
     */
    @Query("SELECT it FROM InventoryTransaction it WHERE it.transactionDate BETWEEN :startDate AND :endDate ORDER BY it.transactionDate DESC")
    List<InventoryTransaction> findByTransactionDateBetweenOrderByTransactionDateDesc(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find transactions within date range with pagination
     */
    @Query("SELECT it FROM InventoryTransaction it WHERE it.transactionDate BETWEEN :startDate AND :endDate ORDER BY it.transactionDate DESC")
    Page<InventoryTransaction> findByTransactionDateBetweenOrderByTransactionDateDesc(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Find transactions by product within date range
     */
    @Query("SELECT it FROM InventoryTransaction it WHERE it.productId = :productId AND it.transactionDate BETWEEN :startDate AND :endDate ORDER BY it.transactionDate DESC")
    List<InventoryTransaction> findByProductIdAndTransactionDateBetweenOrderByTransactionDateDesc(
            @Param("productId") Long productId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find latest transaction for a product
     */
    @Query("SELECT it FROM InventoryTransaction it WHERE it.productId = :productId ORDER BY it.transactionDate DESC")
    Optional<InventoryTransaction> findLatestByProductId(@Param("productId") Long productId);

    /**
     * Calculate total quantity change for a product
     */
    @Query("SELECT COALESCE(SUM(it.quantityChange), 0) FROM InventoryTransaction it WHERE it.productId = :productId")
    Integer calculateTotalQuantityChangeByProductId(@Param("productId") Long productId);

    /**
     * Calculate quantity change for a product within date range
     */
    @Query("SELECT COALESCE(SUM(it.quantityChange), 0) FROM InventoryTransaction it WHERE it.productId = :productId AND it.transactionDate BETWEEN :startDate AND :endDate")
    Integer calculateQuantityChangeByProductIdAndDateRange(
            @Param("productId") Long productId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Calculate quantity change by transaction type for a product
     */
    @Query("SELECT COALESCE(SUM(it.quantityChange), 0) FROM InventoryTransaction it WHERE it.productId = :productId AND it.transactionType = :transactionType")
    Integer calculateQuantityChangeByProductIdAndTransactionType(
            @Param("productId") Long productId,
            @Param("transactionType") TransactionType transactionType);

    /**
     * Find transactions by reference (e.g., order ID, restock batch)
     */
    @Query("SELECT it FROM InventoryTransaction it WHERE it.reference = :reference ORDER BY it.transactionDate DESC")
    List<InventoryTransaction> findByReferenceOrderByTransactionDateDesc(@Param("reference") String reference);

    /**
     * Find recent transactions (last N days)
     */
    @Query("SELECT it FROM InventoryTransaction it WHERE it.transactionDate >= :sinceDate ORDER BY it.transactionDate DESC")
    List<InventoryTransaction> findRecentTransactions(@Param("sinceDate") LocalDateTime sinceDate);

    /**
     * Find recent transactions with pagination
     */
    @Query("SELECT it FROM InventoryTransaction it WHERE it.transactionDate >= :sinceDate ORDER BY it.transactionDate DESC")
    Page<InventoryTransaction> findRecentTransactions(@Param("sinceDate") LocalDateTime sinceDate, Pageable pageable);

    /**
     * Count transactions by product
     */
    @Query("SELECT COUNT(it) FROM InventoryTransaction it WHERE it.productId = :productId")
    Long countByProductId(@Param("productId") Long productId);

    /**
     * Count transactions by type
     */
    @Query("SELECT COUNT(it) FROM InventoryTransaction it WHERE it.transactionType = :transactionType")
    Long countByTransactionType(@Param("transactionType") TransactionType transactionType);

    /**
     * Find products with low stock movements (for analysis)
     */
    @Query("SELECT it.productId, COUNT(it) as transactionCount FROM InventoryTransaction it WHERE it.transactionDate >= :sinceDate GROUP BY it.productId HAVING COUNT(it) < :minTransactions ORDER BY transactionCount ASC")
    List<Object[]> findProductsWithLowStockMovements(
            @Param("sinceDate") LocalDateTime sinceDate,
            @Param("minTransactions") Long minTransactions);

    /**
     * Find products with high stock movements (for analysis)
     */
    @Query("SELECT it.productId, COUNT(it) as transactionCount, SUM(ABS(it.quantityChange)) as totalQuantityMoved FROM InventoryTransaction it WHERE it.transactionDate >= :sinceDate GROUP BY it.productId ORDER BY totalQuantityMoved DESC")
    List<Object[]> findProductsWithHighStockMovements(@Param("sinceDate") LocalDateTime sinceDate, Pageable pageable);

    /**
     * Delete old transactions (for cleanup)
     */
    @Query("DELETE FROM InventoryTransaction it WHERE it.transactionDate < :beforeDate")
    void deleteTransactionsOlderThan(@Param("beforeDate") LocalDateTime beforeDate);

    /**
     * Find transactions with notes containing specific text
     */
    @Query("SELECT it FROM InventoryTransaction it WHERE LOWER(it.notes) LIKE LOWER(CONCAT('%', :searchText, '%')) ORDER BY it.transactionDate DESC")
    List<InventoryTransaction> findByNotesContainingIgnoreCaseOrderByTransactionDateDesc(@Param("searchText") String searchText);

    /**
     * Get stock level at specific point in time
     */
    @Query("SELECT COALESCE(SUM(it.quantityChange), 0) FROM InventoryTransaction it WHERE it.productId = :productId AND it.transactionDate <= :atDate")
    Integer getStockLevelAtDate(@Param("productId") Long productId, @Param("atDate") LocalDateTime atDate);

    /**
     * Find transactions that resulted in negative stock
     */
    @Query("SELECT it FROM InventoryTransaction it WHERE it.quantityAfterTransaction < 0 ORDER BY it.transactionDate DESC")
    List<InventoryTransaction> findTransactionsWithNegativeStock();

    /**
     * Find transactions that resulted in negative stock for specific product
     */
    @Query("SELECT it FROM InventoryTransaction it WHERE it.productId = :productId AND it.quantityAfterTransaction < 0 ORDER BY it.transactionDate DESC")
    List<InventoryTransaction> findTransactionsWithNegativeStockByProductId(@Param("productId") Long productId);

    /**
     * Find duplicate transactions (same product, quantity, and time within margin)
     */
    @Query("SELECT it FROM InventoryTransaction it WHERE EXISTS (SELECT it2 FROM InventoryTransaction it2 WHERE it2.id != it.id AND it2.productId = it.productId AND it2.quantityChange = it.quantityChange AND ABS(TIMESTAMPDIFF(MINUTE, it2.transactionDate, it.transactionDate)) <= :minutesMargin) ORDER BY it.transactionDate DESC")
    List<InventoryTransaction> findPotentialDuplicateTransactions(@Param("minutesMargin") Integer minutesMargin);
}