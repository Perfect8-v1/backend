package com.perfect8.shop.repository;

import com.perfect8.shop.entity.Order;
import com.perfect8.shop.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Order entity.
 * Version 1.0 - Core functionality only
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Find order by order number
     */
    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * Find orders by customer ID
     */
    Page<Order> findByCustomer_CustomerId(Long customerId, Pageable pageable);

    /**
     * Find orders by customer ID ordered by creation date
     */
    List<Order> findByCustomer_CustomerIdOrderByCreatedAtDesc(Long customerId);

    /**
     * Find orders by status
     */
    List<Order> findByOrderStatus(OrderStatus orderStatus);

    /**
     * Find orders by status with pagination
     */
    Page<Order> findByOrderStatus(OrderStatus orderStatus, Pageable pageable);

    /**
     * Find orders by multiple statuses
     */
    List<Order> findByOrderStatusIn(List<OrderStatus> statuses);

    /**
     * Find recent orders (limit by count)
     */
    List<Order> findTopNByOrderByCreatedAtDesc(int limit);

    /**
     * Find orders created between dates
     */
    List<Order> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find orders created between dates with pagination
     */
    Page<Order> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Count orders by status
     */
    long countByOrderStatus(OrderStatus orderStatus);

    /**
     * Count orders for a customer
     */
    long countByCustomer_CustomerId(Long customerId);

    /**
     * Check if order exists by order number
     */
    boolean existsByOrderNumber(String orderNumber);

    /**
     * Search orders by order number, customer email, or customer name
     */
    @Query("SELECT o FROM Order o WHERE " +
            "LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(o.shippingEmail) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(CONCAT(o.shippingFirstName, ' ', o.shippingLastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Order> searchOrders(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find orders with total amount greater than specified value
     */
    List<Order> findByTotalAmountGreaterThan(BigDecimal amount);

    /**
     * Find orders by customer email
     */
    List<Order> findByShippingEmail(String email);

    /**
     * Find pending orders older than specified hours
     */
    @Query("SELECT o FROM Order o WHERE o.orderStatus = 'PENDING' " +
            "AND o.createdAt < :cutoffTime")
    List<Order> findPendingOrdersOlderThan(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Find orders that need shipping (confirmed but not shipped)
     */
    @Query("SELECT o FROM Order o WHERE o.orderStatus IN ('CONFIRMED', 'PROCESSING') " +
            "ORDER BY o.confirmedAt ASC")
    List<Order> findOrdersNeedingShipment();

    /**
     * Find orders shipped but not delivered within days
     */
    @Query("SELECT o FROM Order o WHERE o.orderStatus = 'SHIPPED' " +
            "AND o.shippedAt < :cutoffTime")
    List<Order> findShippedOrdersOlderThan(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Get order statistics for a date range
     */
    @Query("SELECT COUNT(o), SUM(o.totalAmount), AVG(o.totalAmount) " +
            "FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate " +
            "AND o.orderStatus NOT IN ('CANCELLED', 'RETURNED')")
    List<Object[]> getOrderStatistics(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);

    /**
     * Find orders with specific shipping country
     */
    List<Order> findByShippingCountry(String countryCode);

    /**
     * Find orders with specific shipping state
     */
    List<Order> findByShippingStateAndShippingCountry(String state, String countryCode);

    /**
     * Find delivered orders for return eligibility check (within 30 days)
     */
    @Query("SELECT o FROM Order o WHERE o.orderStatus = 'DELIVERED' " +
            "AND o.deliveredAt > :cutoffDate " +
            "AND o.customer.customerId = :customerId")
    List<Order> findReturnableOrders(@Param("customerId") Long customerId,
                                     @Param("cutoffDate") LocalDateTime cutoffDate);

    // Version 2.0 - Commented out for future implementation
    // Analytics and metrics queries
    /*
    @Query("SELECT DATE(o.createdAt) as orderDate, COUNT(o) as orderCount, " +
           "SUM(o.totalAmount) as totalRevenue " +
           "FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(o.createdAt)")
    List<Object[]> getDailyOrderMetrics(@Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    @Query("SELECT o.shippingCountry, COUNT(o) as orderCount, " +
           "SUM(o.totalAmount) as totalRevenue " +
           "FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY o.shippingCountry")
    List<Object[]> getOrdersByCountry(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);
    */
}