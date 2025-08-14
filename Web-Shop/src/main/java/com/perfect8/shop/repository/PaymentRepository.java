package com.perfect8.shop.repository;

import com.perfect8.shop.model.Payment;
import com.perfect8.shop.model.PaymentStatus;
import com.perfect8.shop.model.PaymentMethod;
import com.perfect8.shop.model.Order;
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
 * Repository interface for Payment entity
 *
 * Som Lansen's precisionsnavigering - exakt spårning av alla betalningar!
 * Swedish engineering för säker och tillförlitlig betalningshantering.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // ============================================================================
    // PAYMENT IDENTIFICATION (Radar lock-on)
    // ============================================================================

    /**
     * Find payment by order
     */
    Optional<Payment> findByOrder(Order order);

    /**
     * Find payment by order ID
     */
    Optional<Payment> findByOrderId(Long orderId);

    /**
     * Find payment by transaction ID
     */
    Optional<Payment> findByTransactionId(String transactionId);

    /**
     * Find payment by PayPal payment ID
     */
    Optional<Payment> findByPaypalPaymentId(String paypalPaymentId);

    /**
     * Find payment by reference number
     */
    Optional<Payment> findByReferenceNumber(String referenceNumber);

    /**
     * Check if payment exists for order
     */
    boolean existsByOrderId(Long orderId);

    // ============================================================================
    // STATUS TRACKING (Mission status monitoring)
    // ============================================================================

    /**
     * Find payments by status
     */
    Page<Payment> findByStatusOrderByCreatedAtDesc(PaymentStatus status, Pageable pageable);

    /**
     * Find pending payments (awaiting processing)
     */
    List<Payment> findByStatusAndCreatedAtBefore(PaymentStatus status, LocalDateTime cutoffTime);

    /**
     * Find failed payments requiring attention
     */
    @Query("SELECT p FROM Payment p WHERE p.status = 'FAILED' AND p.retryCount < 3 ORDER BY p.createdAt ASC")
    List<Payment> findFailedPaymentsForRetry();

    /**
     * Find stuck payments (processing too long)
     */
    @Query("SELECT p FROM Payment p WHERE " +
            "(p.status = 'PENDING' AND p.initiatedAt < :pendingCutoff) OR " +
            "(p.status = 'AUTHORIZED' AND p.authorizedAt < :authorizedCutoff)")
    List<Payment> findStuckPayments(
            @Param("pendingCutoff") LocalDateTime pendingCutoff,
            @Param("authorizedCutoff") LocalDateTime authorizedCutoff
    );

    /**
     * Count payments by status
     */
    @Query("SELECT p.status, COUNT(p) FROM Payment p GROUP BY p.status")
    List<Object[]> countPaymentsByStatus();

    /**
     * Find payments requiring manual review
     */
    @Query("SELECT p FROM Payment p WHERE " +
            "p.retryCount >= 3 OR " +
            "(p.status = 'FAILED' AND p.amount >= :highValueThreshold)")
    List<Payment> findPaymentsRequiringReview(@Param("highValueThreshold") BigDecimal highValueThreshold);

    // ============================================================================
    // PAYMENT METHOD ANALYTICS (Weapons system analysis)
    // ============================================================================

    /**
     * Find payments by method
     */
    Page<Payment> findByPaymentMethodOrderByCreatedAtDesc(PaymentMethod method, Pageable pageable);

    /**
     * Get payment method distribution
     */
    @Query("SELECT p.paymentMethod, COUNT(p), SUM(p.amount), AVG(p.amount) " +
            "FROM Payment p WHERE p.status = 'COMPLETED' AND p.createdAt >= :startDate " +
            "GROUP BY p.paymentMethod ORDER BY COUNT(p) DESC")
    List<Object[]> getPaymentMethodDistribution(@Param("startDate") LocalDateTime startDate);

    /**
     * Get success rate by payment method
     */
    @Query("SELECT p.paymentMethod, " +
            "COUNT(p) as total, " +
            "SUM(CASE WHEN p.status = 'COMPLETED' THEN 1 ELSE 0 END) as successful, " +
            "SUM(CASE WHEN p.status = 'FAILED' THEN 1 ELSE 0 END) as failed " +
            "FROM Payment p WHERE p.createdAt >= :startDate " +
            "GROUP BY p.paymentMethod")
    List<Object[]> getPaymentMethodSuccessRates(@Param("startDate") LocalDateTime startDate);

    /**
     * Find PayPal payments needing webhook verification
     */
    @Query("SELECT p FROM Payment p WHERE p.paymentMethod = 'PAYPAL' AND " +
            "p.status IN ('PENDING', 'AUTHORIZED') AND " +
            "p.paypalPaymentId IS NOT NULL")
    List<Payment> findPayPalPaymentsForVerification();

    // ============================================================================
    // FINANCIAL ANALYTICS (Strategic intelligence)
    // ============================================================================

    /**
     * Get daily payment metrics
     */
    @Query("SELECT DATE(p.capturedAt), COUNT(p), SUM(p.amount), AVG(p.amount) " +
            "FROM Payment p WHERE p.status = 'COMPLETED' AND p.capturedAt >= :startDate " +
            "GROUP BY DATE(p.capturedAt) ORDER BY DATE(p.capturedAt)")
    List<Object[]> getDailyPaymentMetrics(@Param("startDate") LocalDateTime startDate);

    /**
     * Get monthly revenue
     */
    @Query("SELECT YEAR(p.capturedAt), MONTH(p.capturedAt), SUM(p.amount), COUNT(p) " +
            "FROM Payment p WHERE p.status = 'COMPLETED' " +
            "GROUP BY YEAR(p.capturedAt), MONTH(p.capturedAt) " +
            "ORDER BY YEAR(p.capturedAt), MONTH(p.capturedAt)")
    List<Object[]> getMonthlyPaymentRevenue();

    /**
     * Get payment volume trends
     */
    @Query("SELECT DATE(p.createdAt), " +
            "COUNT(p) as totalPayments, " +
            "SUM(CASE WHEN p.status = 'COMPLETED' THEN 1 ELSE 0 END) as successfulPayments, " +
            "SUM(CASE WHEN p.status = 'FAILED' THEN 1 ELSE 0 END) as failedPayments, " +
            "SUM(p.amount) as totalAmount " +
            "FROM Payment p WHERE p.createdAt >= :startDate " +
            "GROUP BY DATE(p.createdAt) ORDER BY DATE(p.createdAt)")
    List<Object[]> getPaymentVolumeTrends(@Param("startDate") LocalDateTime startDate);

    /**
     * Calculate total processing fees
     */
    @Query("SELECT SUM(p.processingFee), AVG(p.processingFee) " +
            "FROM Payment p WHERE p.status = 'COMPLETED' AND p.capturedAt >= :startDate")
    Object[] getTotalProcessingFees(@Param("startDate") LocalDateTime startDate);

    /**
     * Get average payment value
     */
    @Query("SELECT AVG(p.amount) FROM Payment p WHERE p.status = 'COMPLETED'")
    BigDecimal getAveragePaymentValue();

    // ============================================================================
    // CUSTOMER PAYMENT ANALYTICS (Target behavior analysis)
    // ============================================================================

    /**
     * Find payments by customer email
     */
    @Query("SELECT p FROM Payment p WHERE p.order.customer.email = :email ORDER BY p.createdAt DESC")
    Page<Payment> findByCustomerEmail(@Param("email") String email, Pageable pageable);

    /**
     * Get customer payment history
     */
    @Query("SELECT p FROM Payment p WHERE p.order.customer.id = :customerId ORDER BY p.createdAt DESC")
    List<Payment> findByCustomerId(@Param("customerId") Long customerId);

    /**
     * Find customers with failed payments
     */
    @Query("SELECT DISTINCT p.order.customer FROM Payment p WHERE p.status = 'FAILED'")
    List<Object> findCustomersWithFailedPayments();

    /**
     * Get customer payment method preferences
     */
    @Query("SELECT p.order.customer, p.paymentMethod, COUNT(p) " +
            "FROM Payment p WHERE p.status = 'COMPLETED' " +
            "GROUP BY p.order.customer, p.paymentMethod " +
            "ORDER BY p.order.customer, COUNT(p) DESC")
    List<Object[]> getCustomerPaymentMethodPreferences();

    /**
     * Find high-value payments
     */
    @Query("SELECT p FROM Payment p WHERE p.amount >= :threshold AND p.status = 'COMPLETED' ORDER BY p.amount DESC")
    List<Payment> findHighValuePayments(@Param("threshold") BigDecimal threshold);

    // ============================================================================
    // FRAUD DETECTION & SECURITY (Threat detection radar)
    // ============================================================================

    /**
     * Find suspicious payment patterns
     */
    @Query("SELECT p.order.customer.email, COUNT(p), SUM(p.amount) " +
            "FROM Payment p WHERE p.createdAt >= :startTime " +
            "GROUP BY p.order.customer.email " +
            "HAVING COUNT(p) > :maxPayments OR SUM(p.amount) > :maxAmount")
    List<Object[]> findSuspiciousPaymentPatterns(
            @Param("startTime") LocalDateTime startTime,
            @Param("maxPayments") Integer maxPayments,
            @Param("maxAmount") BigDecimal maxAmount
    );

    /**
     * Find payments with multiple retry attempts
     */
    @Query("SELECT p FROM Payment p WHERE p.retryCount >= :threshold ORDER BY p.retryCount DESC")
    List<Payment> findPaymentsWithMultipleRetries(@Param("threshold") Integer threshold);

    /**
     * Find payments from same IP/location (requires additional tracking)
     */
    @Query("SELECT p.order.shippingCountry, p.paymentMethod, COUNT(p) " +
            "FROM Payment p WHERE p.createdAt >= :startTime " +
            "GROUP BY p.order.shippingCountry, p.paymentMethod " +
            "HAVING COUNT(p) > :threshold")
    List<Object[]> findUnusualGeographicPatterns(
            @Param("startTime") LocalDateTime startTime,
            @Param("threshold") Integer threshold
    );

    // ============================================================================
    // REFUND MANAGEMENT (Recovery operations)
    // ============================================================================

    /**
     * Find payments eligible for refund
     */
    @Query("SELECT p FROM Payment p WHERE p.status = 'COMPLETED' AND " +
            "p.capturedAt >= :minDate AND p.amount <= :maxRefundAmount")
    List<Payment> findPaymentsEligibleForRefund(
            @Param("minDate") LocalDateTime minDate,
            @Param("maxRefundAmount") BigDecimal maxRefundAmount
    );

    /**
     * Find refunded payments
     */
    List<Payment> findByStatusAndRefundedAtIsNotNull(PaymentStatus status);

    /**
     * Calculate total refunds
     */
    @Query("SELECT COUNT(p), SUM(p.amount) FROM Payment p WHERE p.status = 'REFUNDED' AND p.refundedAt >= :startDate")
    Object[] getTotalRefunds(@Param("startDate") LocalDateTime startDate);

    /**
     * Get refund rate by payment method
     */
    @Query("SELECT p.paymentMethod, " +
            "COUNT(CASE WHEN p.status = 'COMPLETED' THEN 1 END) as completed, " +
            "COUNT(CASE WHEN p.status = 'REFUNDED' THEN 1 END) as refunded " +
            "FROM Payment p WHERE p.capturedAt >= :startDate " +
            "GROUP BY p.paymentMethod")
    List<Object[]> getRefundRateByMethod(@Param("startDate") LocalDateTime startDate);

    // ============================================================================
    // ADVANCED FILTERING (Precision targeting)
    // ============================================================================

    /**
     * Advanced payment search with multiple criteria
     */
    @Query("SELECT p FROM Payment p WHERE " +
            "(:status IS NULL OR p.status = :status) AND " +
            "(:method IS NULL OR p.paymentMethod = :method) AND " +
            "(:startDate IS NULL OR p.createdAt >= :startDate) AND " +
            "(:endDate IS NULL OR p.createdAt <= :endDate) AND " +
            "(:minAmount IS NULL OR p.amount >= :minAmount) AND " +
            "(:maxAmount IS NULL OR p.amount <= :maxAmount) " +
            "ORDER BY p.createdAt DESC")
    Page<Payment> findPaymentsWithFilters(
            @Param("status") PaymentStatus status,
            @Param("method") PaymentMethod method,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            Pageable pageable
    );

    /**
     * Find payments in date range
     */
    @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate ORDER BY p.createdAt DESC")
    List<Payment> findPaymentsByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find payments by amount range
     */
    @Query("SELECT p FROM Payment p WHERE p.amount BETWEEN :minAmount AND :maxAmount ORDER BY p.amount DESC")
    List<Payment> findPaymentsByAmountRange(
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount
    );

    // ============================================================================
    // OPERATIONAL UPDATES (Mission control)
    // ============================================================================

    /**
     * Update payment status
     */
    @Modifying
    @Query("UPDATE Payment p SET p.status = :status WHERE p.id = :paymentId")
    int updatePaymentStatus(@Param("paymentId") Long paymentId, @Param("status") PaymentStatus status);

    /**
     * Update PayPal payment details
     */
    @Modifying
    @Query("UPDATE Payment p SET p.paypalPayerId = :payerId, p.status = :status WHERE p.paypalPaymentId = :paymentId")
    int updatePayPalPayment(
            @Param("paymentId") String paymentId,
            @Param("payerId") String payerId,
            @Param("status") PaymentStatus status
    );

    /**
     * Increment retry count
     */
    @Modifying
    @Query("UPDATE Payment p SET p.retryCount = p.retryCount + 1, p.failureReason = :reason WHERE p.id = :paymentId")
    int incrementRetryCount(@Param("paymentId") Long paymentId, @Param("reason") String reason);

    /**
     * Update gateway response
     */
    @Modifying
    @Query("UPDATE Payment p SET p.gatewayResponseCode = :code, p.gatewayResponseMessage = :message WHERE p.id = :paymentId")
    int updateGatewayResponse(
            @Param("paymentId") Long paymentId,
            @Param("code") String code,
            @Param("message") String message
    );

    // ============================================================================
    // PERFORMANCE OPTIMIZATION (Afterburner speed)
    // ============================================================================

    /**
     * Find payment with order (fetch join)
     */
    @Query("SELECT p FROM Payment p JOIN FETCH p.order WHERE p.id = :paymentId")
    Optional<Payment> findByIdWithOrder(@Param("paymentId") Long paymentId);

    /**
     * Find payment with full order details
     */
    @Query("SELECT p FROM Payment p " +
            "JOIN FETCH p.order o " +
            "JOIN FETCH o.customer " +
            "WHERE p.id = :paymentId")
    Optional<Payment> findByIdWithFullDetails(@Param("paymentId") Long paymentId);

    /**
     * Batch find payments by IDs
     */
    @Query("SELECT p FROM Payment p WHERE p.id IN :paymentIds ORDER BY p.createdAt DESC")
    List<Payment> findPaymentsByIds(@Param("paymentIds") List<Long> paymentIds);

    /**
     * Find payments for export (lightweight)
     */
    @Query("SELECT p.id, p.transactionId, p.amount, p.currency, p.status, p.paymentMethod, p.createdAt " +
            "FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate ORDER BY p.createdAt DESC")
    List<Object[]> findPaymentsForExport(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // ============================================================================
    // REPORTING & ANALYTICS (Intelligence reports)
    // ============================================================================

    /**
     * Executive payment summary
     */
    @Query("SELECT " +
            "COUNT(p) as totalPayments, " +
            "SUM(CASE WHEN p.status = 'COMPLETED' THEN p.amount ELSE 0 END) as totalRevenue, " +
            "AVG(CASE WHEN p.status = 'COMPLETED' THEN p.amount END) as avgPaymentValue, " +
            "COUNT(CASE WHEN p.status = 'COMPLETED' THEN 1 END) * 100.0 / COUNT(p) as successRate " +
            "FROM Payment p WHERE p.createdAt >= :startDate")
    Object[] getPaymentSummary(@Param("startDate") LocalDateTime startDate);

    /**
     * Payment processing time analysis
     */
    @Query("SELECT " +
            "AVG(TIMESTAMPDIFF(SECOND, p.initiatedAt, p.authorizedAt)) as avgAuthTime, " +
            "AVG(TIMESTAMPDIFF(SECOND, p.authorizedAt, p.capturedAt)) as avgCaptureTime, " +
            "MAX(TIMESTAMPDIFF(SECOND, p.initiatedAt, p.capturedAt)) as maxProcessingTime " +
            "FROM Payment p WHERE p.status = 'COMPLETED' AND p.capturedAt >= :startDate")
    Object[] getPaymentProcessingTimes(@Param("startDate") LocalDateTime startDate);

    /**
     * Peak payment times analysis
     */
    @Query("SELECT HOUR(p.createdAt), COUNT(p), AVG(p.amount) " +
            "FROM Payment p WHERE p.createdAt >= :startDate " +
            "GROUP BY HOUR(p.createdAt) ORDER BY HOUR(p.createdAt)")
    List<Object[]> getPaymentTimeDistribution(@Param("startDate") LocalDateTime startDate);

    /**
     * Currency distribution
     */
    @Query("SELECT p.currency, COUNT(p), SUM(p.amount) " +
            "FROM Payment p WHERE p.status = 'COMPLETED' " +
            "GROUP BY p.currency ORDER BY COUNT(p) DESC")
    List<Object[]> getCurrencyDistribution();

    // ============================================================================
    // MAINTENANCE & CLEANUP (Ground maintenance)
    // ============================================================================

    /**
     * Count payments by status and date
     */
    @Query("SELECT " +
            "COUNT(p) as total, " +
            "SUM(CASE WHEN p.status = 'COMPLETED' THEN 1 ELSE 0 END) as completed, " +
            "SUM(CASE WHEN p.status = 'FAILED' THEN 1 ELSE 0 END) as failed, " +
            "SUM(CASE WHEN p.status = 'PENDING' THEN 1 ELSE 0 END) as pending " +
            "FROM Payment p WHERE p.createdAt >= :startDate")
    Object[] getPaymentStatusCounts(@Param("startDate") LocalDateTime startDate);

    /**
     * Find orphaned payments (no order)
     */
    @Query("SELECT p FROM Payment p WHERE p.order IS NULL")
    List<Payment> findOrphanedPayments();

    /**
     * Clean up old failed payments
     */
    @Modifying
    @Query("DELETE FROM Payment p WHERE p.status = 'FAILED' AND p.createdAt < :cutoffDate")
    int deleteOldFailedPayments(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find payments needing attention
     */
    @Query("SELECT p FROM Payment p WHERE " +
            "(p.transactionId IS NULL AND p.status != 'PENDING') OR " +
            "(p.amount <= 0) OR " +
            "(p.gatewayResponseCode IS NULL AND p.status IN ('FAILED', 'COMPLETED'))")
    List<Payment> findPaymentsNeedingAttention();
}