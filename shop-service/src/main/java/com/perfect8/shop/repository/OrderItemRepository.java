package com.perfect8.shop.repository;

import com.perfect8.shop.entity.OrderItem;
import com.perfect8.shop.entity.Order;
import com.perfect8.shop.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for OrderItem entity.
 * Provides database access methods for order item operations.
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * Find all order items by order ID
     */
    List<OrderItem> findByOrderId(Long orderId);

    /**
     * Find all order items by order
     */
    List<OrderItem> findByOrder(Order order);

    /**
     * Find order items by product
     */
    List<OrderItem> findByProduct(Product product);

    /**
     * Find order items by product ID
     */
    List<OrderItem> findByProductId(Long productId);

    /**
     * Count order items by product
     */
    long countByProductId(Long productId);

    /**
     * Delete all order items for an order
     */
    void deleteByOrderId(Long orderId);

    /**
     * Check if product exists in any order
     */
    boolean existsByProductId(Long productId);

    /**
     * Find order item by order and product
     */
    Optional<OrderItem> findByOrderIdAndProductId(Long orderId, Long productId);

    /**
     * Get total quantity ordered for a product
     */
    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi WHERE oi.product.id = :productId")
    Integer getTotalQuantityOrderedForProduct(@Param("productId") Long productId);

    /**
     * Get total revenue for a product
     */
    @Query("SELECT SUM(oi.price * oi.quantity) FROM OrderItem oi WHERE oi.product.id = :productId")
    BigDecimal getTotalRevenueForProduct(@Param("productId") Long productId);

    /**
     * Find most ordered products (top sellers)
     */
    @Query("SELECT oi.product, SUM(oi.quantity) as totalQuantity " +
            "FROM OrderItem oi GROUP BY oi.product " +
            "ORDER BY totalQuantity DESC")
    List<Object[]> findMostOrderedProducts();

    /**
     * Find order items with their order and product (eager fetch)
     */
    @Query("SELECT oi FROM OrderItem oi " +
            "JOIN FETCH oi.order o " +
            "JOIN FETCH oi.product p " +
            "WHERE oi.order.id = :orderId")
    List<OrderItem> findByOrderIdWithDetails(@Param("orderId") Long orderId);

    /**
     * Get order items for orders in a specific status
     */
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.status = :status")
    List<OrderItem> findByOrderStatus(@Param("status") com.perfect8.common.enums.OrderStatus status);

    /**
     * Calculate total value of order items
     */
    @Query("SELECT SUM(oi.price * oi.quantity) FROM OrderItem oi WHERE oi.order.id = :orderId")
    BigDecimal calculateOrderTotal(@Param("orderId") Long orderId);

    /**
     * Find order items by customer
     */
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.customer.id = :customerId")
    List<OrderItem> findByCustomerId(@Param("customerId") Long customerId);

    /**
     * Get customer's purchase history for a product
     */
    @Query("SELECT oi FROM OrderItem oi " +
            "WHERE oi.order.customer.id = :customerId " +
            "AND oi.product.id = :productId " +
            "ORDER BY oi.order.createdAt DESC")
    List<OrderItem> findCustomerPurchaseHistoryForProduct(@Param("customerId") Long customerId,
                                                          @Param("productId") Long productId);

    /**
     * Find order items created between dates
     */
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.createdAt BETWEEN :startDate AND :endDate")
    List<OrderItem> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);

    /**
     * Get product sales by date range
     */
    @Query("SELECT oi.product.id, SUM(oi.quantity), SUM(oi.price * oi.quantity) " +
            "FROM OrderItem oi " +
            "WHERE oi.order.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY oi.product.id")
    List<Object[]> getProductSalesByDateRange(@Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    /**
     * Find order items with quantity greater than
     */
    @Query("SELECT oi FROM OrderItem oi WHERE oi.quantity > :quantity")
    List<OrderItem> findByQuantityGreaterThan(@Param("quantity") Integer quantity);

    /**
     * Get average order item quantity for a product
     */
    @Query("SELECT AVG(oi.quantity) FROM OrderItem oi WHERE oi.product.id = :productId")
    Double getAverageQuantityForProduct(@Param("productId") Long productId);

    /**
     * Find order items for completed orders
     */
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.status = 'DELIVERED'")
    List<OrderItem> findCompletedOrderItems();

    /**
     * Count unique customers who ordered a product
     */
    @Query("SELECT COUNT(DISTINCT oi.order.customer.id) FROM OrderItem oi WHERE oi.product.id = :productId")
    Long countUniqueCustomersForProduct(@Param("productId") Long productId);

    /**
     * Update order item price
     */
    @Query("UPDATE OrderItem oi SET oi.price = :price WHERE oi.id = :itemId")
    int updatePrice(@Param("itemId") Long itemId, @Param("price") BigDecimal price);

    /**
     * Update order item quantity
     */
    @Query("UPDATE OrderItem oi SET oi.quantity = :quantity WHERE oi.id = :itemId")
    int updateQuantity(@Param("itemId") Long itemId, @Param("quantity") Integer quantity);

    /**
     * Bulk delete order items
     */
    void deleteByIdIn(List<Long> itemIds);
}