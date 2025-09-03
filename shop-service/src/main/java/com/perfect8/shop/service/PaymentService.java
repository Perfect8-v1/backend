package com.perfect8.shop.service;

import com.perfect8.shop.dto.PaymentRequestDTO;
import com.perfect8.shop.dto.PaymentResponseDTO;
import com.perfect8.shop.entity.Order;
import com.perfect8.shop.entity.Payment;
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
import java.util.HashMap;
import java.util.UUID;

/**
 * Service for handling payment operations.
 * Version 1.0 - Simplified core payment functionality (PayPal primary)
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
     * Process payment for an order
     */
    public Payment processPayment(Order order, PaymentRequestDTO request) {
        log.info("Processing payment for order: {}", order.getOrderNumber());

        try {
            // Create payment record
            Payment payment = new Payment();
            payment.setOrder(order);
            payment.setAmount(request.getAmount());
            payment.setCurrency(request.getCurrency() != null ? request.getCurrency() : defaultCurrency);
            payment.setPaymentMethod(request.getPaymentMethod());
            payment.setStatus("PENDING");
            payment.setTransactionId(generateTransactionId());
            payment.setCreatedAt(LocalDateTime.now());

            // Save initial payment record
            payment = paymentRepository.save(payment);

            // Process with PayPal (all payments go through PayPal in v1.0)
            PaymentResponseDTO response = payPalService.processPayment(request);

            // Update payment based on response
            if (response.isSuccessful()) {
                payment.setStatus("COMPLETED");
                payment.setGatewayPaymentId(response.getTransactionId());
                payment.setPaymentDate(LocalDateTime.now());
                payment.setIsVerified(true);
                payment.setVerificationDate(LocalDateTime.now());

                // Update order status - Order entity doesn't have paymentStatus field
                // Just save to trigger any update timestamps
                orderRepository.save(order);

                // Send confirmation email
                try {
                    emailService.sendPaymentConfirmation(order, payment.getAmount());
                } catch (Exception e) {
                    log.error("Failed to send payment confirmation email: {}", e.getMessage());
                }

            } else {
                payment.setStatus("FAILED");
                payment.setFailureReason(response.getErrorMessage());
                payment.incrementRetryCount();
            }

            payment.setUpdatedAt(LocalDateTime.now());
            return paymentRepository.save(payment);

        } catch (Exception e) {
            log.error("Payment processing failed for order {}: {}", order.getOrderId(), e.getMessage());
            throw new PaymentException("Payment processing failed: " + e.getMessage());
        }
    }

    /**
     * Create payment record for an order
     */
    public Payment createPayment(Order order, Map<String, Object> paymentDetails) {
        log.info("Creating payment for order: {}", order.getOrderNumber());

        String paymentMethod = (String) paymentDetails.getOrDefault("paymentMethod", "PAYPAL");
        BigDecimal amount = order.getTotalAmount();

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(amount);
        payment.setCurrency(order.getCurrency() != null ? order.getCurrency() : defaultCurrency);
        payment.setPaymentMethod(paymentMethod);
        payment.setStatus("PENDING");
        payment.setTransactionId(generateTransactionId());
        payment.setCreatedAt(LocalDateTime.now());

        // Extract payer information if available
        payment.setPayerEmail((String) paymentDetails.get("payerEmail"));
        payment.setPayerName((String) paymentDetails.get("payerName"));

        return paymentRepository.save(payment);
    }

    /**
     * Complete payment
     */
    @Transactional
    public Payment completePayment(Long paymentId, String gatewayTransactionId) {
        log.info("Completing payment: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException("Payment not found: " + paymentId));

        payment.setStatus("COMPLETED");
        payment.setGatewayPaymentId(gatewayTransactionId);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setIsVerified(true);
        payment.setVerificationDate(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());

        // Update order - just save to trigger update timestamp
        Order order = payment.getOrder();
        if (order != null) {
            orderRepository.save(order);
        }

        return paymentRepository.save(payment);
    }

    /**
     * Process refund
     */
    @Transactional
    public Payment processRefund(Long paymentId, BigDecimal refundAmount, String reason) {
        log.info("Processing refund for payment: {}, amount: {}", paymentId, refundAmount);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException("Payment not found: " + paymentId));

        // Validate refund
        if (!"COMPLETED".equals(payment.getStatus())) {
            throw new PaymentException("Can only refund completed payments");
        }

        if (refundAmount.compareTo(payment.getAmount()) > 0) {
            throw new PaymentException("Refund amount cannot exceed payment amount");
        }

        BigDecimal currentRefunded = payment.getRefundAmount() != null ?
                payment.getRefundAmount() : BigDecimal.ZERO;
        BigDecimal newTotalRefunded = currentRefunded.add(refundAmount);

        if (newTotalRefunded.compareTo(payment.getAmount()) > 0) {
            throw new PaymentException("Total refunds would exceed payment amount");
        }

        // Process refund with PayPal
        boolean refundSuccess = payPalService.processRefund(
                payment.getGatewayPaymentId(),
                refundAmount,
                reason);

        if (refundSuccess) {
            payment.setRefundAmount(newTotalRefunded);
            payment.setRefundReason(reason);
            payment.setRefundDate(LocalDateTime.now());

            if (newTotalRefunded.compareTo(payment.getAmount()) >= 0) {
                payment.setStatus("REFUNDED");
                payment.setIsPartialRefund(false);
            } else {
                payment.setStatus("PARTIALLY_REFUNDED");
                payment.setIsPartialRefund(true);
            }

            payment.setUpdatedAt(LocalDateTime.now());

            // Update order if fully refunded - just save to trigger update
            if ("REFUNDED".equals(payment.getStatus())) {
                Order order = payment.getOrder();
                if (order != null) {
                    orderRepository.save(order);
                }
            }

            log.info("Refund processed successfully for payment: {}", paymentId);
        } else {
            throw new PaymentException("Refund processing failed");
        }

        return paymentRepository.save(payment);
    }

    /**
     * Verify payment status
     */
    public String verifyPaymentStatus(Long paymentId) {
        log.info("Verifying payment status for ID: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException("Payment not found: " + paymentId));

        // If payment is old and pending, verify with PayPal
        if ("PENDING".equals(payment.getStatus()) &&
                payment.getCreatedAt().isBefore(LocalDateTime.now().minusMinutes(5))) {

            String currentStatus = payPalService.verifyPaymentStatus(payment.getTransactionId());

            if (!payment.getStatus().equals(currentStatus)) {
                payment.setStatus(currentStatus);
                payment.setUpdatedAt(LocalDateTime.now());

                if ("COMPLETED".equals(currentStatus)) {
                    payment.setPaymentDate(LocalDateTime.now());
                    payment.setIsVerified(true);
                    payment.setVerificationDate(LocalDateTime.now());
                }

                paymentRepository.save(payment);
            }

            return currentStatus;
        }

        return payment.getStatus();
    }

    /**
     * Get payment by order ID
     */
    public Payment getPaymentByOrderId(Long orderId) {
        return paymentRepository.findFirstByOrderIdOrderByCreatedAtDesc(orderId)
                .orElse(null);
    }

    /**
     * Get all payments for an order
     */
    public List<Payment> getPaymentsByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    /**
     * Check if payment is refundable
     */
    public boolean isRefundable(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElse(null);

        if (payment == null) {
            return false;
        }

        return "COMPLETED".equals(payment.getStatus()) ||
                "PARTIALLY_REFUNDED".equals(payment.getStatus());
    }

    /**
     * Calculate refundable amount
     */
    public BigDecimal getRefundableAmount(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElse(null);

        if (payment == null || !isRefundable(paymentId)) {
            return BigDecimal.ZERO;
        }

        BigDecimal refunded = payment.getRefundAmount() != null ?
                payment.getRefundAmount() : BigDecimal.ZERO;

        return payment.getAmount().subtract(refunded);
    }

    /**
     * Retry failed payment
     */
    @Transactional
    public Payment retryPayment(Long paymentId) {
        log.info("Retrying payment: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException("Payment not found: " + paymentId));

        if (!"FAILED".equals(payment.getStatus())) {
            throw new PaymentException("Can only retry failed payments");
        }

        if (payment.getRetryCount() >= 3) {
            throw new PaymentException("Maximum retry attempts exceeded");
        }

        payment.incrementRetryCount();
        payment.setStatus("PENDING");
        payment.setUpdatedAt(LocalDateTime.now());
        payment.setLastRetryDate(LocalDateTime.now());

        paymentRepository.save(payment);

        // Create new payment request
        PaymentRequestDTO request = new PaymentRequestDTO();
        request.setAmount(payment.getAmount());
        request.setCurrency(payment.getCurrency());
        request.setPaymentMethod(payment.getPaymentMethod());
        request.setOrderId(payment.getOrder().getOrderId());

        // Process payment again
        return processPayment(payment.getOrder(), request);
    }

    /**
     * Cancel pending payment
     */
    @Transactional
    public Payment cancelPayment(Long paymentId, String reason) {
        log.info("Cancelling payment: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException("Payment not found: " + paymentId));

        if ("COMPLETED".equals(payment.getStatus())) {
            throw new PaymentException("Cannot cancel completed payment");
        }

        payment.setStatus("CANCELLED");
        payment.setNotes(reason);
        payment.setUpdatedAt(LocalDateTime.now());

        return paymentRepository.save(payment);
    }

    /**
     * Generate unique transaction ID
     */
    private String generateTransactionId() {
        return "TXN-" + System.currentTimeMillis() + "-" +
                UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Get payment summary for reporting
     */
    public Map<String, Object> getPaymentSummary(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> summary = new HashMap<>();

        BigDecimal totalCompleted = paymentRepository.calculateTotalAmountByStatusAndDateRange(
                "COMPLETED", startDate, endDate);
        BigDecimal totalRefunded = paymentRepository.calculateTotalRefundAmountByDateRange(
                startDate, endDate);
        Long completedCount = paymentRepository.countByStatusAndCreatedAtBetween(
                "COMPLETED", startDate, endDate);

        summary.put("totalRevenue", totalCompleted != null ? totalCompleted : BigDecimal.ZERO);
        summary.put("totalRefunded", totalRefunded != null ? totalRefunded : BigDecimal.ZERO);
        summary.put("netRevenue", totalCompleted != null && totalRefunded != null ?
                totalCompleted.subtract(totalRefunded) : BigDecimal.ZERO);
        summary.put("transactionCount", completedCount);
        summary.put("period", Map.of("start", startDate, "end", endDate));

        return summary;
    }
}