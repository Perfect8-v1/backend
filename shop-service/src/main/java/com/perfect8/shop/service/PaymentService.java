package com.perfect8.shop.service;

import com.perfect8.shop.dto.PaymentRequestDTO;
import com.perfect8.shop.dto.PaymentResponseDTO;
import com.perfect8.shop.entity.Order;
import com.perfect8.shop.entity.Payment;
import com.perfect8.shop.entity.Customer;
import com.perfect8.shop.enums.PaymentMethod;
import com.perfect8.shop.enums.PaymentStatus;
import com.perfect8.shop.exception.PaymentException;
import com.perfect8.shop.exception.OrderNotFoundException;
import com.perfect8.shop.repository.PaymentRepository;
import com.perfect8.shop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for handling payment operations.
 * Version 1.0 - Core payment functionality with PayPal as primary payment method
 *
 * This service handles all critical payment operations:
 * - Payment processing through PayPal
 * - Payment verification and completion
 * - Refund processing (critical for customer service)
 * - Payment retry and cancellation
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PayPalService payPalService;
    private final EmailService emailService;

    @Value("${payment.default.currency:USD}")
    private String defaultCurrency;

    /**
     * Process payment for an order - Core functionality
     * All payments go through PayPal in v1.0
     */
    public Payment processPayment(Order order, PaymentRequestDTO paymentRequest) {
        log.info("Processing payment for order: {}", order.getOrderNumber());

        try {
            // Create payment record
            Payment payment = new Payment();
            payment.setOrder(order);
            payment.setAmount(paymentRequest.getAmount());
            payment.setCurrency(paymentRequest.getCurrency() != null ? paymentRequest.getCurrency() : defaultCurrency);
            payment.setPaymentMethod(PaymentMethod.fromString(paymentRequest.getPaymentMethod()));
            payment.setPaymentStatus(PaymentStatus.PENDING);
            payment.setTransactionId(generateTransactionId());
            payment.setCreatedAt(LocalDateTime.now());

            // Save initial payment record
            payment = paymentRepository.save(payment);

            // Process with PayPal (primary payment method in v1.0)
            PaymentResponseDTO paypalResponse = payPalService.processPayment(paymentRequest);

            // Update payment based on response
            if (paypalResponse.isSuccessful()) {
                payment.setPaymentStatus(PaymentStatus.COMPLETED);
                payment.setGatewayPaymentId(paypalResponse.getTransactionId());
                payment.setPaymentDate(LocalDateTime.now());
                payment.setIsVerified(Boolean.TRUE);
                payment.setVerificationDate(LocalDateTime.now());

                // Update order to trigger update timestamps
                orderRepository.save(order);

                // Send confirmation email - FIXED: Now using correct signature
                try {
                    Customer customer = order.getCustomer();
                    String customerEmail = customer.getEmail();
                    String orderNumber = order.getOrderNumber();
                    String amountString = payment.getAmount().toString();
                    String currencyCode = payment.getCurrency();

                    emailService.sendPaymentConfirmation(customerEmail, orderNumber, amountString, currencyCode);
                } catch (Exception emailException) {
                    log.error("Failed to send payment confirmation email: {}", emailException.getMessage());
                }

            } else {
                payment.setPaymentStatus(PaymentStatus.FAILED);
                payment.setFailureReason(paypalResponse.getErrorMessage());
                payment.incrementRetryCount();
            }

            payment.setUpdatedAt(LocalDateTime.now());
            return paymentRepository.save(payment);

        } catch (Exception paymentException) {
            log.error("Payment processing failed for order {}: {}", order.getOrderId(), paymentException.getMessage());
            throw new PaymentException("Payment processing failed: " + paymentException.getMessage());
        }
    }

    /**
     * Create payment record for an order - Core functionality
     */
    public Payment createPayment(Order order, Map<String, Object> paymentDetailsMap) {
        log.info("Creating payment for order: {}", order.getOrderNumber());

        PaymentMethod paymentMethodEnum = PaymentMethod.fromString(
                (String) paymentDetailsMap.getOrDefault("paymentMethod", "PAYPAL")
        );
        BigDecimal paymentAmount = order.getTotalAmount();

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(paymentAmount);
        payment.setCurrency(order.getCurrency() != null ? order.getCurrency() : defaultCurrency);
        payment.setPaymentMethod(paymentMethodEnum);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setTransactionId(generateTransactionId());
        payment.setCreatedAt(LocalDateTime.now());

        // Extract payer information if available
        String payerEmailAddress = (String) paymentDetailsMap.get("payerEmail");
        String payerFullName = (String) paymentDetailsMap.get("payerName");

        payment.setPayerEmail(payerEmailAddress);
        payment.setPayerName(payerFullName);

        return paymentRepository.save(payment);
    }

    /**
     * Complete payment - Core functionality
     */
    @Transactional
    public Payment completePayment(Long paymentIdLong, String gatewayTransactionIdString) {
        log.info("Completing payment: {}", paymentIdLong);

        Payment payment = paymentRepository.findById(paymentIdLong)
                .orElseThrow(() -> new PaymentException("Payment not found: " + paymentIdLong));

        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        payment.setGatewayPaymentId(gatewayTransactionIdString);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setIsVerified(Boolean.TRUE);
        payment.setVerificationDate(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());

        // Update order to trigger update timestamp
        Order relatedOrder = payment.getOrder();
        if (relatedOrder != null) {
            orderRepository.save(relatedOrder);
        }

        return paymentRepository.save(payment);
    }

    /**
     * Process refund - Core functionality
     * Critical for customer service and legal compliance
     */
    @Transactional
    public Payment processRefund(Long paymentIdLong, BigDecimal refundAmountDecimal, String refundReasonString) {
        log.info("Processing refund for payment: {}, amount: {}", paymentIdLong, refundAmountDecimal);

        Payment payment = paymentRepository.findById(paymentIdLong)
                .orElseThrow(() -> new PaymentException("Payment not found: " + paymentIdLong));

        // Validate refund
        if (!PaymentStatus.COMPLETED.equals(payment.getPaymentStatus())) {
            throw new PaymentException("Can only refund completed payments");
        }

        if (refundAmountDecimal.compareTo(payment.getAmount()) > 0) {
            throw new PaymentException("Refund amount cannot exceed payment amount");
        }

        BigDecimal currentRefundedAmount = payment.getRefundAmount() != null ?
                payment.getRefundAmount() : BigDecimal.ZERO;
        BigDecimal newTotalRefundedAmount = currentRefundedAmount.add(refundAmountDecimal);

        if (newTotalRefundedAmount.compareTo(payment.getAmount()) > 0) {
            throw new PaymentException("Total refunds would exceed payment amount");
        }

        // Process refund with PayPal
        Boolean refundSuccessStatus = payPalService.processRefund(
                payment.getGatewayPaymentId(),
                refundAmountDecimal,
                refundReasonString);

        if (Boolean.TRUE.equals(refundSuccessStatus)) {
            payment.setRefundAmount(newTotalRefundedAmount);
            payment.setRefundReason(refundReasonString);
            payment.setRefundDate(LocalDateTime.now());

            if (newTotalRefundedAmount.compareTo(payment.getAmount()) >= 0) {
                payment.setPaymentStatus(PaymentStatus.REFUNDED);
                payment.setIsPartialRefund(Boolean.FALSE);
            } else {
                payment.setPaymentStatus(PaymentStatus.PARTIALLY_REFUNDED);
                payment.setIsPartialRefund(Boolean.TRUE);
            }

            payment.setUpdatedAt(LocalDateTime.now());

            // Update order if fully refunded
            if (PaymentStatus.REFUNDED.equals(payment.getPaymentStatus())) {
                Order relatedOrder = payment.getOrder();
                if (relatedOrder != null) {
                    orderRepository.save(relatedOrder);
                }
            }

            log.info("Refund processed successfully for payment: {}", paymentIdLong);
        } else {
            throw new PaymentException("Refund processing failed");
        }

        return paymentRepository.save(payment);
    }

    /**
     * Process refund (simplified signature) - Core functionality
     */
    @Transactional
    public Payment processRefund(Long paymentIdLong, BigDecimal refundAmountDecimal) {
        return processRefund(paymentIdLong, refundAmountDecimal, "Customer requested refund");
    }

    /**
     * Verify payment status - Core functionality
     * Important for handling pending payments
     */
    public String verifyPaymentStatus(Long paymentIdLong) {
        log.info("Verifying payment status for ID: {}", paymentIdLong);

        Payment payment = paymentRepository.findById(paymentIdLong)
                .orElseThrow(() -> new PaymentException("Payment not found: " + paymentIdLong));

        // If payment is old and pending, verify with PayPal
        if (PaymentStatus.PENDING.equals(payment.getPaymentStatus()) &&
                payment.getCreatedAt().isBefore(LocalDateTime.now().minusMinutes(5))) {

            String currentStatusString = payPalService.verifyPaymentStatus(payment.getTransactionId());
            PaymentStatus currentPaymentStatus = PaymentStatus.fromString(currentStatusString);

            if (!payment.getPaymentStatus().equals(currentPaymentStatus)) {
                payment.setPaymentStatus(currentPaymentStatus);
                payment.setUpdatedAt(LocalDateTime.now());

                if (PaymentStatus.COMPLETED.equals(currentPaymentStatus)) {
                    payment.setPaymentDate(LocalDateTime.now());
                    payment.setIsVerified(Boolean.TRUE);
                    payment.setVerificationDate(LocalDateTime.now());
                }

                paymentRepository.save(payment);
            }

            return currentPaymentStatus.name();
        }

        return payment.getPaymentStatus().name();
    }

    /**
     * Get payment by order ID - Core functionality
     */
    public Payment getPaymentByOrderId(Long orderIdLong) {
        return paymentRepository.findFirstByOrderIdOrderByCreatedAtDesc(orderIdLong)
                .orElse(null);
    }

    /**
     * Get all payments for an order - Core functionality
     */
    public List<Payment> getPaymentsByOrderId(Long orderIdLong) {
        return paymentRepository.findByOrderId(orderIdLong);
    }

    /**
     * Check if payment is refundable - Core functionality
     */
    public Boolean isRefundable(Long paymentIdLong) {
        Payment payment = paymentRepository.findById(paymentIdLong)
                .orElse(null);

        if (payment == null) {
            return Boolean.FALSE;
        }

        return PaymentStatus.COMPLETED.equals(payment.getPaymentStatus()) ||
                PaymentStatus.PARTIALLY_REFUNDED.equals(payment.getPaymentStatus());
    }

    /**
     * Calculate refundable amount - Core functionality
     */
    public BigDecimal getRefundableAmount(Long paymentIdLong) {
        Payment payment = paymentRepository.findById(paymentIdLong)
                .orElse(null);

        if (payment == null || !isRefundable(paymentIdLong)) {
            return BigDecimal.ZERO;
        }

        BigDecimal refundedAmount = payment.getRefundAmount() != null ?
                payment.getRefundAmount() : BigDecimal.ZERO;

        return payment.getAmount().subtract(refundedAmount);
    }

    /**
     * Retry failed payment - Core functionality
     * Important for handling payment failures
     */
    @Transactional
    public Payment retryPayment(Long paymentIdLong) {
        log.info("Retrying payment: {}", paymentIdLong);

        Payment payment = paymentRepository.findById(paymentIdLong)
                .orElseThrow(() -> new PaymentException("Payment not found: " + paymentIdLong));

        if (!PaymentStatus.FAILED.equals(payment.getPaymentStatus())) {
            throw new PaymentException("Can only retry failed payments");
        }

        Integer currentRetryCount = payment.getRetryCount();
        Integer maxRetryAttempts = 3;

        if (currentRetryCount >= maxRetryAttempts) {
            throw new PaymentException("Maximum retry attempts exceeded");
        }

        payment.incrementRetryCount();
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setUpdatedAt(LocalDateTime.now());
        payment.setLastRetryDate(LocalDateTime.now());

        paymentRepository.save(payment);

        // Create new payment request
        PaymentRequestDTO retryRequest = new PaymentRequestDTO();
        retryRequest.setAmount(payment.getAmount());
        retryRequest.setCurrency(payment.getCurrency());
        retryRequest.setPaymentMethod(payment.getPaymentMethod().name());
        retryRequest.setOrderId(payment.getOrder().getOrderId());

        // Process payment again
        return processPayment(payment.getOrder(), retryRequest);
    }

    /**
     * Cancel pending payment - Core functionality
     */
    @Transactional
    public Payment cancelPayment(Long paymentIdLong, String cancellationReasonString) {
        log.info("Cancelling payment: {}", paymentIdLong);

        Payment payment = paymentRepository.findById(paymentIdLong)
                .orElseThrow(() -> new PaymentException("Payment not found: " + paymentIdLong));

        if (PaymentStatus.COMPLETED.equals(payment.getPaymentStatus())) {
            throw new PaymentException("Cannot cancel completed payment");
        }

        payment.setPaymentStatus(PaymentStatus.CANCELLED);
        payment.setNotes(cancellationReasonString);
        payment.setUpdatedAt(LocalDateTime.now());

        return paymentRepository.save(payment);
    }

    /**
     * Generate unique transaction ID
     */
    private String generateTransactionId() {
        Long currentTimeMillis = System.currentTimeMillis();
        String randomUuidPart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "TXN-" + currentTimeMillis + "-" + randomUuidPart;
    }

    /* ============================================
     * VERSION 2.0 METHODS - Commented out for v1.0
     * ============================================
     * These methods will be implemented in version 2.0:
     * - Payment analytics and reporting
     * - Multiple payment gateway support
     * - Advanced fraud detection
     * - Payment plans and installments
     */

    /*
    // Version 2.0: Get payment summary for reporting
    public Map<String, Object> getPaymentSummary(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        Map<String, Object> summaryMap = new HashMap<>();

        BigDecimal totalCompletedAmount = paymentRepository.calculateTotalAmountByStatusAndDateRange(
                PaymentStatus.COMPLETED, startDateTime, endDateTime);
        BigDecimal totalRefundedAmount = paymentRepository.calculateTotalRefundAmountByDateRange(
                startDateTime, endDateTime);
        Long completedTransactionCount = paymentRepository.countByStatusAndCreatedAtBetween(
                PaymentStatus.COMPLETED, startDateTime, endDateTime);

        summaryMap.put("totalRevenue", totalCompletedAmount != null ? totalCompletedAmount : BigDecimal.ZERO);
        summaryMap.put("totalRefunded", totalRefundedAmount != null ? totalRefundedAmount : BigDecimal.ZERO);
        summaryMap.put("netRevenue", totalCompletedAmount != null && totalRefundedAmount != null ?
                totalCompletedAmount.subtract(totalRefundedAmount) : BigDecimal.ZERO);
        summaryMap.put("transactionCount", completedTransactionCount);
        summaryMap.put("period", Map.of("start", startDateTime, "end", endDateTime));

        return summaryMap;
    }
    */
}