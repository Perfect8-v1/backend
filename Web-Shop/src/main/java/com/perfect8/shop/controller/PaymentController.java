package com.perfect8.shop.controller;

import com.perfect8.shop.model.PaymentMethod;
import com.perfect8.shop.model.PaymentStatus;
import com.perfect8.shop.service.PaymentService;
import com.perfect8.shop.service.PayPalService;
import com.perfect8.shop.service.OrderService;
import com.perfect8.shop.dto.*;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Payment processing
 *
 * Payment Endpoints:
 * POST   /api/shop/payments/initiate          - Initiate payment process
 * POST   /api/shop/payments/process           - Process payment
 * GET    /api/shop/payments/{id}              - Get payment details
 * POST   /api/shop/payments/{id}/cancel       - Cancel payment
 * POST   /api/shop/payments/{id}/refund       - Process refund (Admin)
 *
 * PayPal Endpoints:
 * POST   /api/shop/payments/paypal/create     - Create PayPal payment
 * POST   /api/shop/payments/paypal/execute    - Execute PayPal payment
 * GET    /api/shop/payments/paypal/success    - PayPal success callback
 * GET    /api/shop/payments/paypal/cancel     - PayPal cancel callback
 *
 * Admin Endpoints:
 * GET    /api/shop/payments                   - Get all payments (Admin)
 * GET    /api/shop/payments/stats             - Payment statistics (Admin)
 * POST   /api/shop/payments/bulk-refund       - Bulk refund processing (Admin)
 */
@RestController
@RequestMapping("/api/shop/payments")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080", "http://localhost:8081"})
public class PaymentController {

    private final PaymentService paymentService;
    private final PayPalService payPalService;
    private final OrderService orderService;

    @Autowired
    public PaymentController(PaymentService paymentService, PayPalService payPalService, OrderService orderService) {
        this.paymentService = paymentService;
        this.payPalService = payPalService;
        this.orderService = orderService;
    }

    // ============================================================================
    // PAYMENT PROCESSING ENDPOINTS
    // ============================================================================

