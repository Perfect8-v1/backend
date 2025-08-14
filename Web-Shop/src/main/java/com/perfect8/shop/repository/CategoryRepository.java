package com.perfect8.shop.repository;

import com.perfect8.shop.model.Order;
import com.perfect8.shop.model.OrderStatus;
import com.perfect8.shop.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Order entity
 *
 * Swedish engineering precision for order management like SAAB 32 Lansen!
 * Provides comprehensive order tracking from creation to delivery.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // ============================================================================
    // BASIC FINDER METHODS (Grundläggande sökning)
    // ============================================================================

    /**
     * Find order by order number (unique identifier)
     */
    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * Find orders by customer
     */
    Page<Order> findByCustomerOrderByCreatedAtDesc(Customer customer, Pageable pageable);

    /**
     * Find orders by customer email
     */
    @Query("SELECT o FROM Order o WHERE o.customer.email = :email ORDER BY o.createdAt DESC")
    Page<Order> findByCustomerEmail(@Param("email") String email, Pageable pageable);

    /**
     * Find orders by status
     */
    Page<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);

    /**
     * Find recent orders (last 24 hours)
     */
    @Query("SELECT o FROM Order o WHERE o.createdAt >= :since ORDER BY o.createdAt DESC")
    List<Order> findRecentOrders(@Param("since") LocalDateTime since);

    // ============================================================================
    // STATUS MANAGEMENT (Som Lansen's mission status)
    // ============================================================================

    /**
     * Find orders by multiple statuses
     */
    @Query("SELECT o FROM Order o WHERE o.status IN :statuses ORDER BY o.createdAt DESC")
    Page<Order> findByStatusIn(@Param("statuses") List<OrderStatus> statuses, Pageable pageable);

    /**
     * Find orders requiring attention (pending, failed payments)
     */
    @Query("SELECT o FROM Order o WHERE o.status IN ('PENDING', 'CONFIRMED') " +
            "OR (o.payment IS NOT NULL AND o.payment.status = 'FAILED') " +
            "ORDER BY o.createdAt ASC")
    List<Order> findOrdersRequiringAttention();

    /**
     * Count orders by status
     */
    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    List<Object[]> countOrdersByStatus();

    /**
     * Find orders stuck in status (longer than expected)
     */
    @Query("SELECT o FROM Order o WHERE " +
            "(o.status = 'PENDING' AND o.createdAt < :pendingCutoff) OR " +
            "(o.status = 'CONFIRMED' AND o.confirmedAt < :confirmedCutoff) OR " +
            "(o.status = 'SHIPPED' AND o.shippedAt < :shippedCutoff)")
    List<Order> findStuckOrders(
            @Param("pendingCutoff") LocalDateTime pendingCutoff,
            @Param("confirmedCutoff") LocalDateTime confirmedCutoff,
            @Param("shippedCutoff") LocalDateTime shippedCutoff
    );

    // ============================================================================
    // ADVANCED FILTERING (Precision targeting som Lansen)
    // ============================================================================

    /**
     * Advanced order search with multiple criteria
     */
    @Query("SELECT o FROM Order o WHERE " +
            "(:customerEmail IS NULL OR o.customer.email LIKE %:customerEmail%) AND " +
            "(:status IS NULL OR o.status = :status) AND " +
            "(:startDate IS NULL OR o.createdAt >= :startDate) AND " +
            "(:endDate IS NULL OR o.createdAt <= :endDate) AND " +
            "(:minAmount IS NULL OR o.orderTotal >= :minAmount) AND " +
            "(:maxAmount IS NULL OR o.orderTotal <= :maxAmount) " +
            "ORDER BY o.createdAt DESC")
    Page<Order> findOrdersWithFilters(
            @Param("customerEmail") String customerEmail,
            @Param("status") OrderStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            Pageable pageable
    );

    /**
     * Find orders by date range
     */
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY o.createdAt DESC")
    List<Order> findOrdersByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find high-value orders
     */
    @Query("SELECT o FROM Order o WHERE o.orderTotal >= :minAmount ORDER BY o.orderTotal DESC")
    List<Order> findHighValueOrders(@Param("minAmount") BigDecimal minAmount);

    // ============================================================================
    // ANALYTICS & METRICS (Radar sweep för business intelligence)
    // ============================================================================

    /**
     * Get daily order metrics
     */
    @Query("SELECT DATE(o.createdAt), COUNT(o), SUM(o.orderTotal), AVG(o.orderTotal) " +
            "FROM Order o WHERE o.createdAt >= :startDate " +
            "GROUP BY DATE(o.createdAt) ORDER BY DATE(o.createdAt)")
    List<Object[]> getDailyOrderMetrics(@Param("startDate") LocalDateTime startDate);

    /**
     * Get monthly revenue
     */
    @Query("SELECT YEAR(o.createdAt), MONTH(o.createdAt), SUM(o.orderTotal) " +
            "FROM Order o WHERE o.status IN ('DELIVERED', 'SHIPPED') " +
            "GROUP BY YEAR(o.createdAt), MONTH(o.createdAt) " +
            "ORDER BY YEAR(o.createdAt), MONTH(o.createdAt)")
    List<Object[]> getMonthlyRevenue();

    /**
     * Get top customers by order value
     */
    @Query("SELECT o.customer, SUM(o.orderTotal), COUNT(o) " +
            "FROM Order o WHERE o.status = 'DELIVERED' " +
            "GROUP BY o.customer ORDER BY SUM(o.orderTotal) DESC")
    List<Object[]> getTopCustomersByValue(Pageable pageable);

    /**
     * Get average order value
     */
    @Query("SELECT AVG(o.orderTotal) FROM Order o WHERE o.status IN ('DELIVERED', 'SHIPPED')")
    BigDecimal getAverageOrderValue();

    /**
     * Get conversion funnel metrics
     */
    @Query("SELECT " +
            "SUM(CASE WHEN o.status = 'PENDING' THEN 1 ELSE 0 END) as pending, " +
            "SUM(CASE WHEN o.status = 'CONFIRMED' THEN 1 ELSE 0 END) as confirmed, " +
            "SUM(CASE WHEN o.status = 'SHIPPED' THEN 1 ELSE 0 END) as shipped, " +
            "SUM(CASE WHEN o.status = 'DELIVERED' THEN 1 ELSE 0 END) as delivered, " +
            "SUM(CASE WHEN o.status = 'CANCELLED' THEN 1 ELSE 0 END) as cancelled " +
            "FROM Order o WHERE o.createdAt >= :startDate")
    Object[] getOrderFunnelMetrics(@Param("startDate") LocalDateTime startDate);

    // ============================================================================
    // PRODUCT ANALYTICS (Som Lansen's target analysis)
    // ============================================================================

    /**
     * Get best selling products
     */
    @Query("SELECT oi.product, SUM(oi.quantity), SUM(oi.totalPrice), COUNT(DISTINCT o) " +
            "FROM Order o JOIN o.orderItems oi " +
            "WHERE o.status IN ('DELIVERED', 'SHIPPED') AND o.createdAt >= :startDate " +
            "GROUP BY oi.product ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> getBestSellingProducts(@Param("startDate") LocalDateTime startDate, Pageable pageable);

    /**
     * Get product performance by revenue
     */
    @Query("SELECT oi.product, SUM(oi.totalPrice), COUNT(DISTINCT o), AVG(oi.unitPrice) " +
            "FROM Order o JOIN o.orderItems oi " +
            "WHERE o.status IN ('DELIVERED', 'SHIPPED') AND o.createdAt >= :startDate " +
            "GROUP BY oi.product ORDER BY SUM(oi.totalPrice) DESC")
    List<Object[]> getProductsByRevenue(@Param("startDate") LocalDateTime startDate, Pageable pageable);

    /**
     * Find orders containing specific product
     */
    @Query("SELECT o FROM Order o JOIN o.orderItems oi WHERE oi.product.id = :productId")
    List<Order> findOrdersWithProduct(@Param("productId") Long productId);

    // ============================================================================
    // CUSTOMER ANALYTICS (Customer behavior radar)
    // ============================================================================

    /**
     * Get customer order frequency
     */
    @Query("SELECT o.customer, COUNT(o), AVG(o.orderTotal), " +
            "MIN(o.createdAt), MAX(o.createdAt) " +
            "FROM Order o WHERE o.status = 'DELIVERED' " +
            "GROUP BY o.customer HAVING COUNT(o) > 1 " +
            "ORDER BY COUNT(o) DESC")
    List<Object[]> getCustomerOrderFrequency();

    /**
     * Find first-time customers
     */
    @Query("SELECT o FROM Order o WHERE " +
            "o.id = (SELECT MIN(o2.id) FROM Order o2 WHERE o2.customer = o.customer) " +
            "AND o.createdAt >= :startDate")
    List<Order> findFirstTimeCustomers(@Param("startDate") LocalDateTime startDate);

    /**
     * Find returning customers
     */
    @Query("SELECT DISTINCT o.customer FROM Order o WHERE " +
            "o.customer.id IN (SELECT o2.customer.id FROM Order o2 " +
            "GROUP BY o2.customer.id HAVING COUNT(o2) > 1)")
    List<Customer> findReturningCustomers();

    // ============================================================================
    // OPERATIONAL QUERIES (Mission critical operations)
    // ============================================================================

    /**
     * Find orders ready to ship
     */
    @Query("SELECT o FROM Order o WHERE o.status = 'CONFIRMED' AND " +
            "o.payment IS NOT NULL AND o.payment.status = 'COMPLETED'")
    List<Order> findOrdersReadyToShip();

    /**
     * Find overdue deliveries
     */
    @Query("SELECT o FROM Order o WHERE o.status = 'SHIPPED' AND " +
            "o.shipment IS NOT NULL AND o.shipment.estimatedDeliveryDate < CURRENT_DATE")
    List<Order> findOverdueDeliveries();

    /**
     * Find orders with payment issues
     */
    @Query("SELECT o FROM Order o WHERE " +
            "(o.payment IS NULL AND o.status != 'PENDING') OR " +
            "(o.payment IS NOT NULL AND o.payment.status IN ('FAILED', 'CANCELLED'))")
    List<Order> findOrdersWithPaymentIssues();

    /**
     * Update order status
     */
    @Modifying
    @Query("UPDATE Order o SET o.status = :status WHERE o.id = :orderId")
    int updateOrderStatus(@Param("orderId") Long orderId, @Param("status") OrderStatus status);

    // ============================================================================
    // PERFORMANCE OPTIMIZED QUERIES (Afterburner speed)
    // ============================================================================

    /**
     * Find order with all related data (fetch join)
     */
    @Query("SELECT o FROM Order o " +
            "LEFT JOIN FETCH o.customer " +
            "LEFT JOIN FETCH o.orderItems oi " +
            "LEFT JOIN FETCH oi.product " +
            "LEFT JOIN FETCH o.payment " +
            "LEFT JOIN FETCH o.shipment " +
            "WHERE o.id = :orderId")
    Optional<Order> findByIdWithAllData(@Param("orderId") Long orderId);

    /**
     * Find orders with customer data
     */
    @Query("SELECT o FROM Order o JOIN FETCH o.customer WHERE o.id IN :orderIds")
    List<Order> findOrdersWithCustomer(@Param("orderIds") List<Long> orderIds);

    /**
     * Find orders by tracking number
     */
    @Query("SELECT o FROM Order o JOIN o.shipment s WHERE s.trackingNumber = :trackingNumber")
    Optional<Order> findByTrackingNumber(@Param("trackingNumber") String trackingNumber);

    // ============================================================================
    // REPORTING QUERIES (Intel rapporter)
    // ============================================================================

    /**
     * Executive summary data
     */
    @Query("SELECT " +
            "COUNT(o) as totalOrders, " +
            "SUM(o.orderTotal) as totalRevenue, " +
            "AVG(o.orderTotal) as avgOrderValue, " +
            "COUNT(DISTINCT o.customer) as uniqueCustomers " +
            "FROM Order o WHERE o.createdAt >= :startDate AND o.status != 'CANCELLED'")
    Object[] getExecutiveSummary(@Param("startDate") LocalDateTime startDate);

    /**
     * Geographic distribution of orders
     */
    @Query("SELECT o.shippingCountry, COUNT(o), SUM(o.orderTotal) " +
            "FROM Order o WHERE o.status != 'CANCELLED' " +
            "GROUP BY o.shippingCountry ORDER BY COUNT(o) DESC")
    List<Object[]> getOrdersByCountry();

    /**
     * Hourly order distribution (peak times)
     */
    @Query("SELECT HOUR(o.createdAt), COUNT(o) " +
            "FROM Order o WHERE o.createdAt >= :startDate " +
            "GROUP BY HOUR(o.createdAt) ORDER BY HOUR(o.createdAt)")
    List<Object[]> getHourlyOrderDistribution(@Param("startDate") LocalDateTime startDate);

    /**
     * Find abandoned checkouts (pending orders older than threshold)
     */
    @Query("SELECT o FROM Order o WHERE o.status = 'PENDING' AND " +
            "o.createdAt < :cutoffTime ORDER BY o.createdAt ASC")
    List<Order> findAbandonedCheckouts(@Param("cutoffTime") LocalDateTime cutoffTime);
}