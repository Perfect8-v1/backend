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
 * FIXED: All queries use explicit field names (orderId, productId, orderStatus, createdDate)
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * Find all order items by order ID - FIXED: Explicit query
     */
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.orderId = :orderId")
    List<OrderItem> findByOrderId(@Param("orderId") Long orderId);

    /**
     * Find all order items by order
     */
    List<OrderItem> findByOrder(Order order);

    /**
     * Find order items by product
     */
    List<OrderItem> findByProduct(Product product);

    /**
     * Find order items by product ID - FIXED: Explicit query
     */
    @Query("SELECT oi FROM OrderItem oi WHERE oi.product.productId = :productId")
    List<OrderItem> findByProductId(@Param("productId") Long productId);

    /**
     * Count order items by product - FIXED: Explicit query
     */
    @Query("SELECT COUNT(oi) FROM OrderItem oi WHERE oi.product.productId = :productId")
    long countByProductId(@Param("productId") Long productId);

    /**
     * Delete all order items for an order - FIXED: Explicit query
     */
    @Query("DELETE FROM OrderItem oi WHERE oi.order.orderId = :orderId")
    void deleteByOrderId(@Param("orderId") Long orderId);

    /**
     * Check if product exists in any order - FIXED: Explicit query
     */
    @Query("SELECT CASE WHEN COUNT(oi) > 0 THEN true ELSE false END FROM OrderItem oi WHERE oi.product.productId = :productId")
    boolean existsByProductId(@Param("productId") Long productId);

    /**
     * Find order item by order and product - FIXED: Explicit query
     */
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.orderId = :orderId AND oi.product.productId = :productId")
    Optional<OrderItem> findByOrderIdAndProductId(@Param("orderId") Long orderId, @Param("productId") Long productId);

    /**
     * Get total quantity ordered for a product
     */
    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi WHERE oi.product.productId = :productId")
    Integer getTotalQuantityOrderedForProduct(@Param("productId") Long productId);

    /**
     * Get total revenue for a product
     */
    @Query("SELECT SUM(oi.price * oi.quantity) FROM OrderItem oi WHERE oi.product.productId = :productId")
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
            "WHERE oi.order.orderId = :orderId")
    List<OrderItem> findByOrderIdWithDetails(@Param("orderId") Long orderId);

    /**
     * Get order items for orders in a specific status - FIXED: orderStatus
     */
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.orderStatus = :status")
    List<OrderItem> findByOrderStatus(@Param("status") com.perfect8.common.enums.OrderStatus status);

    /**
     * Calculate total value of order items
     */
    @Query("SELECT SUM(oi.price * oi.quantity) FROM OrderItem oi WHERE oi.order.orderId = :orderId")
    BigDecimal calculateOrderTotal(@Param("orderId") Long orderId);

    /**
     * Find order items by customer
     */
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.customer.customerId = :customerId")
    List<OrderItem> findByCustomerId(@Param("customerId") Long customerId);

    /**
     * Get customer's purchase history for a product - FIXED: createdDate
     */
    @Query("SELECT oi FROM OrderItem oi " +
            "WHERE oi.order.customer.customerId = :customerId " +
            "AND oi.product.productId = :productId " +
            "ORDER BY oi.order.createdDate DESC")
    List<OrderItem> findCustomerPurchaseHistoryForProduct(@Param("customerId") Long customerId,
                                                          @Param("productId") Long productId);

    /**
     * Find order items created between dates - FIXED: createdDate
     */
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.createdDate BETWEEN :startDate AND :endDate")
    List<OrderItem> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);

    /**
     * Get product sales by date range - FIXED: createdDate
     */
    @Query("SELECT oi.product.productId, SUM(oi.quantity), SUM(oi.price * oi.quantity) " +
            "FROM OrderItem oi " +
            "WHERE oi.order.createdDate BETWEEN :startDate AND :endDate " +
            "GROUP BY oi.product.productId")
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
    @Query("SELECT AVG(oi.quantity) FROM OrderItem oi WHERE oi.product.productId = :productId")
    Double getAverageQuantityForProduct(@Param("productId") Long productId);

    /**
     * Find order items for completed orders - FIXED: orderStatus = DELIVERED
     */
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.orderStatus = 'DELIVERED'")
    List<OrderItem> findCompletedOrderItems();

    /**
     * Count unique customers who ordered a product
     */
    @Query("SELECT COUNT(DISTINCT oi.order.customer.customerId) FROM OrderItem oi WHERE oi.product.productId = :productId")
    Long countUniqueCustomersForProduct(@Param("productId") Long productId);

    /**
     * Update order item price
     */
    @Query("UPDATE OrderItem oi SET oi.price = :price WHERE oi.orderItemId = :orderItemId")
    int updatePrice(@Param("orderItemId") Long orderItemId, @Param("price") BigDecimal price);

    /**
     * Update order item quantity
     */
    @Query("UPDATE OrderItem oi SET oi.quantity = :quantity WHERE oi.orderItemId = :orderItemId")
    int updateQuantity(@Param("orderItemId") Long orderItemId, @Param("quantity") Integer quantity);

    /**
     * Bulk delete order items
     */
    void deleteByOrderItemIdIn(List<Long> orderItemIds);
}