    /**
     * Initiate payment process for an order
     *
     * @param request Payment initiation request
     * @param authentication Customer authentication
     * @return Payment initiation response with redirect URLs or next steps
     */
    @PostMapping("/initiate")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PaymentInitiationResponse> initiatePayment(
            @Valid @RequestBody PaymentInitiationRequest request,
            Authentication authentication) {

        String customerEmail = authentication.getName();
        PaymentInitiationResponse response = paymentService.initiatePayment(customerEmail, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Process payment (capture authorized payment)
     *
     * @param request Payment processing request
     * @param authentication Customer authentication
     * @return Payment processing result
     */
    @PostMapping("/process")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PaymentProcessingResponse> processPayment(
            @Valid @RequestBody PaymentProcessingRequest request,
            Authentication authentication) {

        String customerEmail = authentication.getName();
        PaymentProcessingResponse response = paymentService.processPayment(customerEmail, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get payment details by ID
     *
     * @param id Payment ID
     * @param authentication User authentication
     * @return Payment details
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<PaymentDetailResponse> getPaymentById(
            @PathVariable Long id,
            Authentication authentication) {

        PaymentDetailResponse payment = paymentService.getPaymentById(id, authentication);
        return ResponseEntity.ok(payment);
    }

    /**
     * Get payments for customer's orders
     *
     * @param authentication Customer authentication
     * @param pageable Pagination parameters
     * @param status Optional payment status filter
     * @return Customer's payments
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Page<PaymentSummaryResponse>> getCustomerPayments(
            Authentication authentication,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable,
            @RequestParam(required = false) PaymentStatus status) {

        String customerEmail = authentication.getName();
        Page<PaymentSummaryResponse> payments = paymentService.getCustomerPayments(
                customerEmail, pageable, status);
        return ResponseEntity.ok(payments);
    }

    /**
     * Cancel payment (if still in PENDING or AUTHORIZED state)
     *
     * @param id Payment ID
     * @param authentication User authentication
     * @param request Cancellation request with reason
     * @return Cancelled payment details
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<PaymentDetailResponse> cancelPayment(
            @PathVariable Long id,
            Authentication authentication,
            @Valid @RequestBody PaymentCancellationRequest request) {

        PaymentDetailResponse payment = paymentService.cancelPayment(id, authentication, request);
        return ResponseEntity.ok(payment);
    }

    /**
     * Get available payment methods for customer
     *
     * @param orderId Optional order ID for context
     * @param amount Optional amount for method filtering
     * @param currency Optional currency
     * @return Available payment methods
     */
    @GetMapping("/methods")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<PaymentMethodResponse>> getAvailablePaymentMethods(
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) BigDecimal amount,
            @RequestParam(defaultValue = "USD") String currency) {

        List<PaymentMethodResponse> methods = paymentService.getAvailablePaymentMethods(
                orderId, amount, currency);
        return ResponseEntity.ok(methods);
    }

    // ============================================================================
    // PAYPAL SPECIFIC ENDPOINTS
    // ============================================================================

    /**
     * Create PayPal payment
     *
     * @param request PayPal payment creation request
     * @param authentication Customer authentication
     * @return PayPal payment creation response with approval URL
     */
    @PostMapping("/paypal/create")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PayPalPaymentResponse> createPayPalPayment(
            @Valid @RequestBody PayPalPaymentRequest request,
            Authentication authentication) {

        String customerEmail = authentication.getName();
        PayPalPaymentResponse response = payPalService.createPayment(customerEmail, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Execute PayPal payment after customer approval
     *
     * @param request PayPal execution request with payment ID and payer ID
     * @param authentication Customer authentication
     * @return PayPal execution result
     */
    @PostMapping("/paypal/execute")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PayPalExecutionResponse> executePayPalPayment(
            @Valid @RequestBody PayPalExecutionRequest request,
            Authentication authentication) {

        String customerEmail = authentication.getName();
        PayPalExecutionResponse response = payPalService.executePayment(customerEmail, request);
        return ResponseEntity.ok(response);
    }

    /**
     * PayPal success callback (GET endpoint for redirect)
     *
     * @param paymentId PayPal payment ID
     * @param payerId PayPal payer ID
     * @param token PayPal token
     * @return Success page or redirect
     */
    @GetMapping("/paypal/success")
    public ResponseEntity<Map<String, String>> payPalSuccessCallback(
            @RequestParam String paymentId,
            @RequestParam String PayerID,
            @RequestParam String token) {

        // Log the successful callback
        payPalService.handleSuccessCallback(paymentId, PayerID, token);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "PayPal payment approved successfully",
                "paymentId", paymentId,
                "payerId", PayerID,
                "redirectUrl", "/checkout/complete?payment=" + paymentId
        ));
    }

    /**
     * PayPal cancel callback (GET endpoint for redirect)
     *
     * @param token PayPal token
     * @return Cancel page or redirect
     */
    @GetMapping("/paypal/cancel")
    public ResponseEntity<Map<String, String>> payPalCancelCallback(@RequestParam String token) {

        // Log the cancelled callback
        payPalService.handleCancelCallback(token);

        return ResponseEntity.ok(Map.of(
                "status", "cancelled",
                "message", "PayPal payment was cancelled",
                "redirectUrl", "/checkout/payment?error=cancelled"
        ));
    }

    /**
     * Get PayPal payment details
     *
     * @param paypalPaymentId PayPal payment ID
     * @param authentication User authentication
     * @return PayPal payment details
     */
    @GetMapping("/paypal/{paypalPaymentId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<PayPalPaymentDetailResponse> getPayPalPaymentDetails(
            @PathVariable String paypalPaymentId,
            Authentication authentication) {

        PayPalPaymentDetailResponse details = payPalService.getPaymentDetails(
                paypalPaymentId, authentication);
        return ResponseEntity.ok(details);
    }

    // ============================================================================
    // REFUND ENDPOINTS
    // ============================================================================

    /**
     * Process refund for a payment (Admin only)
     *
     * @param id Payment ID
     * @param request Refund processing request
     * @return Refund processing result
     */
    @PostMapping("/{id}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RefundResponse> processRefund(
            @PathVariable Long id,
            @Valid @RequestBody RefundRequest request) {

        RefundResponse response = paymentService.processRefund(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get refund details
     *
     * @param id Payment ID
     * @param refundId Refund ID
     * @param authentication User authentication
     * @return Refund details
     */
    @GetMapping("/{id}/refunds/{refundId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<RefundDetailResponse> getRefundDetails(
            @PathVariable Long id,
            @PathVariable Long refundId,
            Authentication authentication) {

        RefundDetailResponse refund = paymentService.getRefundDetails(id, refundId, authentication);
        return ResponseEntity.ok(refund);
    }

    /**
     * Get all refunds for a payment
     *
     * @param id Payment ID
     * @param authentication User authentication
     * @return List of refunds
     */
    @GetMapping("/{id}/refunds")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<List<RefundSummaryResponse>> getPaymentRefunds(
            @PathVariable Long id,
            Authentication authentication) {

        List<RefundSummaryResponse> refunds = paymentService.getPaymentRefunds(id, authentication);
        return ResponseEntity.ok(refunds);
    }

    // ============================================================================
    // ADMIN ENDPOINTS
    // ============================================================================

    /**
     * Get all payments with filtering (Admin only)
     *
     * @param pageable Pagination parameters
     * @param status Optional payment status filter
     * @param method Optional payment method filter
     * @param startDate Optional start date filter
     * @param endDate Optional end date filter
     * @param minAmount Optional minimum amount filter
     * @param maxAmount Optional maximum amount filter
     * @return Filtered payments
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<PaymentAdminResponse>> getAllPayments(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) PaymentMethod method,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount) {

        Page<PaymentAdminResponse> payments = paymentService.getAllPayments(
                pageable, status, method, startDate, endDate, minAmount, maxAmount);
        return ResponseEntity.ok(payments);
    }

    /**
     * Get payment statistics (Admin only)
     *
     * @param startDate Optional start date filter
     * @param endDate Optional end date filter
     * @return Payment statistics
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentStatisticsResponse> getPaymentStatistics(
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {

        PaymentStatisticsResponse stats = paymentService.getPaymentStatistics(startDate, endDate);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get daily payment metrics (Admin only)
     *
     * @param days Number of days back to analyze
     * @return Daily payment metrics
     */
    @GetMapping("/metrics/daily")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DailyPaymentMetrics>> getDailyPaymentMetrics(
            @RequestParam(defaultValue = "30") Integer days) {

        List<DailyPaymentMetrics> metrics = paymentService.getDailyPaymentMetrics(days);
        return ResponseEntity.ok(metrics);
    }

    /**
     * Get payment method distribution (Admin only)
     *
     * @param startDate Optional start date filter
     * @param endDate Optional end date filter
     * @return Payment method statistics
     */
    @GetMapping("/methods/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaymentMethodStatsResponse>> getPaymentMethodStats(
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {

        List<PaymentMethodStatsResponse> stats = paymentService.getPaymentMethodStats(startDate, endDate);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get failed payments for analysis (Admin only)
     *
     * @param pageable Pagination parameters
     * @param startDate Optional start date filter
     * @param endDate Optional end date filter
     * @return Failed payments
     */
    @GetMapping("/failed")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<FailedPaymentResponse>> getFailedPayments(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {

        Page<FailedPaymentResponse> failedPayments = paymentService.getFailedPayments(
                pageable, startDate, endDate);
        return ResponseEntity.ok(failedPayments);
    }

    /**
     * Retry failed payment (Admin only)
     *
     * @param id Payment ID
     * @param request Retry request with optional new payment method
     * @return Retry result
     */
    @PostMapping("/{id}/retry")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentRetryResponse> retryPayment(
            @PathVariable Long id,
            @Valid @RequestBody PaymentRetryRequest request) {

        PaymentRetryResponse response = paymentService.retryPayment(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Bulk refund processing (Admin only)
     *
     * @param request Bulk refund request with payment IDs
     * @return Bulk refund processing result
     */
    @PostMapping("/bulk-refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BulkRefundResponse> processBulkRefund(
            @Valid @RequestBody BulkRefundRequest request) {

        BulkRefundResponse response = paymentService.processBulkRefund(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Export payments to CSV (Admin only)
     *
     * @param status Optional payment status filter
     * @param startDate Optional start date filter
     * @param endDate Optional end date filter
     * @return CSV file response
     */
    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportPayments(
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {

        byte[] csvData = paymentService.exportPaymentsToCSV(status, startDate, endDate);

        return ResponseEntity.ok()
                .header("Content-Type", "text/csv")
                .header("Content-Disposition", "attachment; filename=payments.csv")
                .body(csvData);
    }

    // DTO Classes for responses
    public static class DailyPaymentMetrics {
        private String date;
        private Integer paymentCount;
        private BigDecimal totalAmount;
        private Integer successfulPayments;
        private Integer failedPayments;
        private Double successRate;
        private BigDecimal averageAmount;

        // Constructors, getters, setters
        public DailyPaymentMetrics() {}

        public DailyPaymentMetrics(String date, Integer paymentCount, BigDecimal totalAmount,
                                   Integer successfulPayments, Integer failedPayments, Double successRate,
                                   BigDecimal averageAmount) {
            this.date = date;
            this.paymentCount = paymentCount;
            this.totalAmount = totalAmount;
            this.successfulPayments = successfulPayments;
            this.failedPayments = failedPayments;
            this.successRate = successRate;
            this.averageAmount = averageAmount;
        }

        // Getters and setters
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }

        public Integer getPaymentCount() { return paymentCount; }
        public void setPaymentCount(Integer paymentCount) { this.paymentCount = paymentCount; }

        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

        public Integer getSuccessfulPayments() { return successfulPayments; }
        public void setSuccessfulPayments(Integer successfulPayments) { this.successfulPayments = successfulPayments; }

        public Integer getFailedPayments() { return failedPayments; }
        public void setFailedPayments(Integer failedPayments) { this.failedPayments = failedPayments; }

        public Double getSuccessRate() { return successRate; }
        public void setSuccessRate(Double successRate) { this.successRate = successRate; }

        public BigDecimal getAverageAmount() { return averageAmount; }
        public void setAverageAmount(BigDecimal averageAmount) { this.averageAmount = averageAmount; }
    }

    public static class PaymentMethodStatsResponse {
        private PaymentMethod method;
        private String methodName;
        private Integer paymentCount;
        private BigDecimal totalAmount;
        private Double percentage;
        private Double successRate;
        private BigDecimal averageAmount;

        // Constructors, getters, setters
        public PaymentMethodStatsResponse() {}

        public PaymentMethodStatsResponse(PaymentMethod method, String methodName, Integer paymentCount,
                                          BigDecimal totalAmount, Double percentage, Double successRate,
                                          BigDecimal averageAmount) {
            this.method = method;
            this.methodName = methodName;
            this.paymentCount = paymentCount;
            this.totalAmount = totalAmount;
            this.percentage = percentage;
            this.successRate = successRate;
            this.averageAmount = averageAmount;
        }

        // Getters and setters
        public PaymentMethod getMethod() { return method; }
        public void setMethod(PaymentMethod method) { this.method = method; }

        public String getMethodName() { return methodName; }
        public void setMethodName(String methodName) { this.methodName = methodName; }

        public Integer getPaymentCount() { return paymentCount; }
        public void setPaymentCount(Integer paymentCount) { this.paymentCount = paymentCount; }

        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

        public Double getPercentage() { return percentage; }
        public void setPercentage(Double percentage) { this.percentage = percentage; }

        public Double getSuccessRate() { return successRate; }
        public void setSuccessRate(Double successRate) { this.successRate = successRate; }

        public BigDecimal getAverageAmount() { return averageAmount; }
        public void setAverageAmount(BigDecimal averageAmount) { this.averageAmount = averageAmount; }
    }
}