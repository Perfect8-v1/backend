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

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Find by transaction ID
    Optional<Payment> findByTransactionId(String transactionId);

    // Find by order
    List<Payment> findByOrderId(Long orderId);

    // Find by order - first payment
    Optional<Payment> findFirstByOrderIdOrderByCreatedAtDesc(Long orderId);

    // Find successful payment for order
    Optional<Payment> findFirstByOrderIdAndStatus(Long orderId, String status);

    // Find by status
    List<Payment> findByStatus(String status);
    Page<Payment> findByStatus(String status, Pageable pageable);

    // Find by payment method
    List<Payment> findByPaymentMethod(String paymentMethod);
    Page<Payment> findByPaymentMethod(String paymentMethod, Pageable pageable);

    // Find by date range
    List<Payment> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    Page<Payment> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Find pending payments older than specified time
    @Query("SELECT p FROM Payment p WHERE p.status = 'PENDING' AND p.createdAt < :cutoffTime")
    List<Payment> findPendingPaymentsOlderThan(@Param("cutoffTime") LocalDateTime cutoffTime);

    // Find failed payments for retry
    @Query("SELECT p FROM Payment p WHERE p.status = 'FAILED' AND p.retryCount < :maxRetries")
    List<Payment> findFailedPaymentsForRetry(@Param("maxRetries") Integer maxRetries);

    // Find by reference number
    Optional<Payment> findByReferenceNumber(String referenceNumber);

    // Find by gateway payment ID
    Optional<Payment> findByGatewayPaymentId(String gatewayPaymentId);

    // Find refunded payments
    @Query("SELECT p FROM Payment p WHERE p.refundAmount > 0")
    List<Payment> findRefundedPayments();

    @Query("SELECT p FROM Payment p WHERE p.refundAmount > 0")
    Page<Payment> findRefundedPayments(Pageable pageable);

    // Find by payer email
    List<Payment> findByPayerEmail(String payerEmail);

    // Count by status
    Long countByStatus(String status);

    // Count by status and date range
    Long countByStatusAndCreatedAtBetween(String status, LocalDateTime startDate, LocalDateTime endDate);

    // Calculate total amount by status
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = :status")
    BigDecimal calculateTotalAmountByStatus(@Param("status") String status);

    // Calculate total amount by status and date range
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = :status AND p.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalAmountByStatusAndDateRange(
            @Param("status") String status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Calculate total refund amount
    @Query("SELECT SUM(p.refundAmount) FROM Payment p WHERE p.refundAmount > 0")
    BigDecimal calculateTotalRefundAmount();

    // Calculate total refund amount by date range
    @Query("SELECT SUM(p.refundAmount) FROM Payment p WHERE p.refundAmount > 0 AND p.refundDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalRefundAmountByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Find payments needing verification
    @Query("SELECT p FROM Payment p WHERE p.status = 'COMPLETED' AND p.isVerified = false")
    List<Payment> findPaymentsNeedingVerification();

    // Find recent successful payments
    @Query("SELECT p FROM Payment p WHERE p.status = 'COMPLETED' ORDER BY p.paymentDate DESC")
    List<Payment> findRecentSuccessfulPayments(Pageable pageable);

    // Check if order has successful payment
    @Query("SELECT COUNT(p) > 0 FROM Payment p WHERE p.order.id = :orderId AND p.status = 'COMPLETED'")
    boolean hasSuccessfulPayment(@Param("orderId") Long orderId);

    // Find duplicate payments (same order, amount, and status within time window)
    @Query("SELECT p FROM Payment p WHERE p.order.id = :orderId " +
            "AND p.amount = :amount " +
            "AND p.status = 'COMPLETED' " +
            "AND p.createdAt BETWEEN :startTime AND :endTime")
    List<Payment> findPotentialDuplicates(
            @Param("orderId") Long orderId,
            @Param("amount") BigDecimal amount,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    // Statistics queries for v1.0
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status AND DATE(p.createdAt) = CURRENT_DATE")
    Long countTodaysPaymentsByStatus(@Param("status") String status);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'COMPLETED' AND DATE(p.paymentDate) = CURRENT_DATE")
    BigDecimal calculateTodaysRevenue();

    // Find by multiple statuses
    List<Payment> findByStatusIn(List<String> statuses);
    Page<Payment> findByStatusIn(List<String> statuses, Pageable pageable);

    // Find payments by customer (through order)
    @Query("SELECT p FROM Payment p WHERE p.order.customer.id = :customerId")
    List<Payment> findByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT p FROM Payment p WHERE p.order.customer.id = :customerId")
    Page<Payment> findByCustomerId(@Param("customerId") Long customerId, Pageable pageable);

    // Delete old cancelled/failed payments (cleanup)
    @Query("DELETE FROM Payment p WHERE p.status IN ('CANCELLED', 'FAILED') AND p.createdAt < :cutoffDate")
    void deleteOldFailedPayments(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Check if transaction ID exists
    boolean existsByTransactionId(String transactionId);

    // Find payments with high retry count
    @Query("SELECT p FROM Payment p WHERE p.retryCount >= :minRetries ORDER BY p.retryCount DESC")
    List<Payment> findHighRetryPayments(@Param("minRetries") Integer minRetries);
}