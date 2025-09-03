package com.perfect8.shop.controller;

import com.perfect8.shop.dto.*;
import com.perfect8.shop.entity.Order;
import com.perfect8.shop.enums.OrderStatus;
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
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    /**
     * Create a new order
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
     * Get order by ID
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
     * Get order by order number
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
     * Get all orders with pagination
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
     * Get orders for a specific customer
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
     * Update order status
     */
    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderDTO>> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderStatusUpdateDTO request) {
        log.info("Updating status for order: {} to: {}", orderId, request.getStatus());

        // Parse status from string - using getStatus() instead of getNewStatus()
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
     * Cancel an order
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
     * Process return for an order
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
     * Get orders by status (Admin only)
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
     * Get recent orders (Admin only)
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
     * Search orders
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
     * Confirm payment for an order
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
     * Mark order as shipped
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
     * Mark order as delivered
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
     * Get orders that need attention (pending, processing)
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
     * Get order count by status (for dashboard)
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
}