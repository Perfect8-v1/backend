package com.perfect8.shop.controller;

import com.perfect8.shop.entity.Order;
import com.perfect8.shop.entity.Payment;
import com.perfect8.shop.dto.ApiResponse;
import com.perfect8.shop.dto.PaymentRequestDTO;
import com.perfect8.shop.dto.RefundRequestDTO;
import com.perfect8.shop.service.PaymentService;
import com.perfect8.shop.service.OrderService;
import com.perfect8.shop.exception.UnauthorizedAccessException;
import com.perfect8.shop.exception.PaymentException;
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
import java.util.List;
import java.util.Map;

/**
 * Payment Controller - Version 1.0
 * Handles core payment operations for e-commerce
 *
 * Essential endpoints for v1.0:
 * - Process payment for order
 * - Get payment status
 * - Process refunds
 * - Retry failed payments
 * - Cancel pending payments
 *
 * FIXED: Helper method uses getCustomerIdFromToken() for consistency (Magnum Opus principle)
 */
@Slf4j
@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderService orderService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Process payment for an order - Core v1.0 functionality
     */
    @PostMapping("/order/{orderId}/process")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('USER')")
    public ResponseEntity<ApiResponse<Payment>> processPayment(
            @PathVariable Long orderId,
            @Valid @RequestBody PaymentRequestDTO paymentRequest,
            HttpServletRequest request) {
        try {
            Long customerId = getCurrentCustomerId(request);

            // Get and validate order
            Order order = orderService.getOrderById(orderId);

            // Verify customer owns this order
            if (!order.getCustomer().getCustomerId().equals(customerId) && !hasRole(request, "ADMIN")) {
                throw new UnauthorizedAccessException("You are not authorized to pay for this order");
            }

            // Ensure request has order ID
            paymentRequest.setOrderId(orderId);

            // Process the payment
            Payment processedPayment = paymentService.processPayment(order, paymentRequest);

            return ResponseEntity.ok(ApiResponse.success(
                    "Payment processing initiated",
                    processedPayment
            ));

        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error processing payment for order {}: {}", orderId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to process payment: " + e.getMessage()));
        }
    }

    /**
     * Create payment record for order - Core v1.0 functionality
     */
    @PostMapping("/order/{orderId}/create")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('USER')")
    public ResponseEntity<ApiResponse<Payment>> createPayment(
            @PathVariable Long orderId,
            @RequestBody Map<String, Object> paymentDetails,
            HttpServletRequest request) {
        try {
            Long customerId = getCurrentCustomerId(request);

            // Get and validate order
            Order order = orderService.getOrderById(orderId);

            // Verify customer owns this order
            if (!order.getCustomer().getCustomerId().equals(customerId) && !hasRole(request, "ADMIN")) {
                throw new UnauthorizedAccessException("You are not authorized to create payment for this order");
            }

            // Create payment
            Payment payment = paymentService.createPayment(order, paymentDetails);

            return ResponseEntity.ok(ApiResponse.success(
                    "Payment created successfully",
                    payment
            ));

        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating payment for order {}: {}", orderId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to create payment: " + e.getMessage()));
        }
    }

    /**
     * Complete payment - Core v1.0 functionality
     */
    @PostMapping("/{paymentId}/complete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Payment>> completePayment(
            @PathVariable Long paymentId,
            @RequestParam String gatewayTransactionId) {
        try {
            Payment completedPayment = paymentService.completePayment(paymentId, gatewayTransactionId);

            return ResponseEntity.ok(ApiResponse.success(
                    "Payment completed successfully",
                    completedPayment
            ));

        } catch (Exception e) {
            log.error("Error completing payment {}: {}", paymentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to complete payment: " + e.getMessage()));
        }
    }

    /**
     * Get payment for order - Core v1.0 functionality
     */
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Payment>> getPaymentByOrderId(
            @PathVariable Long orderId,
            HttpServletRequest request) {
        try {
            Long customerId = getCurrentCustomerId(request);

            // Get order to verify ownership
            Order order = orderService.getOrderById(orderId);

            // Check authorization
            if (!hasRole(request, "ADMIN") && !order.getCustomer().getCustomerId().equals(customerId)) {
                throw new UnauthorizedAccessException("You are not authorized to view payments for this order");
            }

            Payment payment = paymentService.getPaymentByOrderId(orderId);

            if (payment == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("No payment found for this order"));
            }

            return ResponseEntity.ok(ApiResponse.success(
                    "Payment retrieved successfully",
                    payment
            ));

        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error retrieving payment for order {}: {}", orderId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to retrieve payment"));
        }
    }

    /**
     * Get all payments for order - Core v1.0 functionality
     */
    @GetMapping("/order/{orderId}/all")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Payment>>> getPaymentsByOrderId(
            @PathVariable Long orderId,
            HttpServletRequest request) {
        try {
            Long customerId = getCurrentCustomerId(request);

            // Get order to verify ownership
            Order order = orderService.getOrderById(orderId);

            // Check authorization
            if (!hasRole(request, "ADMIN") && !order.getCustomer().getCustomerId().equals(customerId)) {
                throw new UnauthorizedAccessException("You are not authorized to view payments for this order");
            }

            List<Payment> payments = paymentService.getPaymentsByOrderId(orderId);

            return ResponseEntity.ok(ApiResponse.success(
                    "Payments retrieved successfully",
                    payments
            ));

        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error retrieving payments for order {}: {}", orderId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to retrieve payments"));
        }
    }

    /**
     * Process refund - Core v1.0 functionality
     * Critical for customer service
     */
    @PostMapping("/{paymentId}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Payment>> processRefund(
            @PathVariable Long paymentId,
            @Valid @RequestBody RefundRequestDTO refundRequest) {
        try {
            Payment refund = paymentService.processRefund(
                    paymentId,
                    refundRequest.getAmount(),
                    refundRequest.getReason() != null ? refundRequest.getReason() : "Customer requested refund"
            );

            return ResponseEntity.ok(ApiResponse.success(
                    "Refund processed successfully",
                    refund
            ));

        } catch (PaymentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error processing refund for payment {}: {}", paymentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to process refund: " + e.getMessage()));
        }
    }

    /**
     * Check if payment is refundable - Core v1.0 functionality
     */
    @GetMapping("/{paymentId}/refundable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkRefundable(
            @PathVariable Long paymentId) {
        try {
            boolean isRefundable = paymentService.isRefundable(paymentId);
            BigDecimal refundableAmount = paymentService.getRefundableAmount(paymentId);

            Map<String, Object> result = Map.of(
                    "isRefundable", isRefundable,
                    "refundableAmount", refundableAmount
            );

            return ResponseEntity.ok(ApiResponse.success(
                    "Refund status checked",
                    result
            ));

        } catch (Exception e) {
            log.error("Error checking refundable status for payment {}: {}", paymentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to check refund status"));
        }
    }

    /**
     * Verify payment status with provider - Core v1.0 functionality
     */
    @PostMapping("/{paymentId}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> verifyPaymentStatus(
            @PathVariable Long paymentId) {
        try {
            String status = paymentService.verifyPaymentStatus(paymentId);

            return ResponseEntity.ok(ApiResponse.success(
                    "Payment status verified",
                    status
            ));

        } catch (PaymentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error verifying payment {}: {}", paymentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to verify payment status"));
        }
    }

    /**
     * Retry failed payment - Core v1.0 functionality
     */
    @PostMapping("/{paymentId}/retry")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Payment>> retryPayment(
            @PathVariable Long paymentId) {
        try {
            Payment retriedPayment = paymentService.retryPayment(paymentId);

            return ResponseEntity.ok(ApiResponse.success(
                    "Payment retry initiated",
                    retriedPayment
            ));

        } catch (PaymentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error retrying payment {}: {}", paymentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retry payment"));
        }
    }

    /**
     * Cancel payment - Core v1.0 functionality
     */
    @PostMapping("/{paymentId}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Payment>> cancelPayment(
            @PathVariable Long paymentId,
            @RequestParam(required = false) String reason) {
        try {
            Payment cancelledPayment = paymentService.cancelPayment(
                    paymentId,
                    reason != null ? reason : "Payment cancelled by admin"
            );

            return ResponseEntity.ok(ApiResponse.success(
                    "Payment cancelled successfully",
                    cancelledPayment
            ));

        } catch (PaymentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error cancelling payment {}: {}", paymentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to cancel payment"));
        }
    }

    /* ============================================
     * VERSION 2.0 ENDPOINTS - Reserved for future
     * ============================================
     * These endpoints will be added in version 2.0:
     *
     * @GetMapping("/metrics")
     * - Payment metrics and analytics
     *
     * @GetMapping("/revenue")
     * - Revenue analytics and reporting
     *
     * @GetMapping("/issues")
     * - Payment issues dashboard
     *
     * @GetMapping("/status/{status}")
     * - Bulk payment queries by status
     *
     * @GetMapping("/date-range")
     * - Payment history by date range
     *
     * @GetMapping("/recent")
     * - Recent payments dashboard
     */

    // Helper methods

    /**
     * Get customer ID from JWT token
     * FIXED: Changed from getUserIdFromToken() to getCustomerIdFromToken() for consistency (Magnum Opus principle)
     */
    private Long getCurrentCustomerId(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            return jwtTokenProvider.getCustomerIdFromToken(token);
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