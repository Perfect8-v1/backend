package com.perfect8.shop.repository;

import com.perfect8.shop.entity.Order;
import com.perfect8.shop.entity.Customer;
import com.perfect8.common.enums.OrderStatus;
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
 * Repository for Order entity
 * Version 1.0 - Core order management
 * NO HELPERS, NO ALIASES, NO WRAPPERS - Built right from the start!
 * Follows Magnum Opus: orderId not id, customerId not id, createdDate not createdAt
 * FIXED: Payment queries now use EXISTS with o.payments (List<Payment>) instead of o.payment
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Find order by order number
     */
    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * Find orders by customer
     * Spring Data understands the relationship - no underscore needed!
     */
    Page<Order> findByCustomer(Customer customer, Pageable pageable);

    /**
     * Find orders by customer ID with pagination
     * Alternative method using customerId directly
     */
    Page<Order> findByCustomerCustomerId(Long customerId, Pageable pageable);

    /**
     * Find orders by customer ordered by created date (DESC)
     * FIXED: Uses createdDate to match Order entity (Magnum Opus)
     */
    Page<Order> findByCustomerOrderByCreatedDateDesc(Customer customer, Pageable pageable);

    /**
     * @deprecated Use findByCustomerOrderByCreatedDateDesc instead
     * Kept for backward compatibility during refactoring
     */
    @Deprecated
    default Page<Order> findByCustomerOrderByCreatedDateDesc(Customer customer, Pageable pageable) {
        return findByCustomerOrderByCreatedDateDesc(customer, pageable);
    }

    /**
     * Find orders by status
     */
    Page<Order> findByOrderStatus(OrderStatus status, Pageable pageable);

    /**
     * Find orders by multiple statuses
     */
    Page<Order> findByOrderStatusIn(List<OrderStatus> statuses, Pageable pageable);

    /**
     * Find orders created between dates
     * FIXED: Uses createdDate to match Order entity
     */
    Page<Order> findByCreatedDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Find orders by customer and status
     */
    Page<Order> findByCustomerAndOrderStatus(Customer customer, OrderStatus status, Pageable pageable);

    /**
     * Count orders by customer
     */
    long countByCustomer(Customer customer);

    /**
     * Count orders by customer ID
     */
    long countByCustomerCustomerId(Long customerId);

    /**
     * Count orders by status
     */
    long countByOrderStatus(OrderStatus status);

    /**
     * Check if order exists by order number
     */
    boolean existsByOrderNumber(String orderNumber);

    /**
     * Find recent orders
     * FIXED: Uses createdDate to match Order entity
     */
    List<Order> findAllByOrderByCreatedDateDesc(Pageable pageable);

    /**
     * Find orders requiring attention (specific statuses)
     * For v1.0, this is a simple status check
     * FIXED: Uses createdDate in ORDER BY
     */
    @Query("SELECT o FROM Order o WHERE o.orderStatus IN :statuses ORDER BY o.createdDate DESC")
    List<Order> findOrdersRequiringAttention(@Param("statuses") List<OrderStatus> statuses, Pageable pageable);

    /**
     * Find orders with payment issues
     * FIXED: Uses EXISTS with o.payments (List) instead of o.payment
     * Checks if order has any payment with FAILED or PENDING status
     */
    @Query("SELECT o FROM Order o WHERE EXISTS (SELECT p FROM o.payments p WHERE p.paymentStatus = 'FAILED' OR p.paymentStatus = 'PENDING')")
    List<Order> findOrdersWithPaymentIssues(Pageable pageable);

    /**
     * Find unfulfilled orders
     */
    @Query("SELECT o FROM Order o WHERE o.orderStatus NOT IN ('SHIPPED', 'DELIVERED', 'CANCELLED')")
    List<Order> findUnfulfilledOrders(Pageable pageable);

    /**
     * Find orders ready to ship
     * FIXED: Uses EXISTS with o.payments (List) instead of o.payment
     * Checks if order is PROCESSING and has a COMPLETED payment
     */
    @Query("SELECT o FROM Order o WHERE o.orderStatus = 'PROCESSING' AND EXISTS (SELECT p FROM o.payments p WHERE p.paymentStatus = 'COMPLETED')")
    List<Order> findOrdersReadyToShip(Pageable pageable);

    /**
     * Get order count for customer since date
     * Uses customerId explicitly
     * FIXED: Uses createdDate to match Order entity
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.customer.customerId = :customerId AND o.createdDate >= :sinceDate")
    long countCustomerOrdersSince(@Param("customerId") Long customerId, @Param("sinceDate") LocalDateTime sinceDate);

    /**
     * Find customer's last order
     * FIXED: Uses createdDate to match Order entity
     */
    Optional<Order> findFirstByCustomerOrderByCreatedDateDesc(Customer customer);

    /**
     * Delete orders by customer (for GDPR compliance)
     */
    void deleteByCustomer(Customer customer);

    // Version 1.0 - Simple implementations
    // Note: searchOrders() is NOT a Spring Data method - must be implemented in Service layer
    // Complex search logic belongs in the service, not repository

    /**
     * Find orders by customer email
     * Simple implementation for v1.0
     * FIXED: Uses createdDate in ORDER BY
     */
    @Query("SELECT o FROM Order o WHERE o.customer.email = :email ORDER BY o.createdDate DESC")
    Page<Order> findByCustomerEmail(@Param("email") String email, Pageable pageable);

    /**
     * Find orders by shipping address postal code
     * Critical for logistics
     */
    @Query("SELECT o FROM Order o WHERE o.shippingAddress.postalCode = :postalCode")
    List<Order> findByShippingPostalCode(@Param("postalCode") String postalCode);

    /**
     * Find today's orders
     * FIXED: Uses createdDate to match Order entity
     */
    @Query("SELECT o FROM Order o WHERE DATE(o.createdDate) = CURRENT_DATE ORDER BY o.createdDate DESC")
    List<Order> findTodaysOrders();

    /**
     * Count today's orders
     * FIXED: Uses createdDate to match Order entity
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE DATE(o.createdDate) = CURRENT_DATE")
    long countTodaysOrders();

    // Version 2.0 features - commented out
    /*
    // Analytics queries for v2.0
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.createdDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateRevenueBetween(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    @Query("SELECT AVG(o.totalAmount) FROM Order o WHERE o.customer.customerId = :customerId")
    BigDecimal calculateAverageOrderValue(@Param("customerId") Long customerId);

    // Complex search with specifications - v2.0
    Page<Order> findAll(Specification<Order> spec, Pageable pageable);
    */
}
