package com.perfect8.shop.controller;

import com.perfect8.shop.dto.*;
import com.perfect8.shop.entity.Order;
import com.perfect8.common.enums.OrderStatus;
import com.perfect8.shop.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for order management.
 * Version 1.0 - Core functionality only
 *
 * This controller handles all critical order operations:
 * - Order creation and retrieval
 * - Status management (full order lifecycle)
 * - Payment confirmation
 * - Shipping and delivery tracking
 * - Returns and cancellations
 * - Basic operational oversight
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    /**
     * Create a new order - Core functionality
     * Public endpoint for customers to place orders
     */
    @PostMapping
    public ResponseEntity<ApiResponse<OrderDTO>> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {
        log.info("Creating new order for customer: {}", request.getCustomerId());

        Order createdOrder = orderService.createOrder(request);
        OrderDTO orderDTO = orderService.convertToDTO(createdOrder);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<OrderDTO>builder()
                        .success(true)
                        .message("Order created successfully")
                        .data(orderDTO)
                        .build());
    }

    /**
     * Get order by ID - Core functionality
     * Customers can view their own orders
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrderById(@PathVariable Long orderId) {
        log.info("Fetching order with ID: {}", orderId);

        Order order = orderService.getOrderById(orderId);
        OrderDTO orderDTO = orderService.convertToDTO(order);

        return ResponseEntity.ok(ApiResponse.<OrderDTO>builder()
                .success(true)
                .data(orderDTO)
                .build());
    }

    /**
     * Get order by order number - Core functionality
     * Alternative lookup method for customer service
     */
    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrderByNumber(@PathVariable String orderNumber) {
        log.info("Fetching order with number: {}", orderNumber);

        Order order = orderService.getOrderByNumber(orderNumber);
        OrderDTO orderDTO = orderService.convertToDTO(order);

        return ResponseEntity.ok(ApiResponse.<OrderDTO>builder()
                .success(true)
                .data(orderDTO)
                .build());
    }

    /**
     * Get all orders with pagination - Core functionality
     * Admin-only for order management
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<OrderDTO>>> getAllOrders(Pageable pageable) {
        log.info("Fetching all orders, page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<OrderDTO> orders = orderService.getAllOrders(pageable);

        return ResponseEntity.ok(ApiResponse.<Page<OrderDTO>>builder()
                .success(true)
                .data(orders)
                .build());
    }

    /**
     * Get orders for a specific customer - Core functionality
     * Customers can view their order history
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<Page<OrderDTO>>> getCustomerOrders(
            @PathVariable Long customerId,
            Pageable pageable) {
        log.info("Fetching orders for customer: {}", customerId);

        Page<OrderDTO> orders = orderService.getOrdersByCustomerId(customerId, pageable);

        return ResponseEntity.ok(ApiResponse.<Page<OrderDTO>>builder()
                .success(true)
                .data(orders)
                .build());
    }

    /**
     * Update order status - Core functionality
     * Admin-only for order lifecycle management
     */
    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderDTO>> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderStatusUpdateDTO request) {
        log.info("Updating status for order: {} to: {}", orderId, request.getStatus());

        // Parse status from string
        OrderStatus newStatus = OrderStatus.valueOf(request.getStatus().toUpperCase());
        Order order = orderService.updateOrderStatus(orderId, newStatus, request.getNotes());

        OrderDTO orderDTO = orderService.convertToDTO(order);

        return ResponseEntity.ok(ApiResponse.<OrderDTO>builder()
                .success(true)
                .message("Order status updated successfully")
                .data(orderDTO)
                .build());
    }

    /**
     * Cancel an order - Core functionality
     * Critical for customer service
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderDTO>> cancelOrder(
            @PathVariable Long orderId,
            @RequestParam(required = false) String reason) {
        log.info("Cancelling order: {}", orderId);

        Order order = orderService.cancelOrder(orderId, reason);
        OrderDTO orderDTO = orderService.convertToDTO(order);

        return ResponseEntity.ok(ApiResponse.<OrderDTO>builder()
                .success(true)
                .message("Order cancelled successfully")
                .data(orderDTO)
                .build());
    }

    /**
     * Process return for an order - Core functionality
     * Critical for customer service
     */
    @PostMapping("/{orderId}/return")
    public ResponseEntity<ApiResponse<OrderDTO>> returnOrder(
            @PathVariable Long orderId,
            @RequestParam String reason) {
        log.info("Processing return for order: {}", orderId);

        Order order = orderService.processReturn(orderId, reason);
        OrderDTO orderDTO = orderService.convertToDTO(order);

        return ResponseEntity.ok(ApiResponse.<OrderDTO>builder()
                .success(true)
                .message("Return processed successfully")
                .data(orderDTO)
                .build());
    }

    /**
     * Get orders by status - Core functionality
     * Admin-only for operational management
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getOrdersByStatus(
            @PathVariable OrderStatus status) {
        log.info("Fetching orders with status: {}", status);

        List<OrderDTO> orders = orderService.getOrdersByStatus(status);

        return ResponseEntity.ok(ApiResponse.<List<OrderDTO>>builder()
                .success(true)
                .data(orders)
                .build());
    }

    /**
     * Get recent orders - Core functionality
     * Admin-only for dashboard overview
     */
    @GetMapping("/recent")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getRecentOrders(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("Fetching {} recent orders", limit);

        List<OrderDTO> orders = orderService.getRecentOrders(limit);

        return ResponseEntity.ok(ApiResponse.<List<OrderDTO>>builder()
                .success(true)
                .data(orders)
                .build());
    }

    /**
     * Search orders - Core functionality
     * Admin-only for customer service
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<OrderDTO>>> searchOrders(
            @RequestParam String query,
            Pageable pageable) {
        log.info("Searching orders with query: {}", query);

        Page<OrderDTO> orders = orderService.searchOrders(query, pageable);

        return ResponseEntity.ok(ApiResponse.<Page<OrderDTO>>builder()
                .success(true)
                .data(orders)
                .build());
    }

    /**
     * Confirm payment for an order - Core functionality
     * Admin-only for payment processing
     */
    @PostMapping("/{orderId}/confirm-payment")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderDTO>> confirmPayment(
            @PathVariable Long orderId,
            @RequestBody Map<String, Object> paymentDetails) {
        log.info("Confirming payment for order: {}", orderId);

        Order order = orderService.confirmPayment(orderId, paymentDetails);
        OrderDTO orderDTO = orderService.convertToDTO(order);

        return ResponseEntity.ok(ApiResponse.<OrderDTO>builder()
                .success(true)
                .message("Payment confirmed successfully")
                .data(orderDTO)
                .build());
    }

    /**
     * Mark order as shipped - Core functionality
     * Admin-only for fulfillment
     */
    @PostMapping("/{orderId}/ship")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderDTO>> shipOrder(
            @PathVariable Long orderId,
            @RequestParam String trackingNumber,
            @RequestParam String carrier) {
        log.info("Marking order {} as shipped", orderId);

        Order order = orderService.markAsShipped(orderId, trackingNumber, carrier);
        OrderDTO orderDTO = orderService.convertToDTO(order);

        return ResponseEntity.ok(ApiResponse.<OrderDTO>builder()
                .success(true)
                .message("Order marked as shipped")
                .data(orderDTO)
                .build());
    }

    /**
     * Mark order as delivered - Core functionality
     * Admin-only to complete order lifecycle
     */
    @PostMapping("/{orderId}/deliver")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderDTO>> deliverOrder(
            @PathVariable Long orderId,
            @RequestParam(required = false) String deliveryNotes) {
        log.info("Marking order {} as delivered", orderId);

        Order order = orderService.markAsDelivered(orderId, deliveryNotes);
        OrderDTO orderDTO = orderService.convertToDTO(order);

        return ResponseEntity.ok(ApiResponse.<OrderDTO>builder()
                .success(true)
                .message("Order marked as delivered")
                .data(orderDTO)
                .build());
    }

    /**
     * Get orders that need attention - Core functionality
     * Admin-only for daily operations
     */
    @GetMapping("/attention-needed")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getOrdersNeedingAttention() {
        log.info("Fetching orders needing attention");

        List<OrderDTO> orders = orderService.getOrdersNeedingAttention();

        return ResponseEntity.ok(ApiResponse.<List<OrderDTO>>builder()
                .success(true)
                .data(orders)
                .build());
    }

    /**
     * Get order count by status - Core functionality
     * Admin-only for operational dashboard
     */
    @GetMapping("/count-by-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<OrderStatus, Long>>> getOrderCountByStatus() {
        log.info("Fetching order count by status");

        Map<OrderStatus, Long> counts = orderService.getOrderCountByStatus();

        return ResponseEntity.ok(ApiResponse.<Map<OrderStatus, Long>>builder()
                .success(true)
                .data(counts)
                .build());
    }

    /* ============================================
     * VERSION 2.0 ENDPOINTS - To be added later
     * ============================================
     * Future endpoints for version 2.0:
     * - /api/v1/orders/analytics - Advanced order analytics
     * - /api/v1/orders/export - Export orders to CSV/Excel
     * - /api/v1/orders/bulk-update - Bulk status updates
     * - /api/v1/orders/revenue-report - Revenue analytics
     * - /api/v1/orders/performance-metrics - Fulfillment metrics
     * - /api/v1/orders/customer-lifetime-value - CLV calculations
     * - /api/v1/orders/subscription - Subscription order management
     * - /api/v1/orders/recurring - Recurring order handling
     */
}