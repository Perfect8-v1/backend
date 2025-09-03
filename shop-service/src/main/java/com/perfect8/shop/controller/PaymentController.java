package com.perfect8.shop.controller;

import com.perfect8.shop.entity.Payment;
import com.perfect8.shop.dto.ApiResponse;
import com.perfect8.shop.dto.PaymentRequestDTO;
import com.perfect8.shop.dto.PaymentResponseDTO;
import com.perfect8.shop.dto.RefundRequestDTO;
import com.perfect8.shop.service.PaymentService;
import com.perfect8.shop.exception.UnauthorizedAccessException;
import com.perfect8.shop.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Payment Controller - Version 1.0
 * Core payment functionality only - no analytics or metrics
 */
@Slf4j
@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Process payment for an order
     */
    @PostMapping("/process")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('USER')")
    public ResponseEntity<ApiResponse<Payment>> processPayment(
            @Valid @RequestBody PaymentRequestDTO paymentRequest,
            HttpServletRequest request) {
        try {
            Long customerId = getCurrentCustomerId(request);

            // Create payment entity from DTO
            Payment payment = new Payment();
            payment.setAmount(paymentRequest.getAmount());
            payment.setPaymentMethod(Payment.PaymentMethod.valueOf(paymentRequest.getPaymentMethod()));
            payment.setStatus(Payment.PaymentStatus.PENDING);
            payment.setTransactionId(paymentRequest.getTransactionId());

            // Process the payment
            Payment processedPayment = paymentService.processPayment(payment);

            return ResponseEntity.ok(ApiResponse.success(
                    "Payment processed successfully",
                    processedPayment
            ));

        } catch (Exception e) {
            log.error("Error processing payment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to process payment: " + e.getMessage()));
        }
    }

    /**
     * Get payment by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Payment>> getPaymentById(
            @PathVariable Long id,
            HttpServletRequest request) {
        try {
            Payment payment = paymentService.findById(id);

            // Check authorization
            if (!hasRole(request, "ADMIN")) {
                Long customerId = getCurrentCustomerId(request);
                if (!payment.getCustomer().getId().equals(customerId)) {
                    throw UnauthorizedAccessException.paymentAccess(id, customerId.toString());
                }
            }

            return ResponseEntity.ok(ApiResponse.success(
                    "Payment retrieved successfully",
                    payment
            ));

        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getUserFriendlyMessage()));
        } catch (Exception e) {
            log.error("Error retrieving payment {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Payment not found"));
        }
    }

    /**
     * Get payments for an order
     */
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Payment>>> getPaymentsByOrder(
            @PathVariable Long orderId,
            HttpServletRequest request) {
        try {
            List<Payment> payments = paymentService.findByOrderId(orderId);

            return ResponseEntity.ok(ApiResponse.success(
                    "Payments retrieved successfully",
                    payments
            ));

        } catch (Exception e) {
            log.error("Error retrieving payments for order {}: {}", orderId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to retrieve payments"));
        }
    }

    /**
     * Get recent payments (Admin only)
     */
    @GetMapping("/recent")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Payment>>> getRecentPayments(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Payment> recentPayments = paymentService.getRecentPayments(limit);

            return ResponseEntity.ok(ApiResponse.success(
                    "Recent payments retrieved successfully",
                    recentPayments
            ));

        } catch (Exception e) {
            log.error("Error retrieving recent payments: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to retrieve recent payments"));
        }
    }

    /**
     * Process refund
     */
    @PostMapping("/{id}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Payment>> processRefund(
            @PathVariable Long id,
            @Valid @RequestBody RefundRequestDTO refundRequest) {
        try {
            Payment refund = paymentService.processRefund(id, refundRequest.getAmount());

            return ResponseEntity.ok(ApiResponse.success(
                    "Refund processed successfully",
                    refund
            ));

        } catch (Exception e) {
            log.error("Error processing refund for payment {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to process refund: " + e.getMessage()));
        }
    }

    /**
     * Cancel payment
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Payment>> cancelPayment(@PathVariable Long id) {
        try {
            Payment cancelledPayment = paymentService.cancelPayment(id);

            return ResponseEntity.ok(ApiResponse.success(
                    "Payment cancelled successfully",
                    cancelledPayment
            ));

        } catch (Exception e) {
            log.error("Error cancelling payment {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to cancel payment: " + e.getMessage()));
        }
    }

    /**
     * Update payment status (Admin only)
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Payment>> updatePaymentStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        try {
            Payment.PaymentStatus paymentStatus = Payment.PaymentStatus.valueOf(status.toUpperCase());
            Payment updatedPayment = paymentService.updatePaymentStatus(id, paymentStatus);

            return ResponseEntity.ok(ApiResponse.success(
                    "Payment status updated successfully",
                    updatedPayment
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Invalid payment status: " + status));
        } catch (Exception e) {
            log.error("Error updating payment status for {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to update payment status"));
        }
    }

    /**
     * Verify payment status with provider
     */
    @PostMapping("/{id}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Payment>> verifyPaymentStatus(@PathVariable Long id) {
        try {
            Payment payment = paymentService.verifyPaymentStatus(id);

            return ResponseEntity.ok(ApiResponse.success(
                    "Payment status verified",
                    payment
            ));

        } catch (Exception e) {
            log.error("Error verifying payment {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to verify payment status"));
        }
    }

    /**
     * Get payments by status (Admin only)
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Payment>>> getPaymentsByStatus(
            @PathVariable String status) {
        try {
            Payment.PaymentStatus paymentStatus = Payment.PaymentStatus.valueOf(status.toUpperCase());
            List<Payment> payments = paymentService.getPaymentsByStatus(paymentStatus);

            return ResponseEntity.ok(ApiResponse.success(
                    "Payments retrieved successfully",
                    payments
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Invalid payment status: " + status));
        } catch (Exception e) {
            log.error("Error retrieving payments by status {}: {}", status, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to retrieve payments"));
        }
    }

    /**
     * Get payments by date range (Admin only)
     */
    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Payment>>> getPaymentsByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);

            List<Payment> payments = paymentService.getPaymentsByDateRange(start, end);

            return ResponseEntity.ok(ApiResponse.success(
                    "Payments retrieved successfully",
                    payments
            ));

        } catch (Exception e) {
            log.error("Error retrieving payments by date range: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to retrieve payments"));
        }
    }

    /* VERSION 2.0 - ANALYTICS & METRICS FEATURES
    // Commented out for v1.0 - will be reimplemented in v2.0

    @GetMapping("/metrics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaymentMetricsResponse>> getPaymentMetrics(
            @RequestParam(defaultValue = "30") Integer days) {
        // Analytics feature - not needed for core payment functionality
    }

    @GetMapping("/issues")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<PaymentIssueResponse>>> getPaymentIssues(
            @RequestParam(defaultValue = "10") Integer limit) {
        // Dashboard feature - not needed for core payment functionality
    }

    @GetMapping("/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RevenueAnalyticsResponse>> getRevenueAnalytics(
            @RequestParam(defaultValue = "30") Integer days) {
        // Analytics feature - not needed for core payment functionality
    }
    */

    // Helper methods

    private Long getCurrentCustomerId(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            return jwtTokenProvider.getUserIdFromToken(token);
        }
        throw new RuntimeException("Unable to determine customer ID from token");
    }

    private boolean hasRole(HttpServletRequest request, String role) {
        String token = jwtTokenProvider.resolveToken(request);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            String userRole = jwtTokenProvider.getRoleFromToken(token);
            return role.equalsIgnoreCase(userRole);
        }
        return false;
    }
}