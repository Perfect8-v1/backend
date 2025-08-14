package com.perfect8.shop.controller;

import com.perfect8.shop.model.Order;
import com.perfect8.shop.model.OrderStatus;
import com.perfect8.shop.service.OrderService;
import com.perfect8.shop.service.PaymentService;
import com.perfect8.shop.service.ShipmentService;
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
 * REST Controller for Order management
 *
 * Customer Endpoints:
 * GET    /api/shop/orders/my            - Get customer's orders
 * GET    /api/shop/orders/{id}          - Get order details
 * POST   /api/shop/orders               - Create new order
 * POST   /api/shop/orders/{id}/cancel   - Cancel order
 *
 * Admin Endpoints:
 * GET    /api/shop/orders               - Get all orders (Admin)
 * PUT    /api/shop/orders/{id}/status   - Update order status (Admin)
 * GET    /api/shop/orders/stats         - Get order statistics (Admin)
 */
@RestController
@RequestMapping("/api/shop/orders")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080", "http://localhost:8081"})
public class OrderController {

    private final OrderService orderService;
    private final PaymentService paymentService;
    private final ShipmentService shipmentService;

    @Autowired
    public OrderController(OrderService orderService, PaymentService paymentService, ShipmentService shipmentService) {
        this.orderService = orderService;
        this.paymentService = paymentService;
        this.shipmentService = shipmentService;
    }

    /**
     * Get customer's orders
     *
     * @param authentication Customer authentication
     * @param pageable Pagination parameters
     * @param status Optional status filter
     * @return Customer's orders
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Page<OrderSummaryResponse>> getCustomerOrders(
            Authentication authentication,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable,
            @RequestParam(required = false) OrderStatus status) {

        String customerEmail = authentication.getName();
        Page<OrderSummaryResponse> orders = orderService.findCustomerOrders(customerEmail, pageable, status);
        return ResponseEntity.ok(orders);
    }

    /**
     * Get all orders (Admin only)
     *
     * @param pageable Pagination parameters
     * @param status Optional status filter
     * @param customerEmail Optional customer filter
     * @param startDate Optional start date filter
     * @param endDate Optional end date filter
     * @return All orders with filters
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrderSummaryResponse>> getAllOrders(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String customerEmail,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {

        Page<OrderSummaryResponse> orders = orderService.findOrders(
                pageable, status, customerEmail, startDate, endDate);
        return ResponseEntity.ok(orders);
    }

    /**
     * Get order by ID
     *
     * @param id Order ID
     * @param authentication User authentication
     * @return Order details
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<OrderDetailResponse> getOrderById(
            @PathVariable Long id,
            Authentication authentication) {

        OrderDetailResponse order = orderService.findOrderById(id, authentication);
        return ResponseEntity.ok(order);
    }

    /**
     * Get order by order number
     *
     * @param orderNumber Order number
     * @param authentication User authentication
     * @return Order details
     */
    @GetMapping("/number/{orderNumber}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<OrderDetailResponse> getOrderByNumber(
            @PathVariable String orderNumber,
            Authentication authentication) {

        OrderDetailResponse order = orderService.findOrderByNumber(orderNumber, authentication);
        return ResponseEntity.ok(order);
    }

