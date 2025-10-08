package com.perfect8.shop.repository;

import com.perfect8.shop.entity.Payment;
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
 * Repository for Payment entity - Version 1.0
 * Focuses on core payment operations essential for e-commerce functionality
 * Follows Magnum Opus: paymentId not id, orderId not id, customerId not id
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /* ============================================
     * CORE PAYMENT OPERATIONS - Version 1.0
     * Essential for basic e-commerce functionality
     * ============================================ */

    // Find by transaction ID - Essential for payment tracking
    Optional<Payment> findByTransactionId(String transactionId);

    // Find by order - Essential for order management
    List<Payment> findByOrderId(Long orderId);

    // Find most recent payment for order - Essential for payment status
    Optional<Payment> findFirstByOrderIdOrderByCreatedAtDesc(Long orderId);

    // Find successful payment for order - Essential for order completion
    Optional<Payment> findFirstByOrderIdAndStatus(Long orderId, String status);

    // Find by status - Essential for payment management
    List<Payment> findByStatus(String status);
    Page<Payment> findByStatus(String status, Pageable pageable);

    // Find by payment method - Essential for PayPal integration
    List<Payment> findByPaymentMethod(String paymentMethod);

    // Find pending payments older than specified time - Essential for timeout handling
    @Query("SELECT p FROM Payment p WHERE p.status = 'PENDING' AND p.createdAt < :cutoffTime")
    List<Payment> findPendingPaymentsOlderThan(@Param("cutoffTime") LocalDateTime cutoffTime);

    // Find failed payments for retry - Essential for payment recovery
    @Query("SELECT p FROM Payment p WHERE p.status = 'FAILED' AND p.retryCount < :maxRetries")
    List<Payment> findFailedPaymentsForRetry(@Param("maxRetries") Integer maxRetries);

    // Find by reference number - Essential for payment lookup
    Optional<Payment> findByReferenceNumber(String referenceNumber);

    // Find by gateway payment ID - Essential for PayPal integration
    Optional<Payment> findByGatewayPaymentId(String gatewayPaymentId);

    // Find refunded payments - Essential for refund management
    @Query("SELECT p FROM Payment p WHERE p.refundAmount > 0")
    List<Payment> findRefundedPayments();

    // Find by payer email - Essential for customer service
    List<Payment> findByPayerEmail(String payerEmail);

    // Check if order has successful payment - Essential for order validation
    @Query("SELECT COUNT(p) > 0 FROM Payment p WHERE p.order.orderId = :orderId AND p.status = 'COMPLETED'")
    boolean hasSuccessfulPayment(@Param("orderId") Long orderId);

    // Find duplicate payments - Essential for preventing double charges
    @Query("SELECT p FROM Payment p WHERE p.order.orderId = :orderId " +
            "AND p.amount = :amount " +
            "AND p.status = 'COMPLETED' " +
            "AND p.createdAt BETWEEN :startTime AND :endTime")
    List<Payment> findPotentialDuplicates(
            @Param("orderId") Long orderId,
            @Param("amount") BigDecimal amount,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    // Find payments needing verification - Essential for payment security
    @Query("SELECT p FROM Payment p WHERE p.status = 'COMPLETED' AND p.isVerified = false")
    List<Payment> findPaymentsNeedingVerification();

    // Check if transaction ID exists - Essential for preventing duplicates
    boolean existsByTransactionId(String transactionId);

    // Find payments with high retry count - Essential for identifying problem payments
    @Query("SELECT p FROM Payment p WHERE p.retryCount >= :minRetries ORDER BY p.retryCount DESC")
    List<Payment> findHighRetryPayments(@Param("minRetries") Integer minRetries);

    // Find by multiple statuses - Essential for bulk status checks
    List<Payment> findByStatusIn(List<String> statuses);

    // Find payments by customer - Essential for customer service
    @Query("SELECT p FROM Payment p WHERE p.order.customer.customerId = :customerId ORDER BY p.createdAt DESC")
    List<Payment> findByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT p FROM Payment p WHERE p.order.customer.customerId = :customerId")
    Page<Payment> findByCustomerId(@Param("customerId") Long customerId, Pageable pageable);

    /* ============================================
     * VERSION 2.0 METHODS - Reserved for Analytics & Reporting
     * ============================================
     * These methods will be uncommented in version 2.0:
     *
     * // Analytics and Reporting Methods
     * Page<Payment> findByPaymentMethod(String paymentMethod, Pageable pageable);
     *
     * // Date range queries for reporting
     * List<Payment> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
     * Page<Payment> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
     *
     * // Refund analytics
     * Page<Payment> findRefundedPayments(Pageable pageable);
     *
     * // Revenue calculations
     * @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = :status")
     * BigDecimal calculateTotalAmountByStatus(@Param("status") String status);
     *
     * @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = :status AND p.createdAt BETWEEN :startDate AND :endDate")
     * BigDecimal calculateTotalAmountByStatusAndDateRange(
     *         @Param("status") String status,
     *         @Param("startDate") LocalDateTime startDate,
     *         @Param("endDate") LocalDateTime endDate);
     *
     * @Query("SELECT SUM(p.refundAmount) FROM Payment p WHERE p.refundAmount > 0")
     * BigDecimal calculateTotalRefundAmount();
     *
     * @Query("SELECT SUM(p.refundAmount) FROM Payment p WHERE p.refundAmount > 0 AND p.refundDate BETWEEN :startDate AND :endDate")
     * BigDecimal calculateTotalRefundAmountByDateRange(
     *         @Param("startDate") LocalDateTime startDate,
     *         @Param("endDate") LocalDateTime endDate);
     *
     * // Statistics and metrics
     * Long countByStatus(String status);
     * Long countByStatusAndCreatedAtBetween(String status, LocalDateTime startDate, LocalDateTime endDate);
     *
     * @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status AND DATE(p.createdAt) = CURRENT_DATE")
     * Long countTodaysPaymentsByStatus(@Param("status") String status);
     *
     * @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'COMPLETED' AND DATE(p.paymentDate) = CURRENT_DATE")
     * BigDecimal calculateTodaysRevenue();
     *
     * @Query("SELECT p FROM Payment p WHERE p.status = 'COMPLETED' ORDER BY p.paymentDate DESC")
     * List<Payment> findRecentSuccessfulPayments(Pageable pageable);
     *
     * // Cleanup operations
     * @Query("DELETE FROM Payment p WHERE p.status IN ('CANCELLED', 'FAILED') AND p.createdAt < :cutoffDate")
     * void deleteOldFailedPayments(@Param("cutoffDate") LocalDateTime cutoffDate);
     *
     * Page<Payment> findByStatusIn(List<String> statuses, Pageable pageable);
     */
}