package com.perfect8.shop.repository;

import com.perfect8.shop.entity.Order;
import com.perfect8.shop.entity.Customer;
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
 * Repository for Order entity
 * Version 1.0 - Core order data access
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Find order by order number
     */
    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * Find orders by customer
     */
    List<Order> findByCustomer(Customer customer);

    /**
     * Find orders by customer with pagination
     */
    Page<Order> findByCustomer(Customer customer, Pageable pageable);

    /**
     * Find orders by customer ordered by created date desc - REQUIRED METHOD
     * Used by CustomerService
     */
    Page<Order> findByCustomerOrderByCreatedAtDesc(Customer customer, Pageable pageable);

    /**
     * Find orders by customer ID
     */
    @Query("SELECT o FROM Order o WHERE o.customer.customerId = :customerId")
    List<Order> findByCustomerId(@Param("customerId") Long customerId);

    /**
     * Find orders by customer ID with pagination
     */
    @Query("SELECT o FROM Order o WHERE o.customer.customerId = :customerId")
    Page<Order> findByCustomerId(@Param("customerId") Long customerId, Pageable pageable);

    /**
     * Find orders by status
     */
    List<Order> findByOrderStatus(OrderStatus orderStatus);

    /**
     * Find orders by status with pagination
     */
    Page<Order> findByOrderStatus(OrderStatus orderStatus, Pageable pageable);

    /**
     * Find orders created between dates
     */
    List<Order> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find orders created between dates with pagination
     */
    Page<Order> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Find pending orders
     */
    List<Order> findByOrderStatusIn(List<OrderStatus> statuses);

    /**
     * Find orders needing attention
     */
    @Query("SELECT o FROM Order o WHERE o.orderStatus IN ('PENDING', 'PROCESSING') " +
            "AND o.createdAt < :cutoffDate")
    List<Order> findOrdersNeedingAttention(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find paid orders
     */
    @Query("SELECT o FROM Order o WHERE o.orderStatus IN ('PAID', 'PROCESSING', 'SHIPPED', 'DELIVERED')")
    List<Order> findPaidOrders();

    /**
     * Find orders ready to ship
     */
    @Query("SELECT o FROM Order o WHERE o.orderStatus IN ('PAID', 'PROCESSING') " +
            "AND o.requiresShipping = true")
    List<Order> findOrdersReadyToShip();

    /**
     * Find orders by email
     */
    @Query("SELECT o FROM Order o WHERE o.customer.email = :email OR o.shippingEmail = :email")
    List<Order> findByEmail(@Param("email") String email);

    /**
     * Count orders by status
     */
    Long countByOrderStatus(OrderStatus orderStatus);

    /**
     * Count orders by customer
     */
    Long countByCustomer(Customer customer);

    /**
     * Count orders by customer ID
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.customer.customerId = :customerId")
    Long countByCustomerId(@Param("customerId") Long customerId);

    /**
     * Calculate total revenue
     */
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.orderStatus IN ('PAID', 'PROCESSING', 'SHIPPED', 'DELIVERED')")
    BigDecimal calculateTotalRevenue();

    /**
     * Calculate revenue between dates
     */
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.orderStatus IN ('PAID', 'PROCESSING', 'SHIPPED', 'DELIVERED') " +
            "AND o.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal calculateRevenueBetween(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    /**
     * Find today's orders
     */
    @Query("SELECT o FROM Order o WHERE DATE(o.createdAt) = CURRENT_DATE")
    List<Order> findTodaysOrders();

    /**
     * Find recent orders
     */
    @Query("SELECT o FROM Order o ORDER BY o.createdAt DESC")
    List<Order> findRecentOrders(Pageable pageable);

    /**
     * Find orders by shipping postal code
     */
    List<Order> findByShippingPostalCode(String postalCode);

    /**
     * Find orders by shipping city
     */
    List<Order> findByShippingCity(String city);

    /**
     * Find orders by shipping state
     */
    List<Order> findByShippingState(String state);

    /**
     * Find orders by shipping country
     */
    List<Order> findByShippingCountry(String country);

    /**
     * Find abandoned carts (orders in PENDING status older than specified hours)
     */
    @Query("SELECT o FROM Order o WHERE o.orderStatus = 'PENDING' " +
            "AND o.createdAt < :cutoffTime")
    List<Order> findAbandonedCarts(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Find high value orders
     */
    @Query("SELECT o FROM Order o WHERE o.totalAmount >= :minAmount")
    List<Order> findHighValueOrders(@Param("minAmount") BigDecimal minAmount);

    /**
     * Find orders with discounts
     */
    @Query("SELECT o FROM Order o WHERE o.discountAmount > 0")
    List<Order> findOrdersWithDiscounts();

    /**
     * Find gift orders
     */
    List<Order> findByIsGiftTrue();

    /**
     * Update order status
     */
    @Query("UPDATE Order o SET o.orderStatus = :status WHERE o.orderId = :orderId")
    void updateOrderStatus(@Param("orderId") Long orderId, @Param("status") OrderStatus status);

    /**
     * Get customer's last order
     */
    @Query("SELECT o FROM Order o WHERE o.customer = :customer ORDER BY o.createdAt DESC")
    Optional<Order> findLastOrderByCustomer(@Param("customer") Customer customer, Pageable pageable);

    /**
     * Check if order number exists
     */
    boolean existsByOrderNumber(String orderNumber);

    /**
     * Find orders that need fulfillment
     */
    @Query("SELECT o FROM Order o WHERE o.orderStatus = 'PAID' " +
            "AND o.requiresShipping = true " +
            "AND o.shipment IS NULL")
    List<Order> findOrdersNeedingFulfillment();

    /**
     * Find orders shipped but not delivered
     */
    @Query("SELECT o FROM Order o WHERE o.orderStatus = 'SHIPPED' " +
            "AND o.shippedAt < :cutoffDate")
    List<Order> findInTransitOrders(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Get order statistics for a customer
     */
    @Query("SELECT COUNT(o), SUM(o.totalAmount), AVG(o.totalAmount) " +
            "FROM Order o WHERE o.customer = :customer " +
            "AND o.orderStatus IN ('PAID', 'PROCESSING', 'SHIPPED', 'DELIVERED')")
    List<Object[]> getCustomerOrderStatistics(@Param("customer") Customer customer);

    // Version 2.0 queries - commented out
    /*
    // Subscription orders
    @Query("SELECT o FROM Order o WHERE o.isSubscription = true")
    List<Order> findSubscriptionOrders();

    // Orders with coupons
    @Query("SELECT o FROM Order o WHERE o.couponCode IS NOT NULL")
    List<Order> findOrdersWithCoupons();

    // B2B orders
    @Query("SELECT o FROM Order o WHERE o.purchaseOrderNumber IS NOT NULL")
    List<Order> findB2BOrders();

    // International orders
    @Query("SELECT o FROM Order o WHERE o.shippingCountry != 'USA'")
    List<Order> findInternationalOrders();
    */
}