    /**
     * Create new order
     *
     * @param request Order creation request
     * @param authentication Customer authentication
     * @return Created order with payment details
     */
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<OrderCreationResponse> createOrder(
            @Valid @RequestBody OrderCreateRequest request,
            Authentication authentication) {

        String customerEmail = authentication.getName();
        OrderCreationResponse response = orderService.createOrder(request, customerEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Calculate order total (before creating order)
     *
     * @param request Order calculation request
     * @return Order total calculation
     */
    @PostMapping("/calculate")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<OrderCalculationResponse> calculateOrder(
            @Valid @RequestBody OrderCalculationRequest request) {

        OrderCalculationResponse calculation = orderService.calculateOrder(request);
        return ResponseEntity.ok(calculation);
    }

    /**
     * Update order status (Admin only)
     *
     * @param id Order ID
     * @param request Status update request
     * @return Updated order
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderDetailResponse> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateRequest request) {

        OrderDetailResponse order = orderService.updateOrderStatus(id, request);
        return ResponseEntity.ok(order);
    }

    /**
     * Cancel order
     *
     * @param id Order ID
     * @param authentication User authentication
     * @param request Cancellation request with reason
     * @return Cancelled order
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<OrderDetailResponse> cancelOrder(
            @PathVariable Long id,
            Authentication authentication,
            @Valid @RequestBody OrderCancellationRequest request) {

        OrderDetailResponse order = orderService.cancelOrder(id, authentication, request);
        return ResponseEntity.ok(order);
    }

    /**
     * Confirm order (transition from PENDING to CONFIRMED)
     *
     * @param id Order ID
     * @return Confirmed order
     */
    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderDetailResponse> confirmOrder(@PathVariable Long id) {
        OrderDetailResponse order = orderService.confirmOrder(id);
        return ResponseEntity.ok(order);
    }

    /**
     * Ship order (transition to SHIPPED)
     *
     * @param id Order ID
     * @param request Shipping details
     * @return Shipped order
     */
    @PostMapping("/{id}/ship")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderDetailResponse> shipOrder(
            @PathVariable Long id,
            @Valid @RequestBody OrderShippingRequest request) {

        OrderDetailResponse order = orderService.shipOrder(id, request);
        return ResponseEntity.ok(order);
    }

    /**
     * Mark order as delivered
     *
     * @param id Order ID
     * @param request Delivery confirmation
     * @return Delivered order
     */
    @PostMapping("/{id}/deliver")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderDetailResponse> deliverOrder(
            @PathVariable Long id,
            @Valid @RequestBody OrderDeliveryRequest request) {

        OrderDetailResponse order = orderService.deliverOrder(id, request);
        return ResponseEntity.ok(order);
    }

    /**
     * Get order timeline (status history)
     *
     * @param id Order ID
     * @param authentication User authentication
     * @return Order status timeline
     */
    @GetMapping("/{id}/timeline")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<List<OrderTimelineEvent>> getOrderTimeline(
            @PathVariable Long id,
            Authentication authentication) {

        List<OrderTimelineEvent> timeline = orderService.getOrderTimeline(id, authentication);
        return ResponseEntity.ok(timeline);
    }

    /**
     * Get order statistics (Admin only)
     *
     * @param startDate Optional start date
     * @param endDate Optional end date
     * @return Order statistics
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderStatisticsResponse> getOrderStatistics(
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {

        OrderStatisticsResponse stats = orderService.getOrderStatistics(startDate, endDate);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get daily order metrics (Admin only)
     *
     * @param days Number of days back to analyze
     * @return Daily order metrics
     */
    @GetMapping("/metrics/daily")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DailyOrderMetrics>> getDailyOrderMetrics(
            @RequestParam(defaultValue = "30") Integer days) {

        List<DailyOrderMetrics> metrics = orderService.getDailyOrderMetrics(days);
        return ResponseEntity.ok(metrics);
    }

    /**
     * Get top selling products from orders (Admin only)
     *
     * @param limit Number of top products to return
     * @param startDate Optional start date
     * @param endDate Optional end date
     * @return Top selling products
     */
    @GetMapping("/top-products")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TopSellingProductResponse>> getTopSellingProducts(
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {

        List<TopSellingProductResponse> products = orderService.getTopSellingProducts(
                limit, startDate, endDate);
        return ResponseEntity.ok(products);
    }

    /**
     * Get order by tracking number
     *
     * @param trackingNumber Shipment tracking number
     * @return Order with shipping details
     */
    @GetMapping("/tracking/{trackingNumber}")
    public ResponseEntity<OrderTrackingResponse> getOrderByTracking(
            @PathVariable String trackingNumber) {

        OrderTrackingResponse tracking = orderService.getOrderByTracking(trackingNumber);
        return ResponseEntity.ok(tracking);
    }

    /**
     * Process refund for order (Admin only)
     *
     * @param id Order ID
     * @param request Refund request details
     * @return Refund response
     */
    @PostMapping("/{id}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RefundResponse> processRefund(
            @PathVariable Long id,
            @Valid @RequestBody RefundRequest request) {

        RefundResponse refund = orderService.processRefund(id, request);
        return ResponseEntity.ok(refund);
    }

    /**
     * Resend order confirmation email
     *
     * @param id Order ID
     * @param authentication User authentication
     * @return Success response
     */
    @PostMapping("/{id}/resend-confirmation")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> resendOrderConfirmation(
            @PathVariable Long id,
            Authentication authentication) {

        orderService.resendOrderConfirmation(id, authentication);
        return ResponseEntity.ok(Map.of("message", "Order confirmation email sent successfully"));
    }

    /**
     * Get recent orders for dashboard (Admin only)
     *
     * @param limit Number of recent orders
     * @return Recent orders
     */
    @GetMapping("/recent")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderSummaryResponse>> getRecentOrders(
            @RequestParam(defaultValue = "10") Integer limit) {

        List<OrderSummaryResponse> orders = orderService.getRecentOrders(limit);
        return ResponseEntity.ok(orders);
    }

    /**
     * Export orders to CSV (Admin only)
     *
     * @param startDate Optional start date
     * @param endDate Optional end date
     * @param status Optional status filter
     * @return CSV file response
     */
    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportOrders(
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam(required = false) OrderStatus status) {

        byte[] csvData = orderService.exportOrdersToCSV(startDate, endDate, status);

        return ResponseEntity.ok()
                .header("Content-Type", "text/csv")
                .header("Content-Disposition", "attachment; filename=orders.csv")
                .body(csvData);
    }

    // DTO Classes for responses
    public static class OrderTimelineEvent {
        private LocalDateTime timestamp;
        private OrderStatus status;
        private String title;
        private String description;
        private String actor; // Who performed the action

        // Constructors, getters, setters
        public OrderTimelineEvent() {}

        public OrderTimelineEvent(LocalDateTime timestamp, OrderStatus status, String title, String description, String actor) {
            this.timestamp = timestamp;
            this.status = status;
            this.title = title;
            this.description = description;
            this.actor = actor;
        }

        // Getters and setters
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        public OrderStatus getStatus() { return status; }
        public void setStatus(OrderStatus status) { this.status = status; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getActor() { return actor; }
        public void setActor(String actor) { this.actor = actor; }
    }

    public static class DailyOrderMetrics {
        private String date;
        private Integer orderCount;
        private BigDecimal totalRevenue;
        private Double averageOrderValue;
        private Integer cancelledOrders;

        // Constructors, getters, setters
        public DailyOrderMetrics() {}

        public DailyOrderMetrics(String date, Integer orderCount, BigDecimal totalRevenue, Double averageOrderValue, Integer cancelledOrders) {
            this.date = date;
            this.orderCount = orderCount;
            this.totalRevenue = totalRevenue;
            this.averageOrderValue = averageOrderValue;
            this.cancelledOrders = cancelledOrders;
        }

        // Getters and setters
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }

        public Integer getOrderCount() { return orderCount; }
        public void setOrderCount(Integer orderCount) { this.orderCount = orderCount; }

        public BigDecimal getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

        public Double getAverageOrderValue() { return averageOrderValue; }
        public void setAverageOrderValue(Double averageOrderValue) { this.averageOrderValue = averageOrderValue; }

        public Integer getCancelledOrders() { return cancelledOrders; }
        public void setCancelledOrders(Integer cancelledOrders) { this.cancelledOrders = cancelledOrders; }
    }

    public static class TopSellingProductResponse {
        private Long productId;
        private String productName;
        private Integer quantitySold;
        private BigDecimal totalRevenue;
        private Integer orderCount;

        // Constructors, getters, setters
        public TopSellingProductResponse() {}

        public TopSellingProductResponse(Long productId, String productName, Integer quantitySold, BigDecimal totalRevenue, Integer orderCount) {
            this.productId = productId;
            this.productName = productName;
            this.quantitySold = quantitySold;
            this.totalRevenue = totalRevenue;
            this.orderCount = orderCount;
        }

        // Getters and setters
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }

        public Integer getQuantitySold() { return quantitySold; }
        public void setQuantitySold(Integer quantitySold) { this.quantitySold = quantitySold; }

        public BigDecimal getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

        public Integer getOrderCount() { return orderCount; }
        public void setOrderCount(Integer orderCount) { this.orderCount = orderCount; }
    }
}