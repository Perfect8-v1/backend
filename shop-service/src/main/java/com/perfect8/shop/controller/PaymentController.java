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
 * 
 * UPDATED (2025-12-28 - FAS 3):
 * - Removed JwtTokenProvider dependency (Gateway handles JWT)
 * - Removed helper methods (getCurrentCustomerId, hasRole)
 * - Reads customer info directly from Gateway headers (X-Auth-User, X-Auth-Customer-Id, X-Auth-Role)
 * - Gateway validates JWT and adds headers before request reaches this service
 * 
 * Gateway Headers:
 * - X-Auth-User: username/email
 * - X-Auth-Customer-Id: customer ID (for customer logins)
 * - X-Auth-Role: user role (ROLE_ADMIN, ROLE_CUSTOMER, etc.)
 * 
 * Essential endpoints for v1.0:
 * - Process payment for order
 * - Get payment status
 * - Process refunds
 * - Retry failed payments
 * - Cancel pending payments
 * 
 * Magnum Opus Principles:
 * - Trust Gateway for authentication (single point of JWT validation)
 * - Read headers directly - no helper methods
 * - Clear variable names
 */
@Slf4j
@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderService orderService;

    /**
     * Process payment for an order
     */
    @PostMapping("/order/{orderId}/process")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('USER')")
    public ResponseEntity<ApiResponse<Payment>> processPayment(
            @PathVariable Long orderId,
            @Valid @RequestBody PaymentRequestDTO paymentRequest,
            HttpServletRequest request) {
        try {
            // Read customer ID from Gateway header
            String customerIdHeader = request.getHeader("X-Auth-Customer-Id");
            String role = request.getHeader("X-Auth-Role");
            
            Long customerId = null;
            if (customerIdHeader != null) {
                customerId = Long.parseLong(customerIdHeader);
            }

            // Get and validate order
            Order order = orderService.getOrderById(orderId);

            // Verify customer owns this order (unless admin)
            if (customerId != null && !order.getCustomer().getCustomerId().equals(customerId) 
                && !"ROLE_ADMIN".equalsIgnoreCase(role)) {
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
     * Create payment record for order
     */
    @PostMapping("/order/{orderId}/create")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('USER')")
    public ResponseEntity<ApiResponse<Payment>> createPayment(
            @PathVariable Long orderId,
            @RequestBody Map<String, Object> paymentDetails,
            HttpServletRequest request) {
        try {
            // Read customer ID from Gateway header
            String customerIdHeader = request.getHeader("X-Auth-Customer-Id");
            String role = request.getHeader("X-Auth-Role");
            
            Long customerId = null;
            if (customerIdHeader != null) {
                customerId = Long.parseLong(customerIdHeader);
            }

            // Get and validate order
            Order order = orderService.getOrderById(orderId);

            // Verify customer owns this order (unless admin)
            if (customerId != null && !order.getCustomer().getCustomerId().equals(customerId) 
                && !"ROLE_ADMIN".equalsIgnoreCase(role)) {
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
     * Complete payment
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
     * Get payment for order
     */
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Payment>> getPaymentByOrderId(
            @PathVariable Long orderId,
            HttpServletRequest request) {
        try {
            // Read customer ID from Gateway header
            String customerIdHeader = request.getHeader("X-Auth-Customer-Id");
            String role = request.getHeader("X-Auth-Role");
            
            Long customerId = null;
            if (customerIdHeader != null) {
                customerId = Long.parseLong(customerIdHeader);
            }

            // Get order to verify ownership
            Order order = orderService.getOrderById(orderId);

            // Check authorization
            if (!"ROLE_ADMIN".equalsIgnoreCase(role)) {
                if (customerId == null || !order.getCustomer().getCustomerId().equals(customerId)) {
                    throw new UnauthorizedAccessException("You are not authorized to view payments for this order");
                }
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
     * Get all payments for order
     */
    @GetMapping("/order/{orderId}/all")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Payment>>> getPaymentsByOrderId(
            @PathVariable Long orderId,
            HttpServletRequest request) {
        try {
            // Read customer ID from Gateway header
            String customerIdHeader = request.getHeader("X-Auth-Customer-Id");
            String role = request.getHeader("X-Auth-Role");
            
            Long customerId = null;
            if (customerIdHeader != null) {
                customerId = Long.parseLong(customerIdHeader);
            }

            // Get order to verify ownership
            Order order = orderService.getOrderById(orderId);

            // Check authorization
            if (!"ROLE_ADMIN".equalsIgnoreCase(role)) {
                if (customerId == null || !order.getCustomer().getCustomerId().equals(customerId)) {
                    throw new UnauthorizedAccessException("You are not authorized to view payments for this order");
                }
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
     * Process refund
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
     * Check if payment is refundable
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
     * Verify payment status with provider
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
     * Retry failed payment
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
     * Cancel payment
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
}
