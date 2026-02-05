package com.perfect8.shop.controller;

import com.perfect8.common.enums.OrderStatus;
import com.perfect8.shop.dto.ApiResponse;
import com.perfect8.shop.dto.CreateOrderRequest;
import com.perfect8.shop.dto.OrderDTO;
import com.perfect8.shop.entity.Order;
import com.perfect8.shop.exception.UnauthorizedAccessException;
import com.perfect8.shop.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * REST Controller for Order operations.
 * 
 * SECURITY: All customer-facing endpoints verify ownership via X-Auth-Customer-Id header.
 * Admin endpoints are protected with @PreAuthorize("hasRole('ADMIN')").
 * 
 * @version 1.3.1 - Security fix for IDOR vulnerabilities
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Orders", description = "Order management endpoints")
public class OrderController {

    private final OrderService orderService;

    // ========== HELPER METHODS FOR SECURITY ==========

    /**
     * Extracts customerId from Gateway header.
     * Returns null if not present (for admin requests).
     */
    private Long getCustomerIdFromRequest(HttpServletRequest request) {
        String customerIdHeader = request.getHeader("X-Auth-Customer-Id");
        if (customerIdHeader != null && !customerIdHeader.isEmpty()) {
            try {
                return Long.parseLong(customerIdHeader);
            } catch (NumberFormatException e) {
                log.warn("Invalid X-Auth-Customer-Id header: {}", customerIdHeader);
                return null;
            }
        }
        return null;
    }

    /**
     * Checks if the request is from an admin user.
     */
    private boolean isAdminRequest(HttpServletRequest request) {
        String role = request.getHeader("X-Auth-Role");
        return role != null && (role.contains("ADMIN") || role.contains("SUPER_ADMIN"));
    }

    /**
     * Verifies that the order belongs to the requesting customer.
     * Throws UnauthorizedAccessException if not.
     * Admin users bypass this check.
     */
    private void verifyOrderOwnership(Order order, HttpServletRequest request) {
        if (isAdminRequest(request)) {
            log.debug("Admin access granted for order: {}", order.getOrderId());
            return;
        }

        Long customerId = getCustomerIdFromRequest(request);
        if (customerId == null) {
            throw new UnauthorizedAccessException("Customer ID not found in request");
        }

        if (!order.getCustomer().getCustomerId().equals(customerId)) {
            log.warn("Unauthorized access attempt: Customer {} tried to access order {} belonging to customer {}",
                    customerId, order.getOrderId(), order.getCustomer().getCustomerId());
            throw new UnauthorizedAccessException("You can only access your own orders");
        }
    }

    /**
     * Verifies that the customerId in URL matches the authenticated customer.
     */
    private void verifyCustomerIdMatch(Long urlCustomerId, HttpServletRequest request) {
        if (isAdminRequest(request)) {
            log.debug("Admin access granted for customer: {}", urlCustomerId);
            return;
        }

        Long authenticatedCustomerId = getCustomerIdFromRequest(request);
        if (authenticatedCustomerId == null) {
            throw new UnauthorizedAccessException("Customer ID not found in request");
        }

        if (!urlCustomerId.equals(authenticatedCustomerId)) {
            log.warn("Unauthorized access attempt: Customer {} tried to access data for customer {}",
                    authenticatedCustomerId, urlCustomerId);
            throw new UnauthorizedAccessException("You can only access your own orders");
        }
    }

    // ========== CUSTOMER ENDPOINTS (with ownership verification) ==========

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID", description = "Returns order details. Customers can only view their own orders.")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrderById(
            @PathVariable Long orderId,
            HttpServletRequest request) {
        
        log.debug("Getting order by ID: {}", orderId);
        
        Order order = orderService.getOrderById(orderId);
        
        // SECURITY: Verify ownership
        verifyOrderOwnership(order, request);
        
        OrderDTO orderDTO = orderService.convertToDTO(order);
        
        return ResponseEntity.ok(ApiResponse.<OrderDTO>builder()
                .success(true)
                .message("Order retrieved successfully")
                .data(orderDTO)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @GetMapping("/number/{orderNumber}")
    @Operation(summary = "Get order by order number", description = "Returns order details by order number. Customers can only view their own orders.")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrderByNumber(
            @PathVariable String orderNumber,
            HttpServletRequest request) {
        
        log.debug("Getting order by number: {}", orderNumber);
        
        Order order = orderService.getOrderByNumber(orderNumber);
        
        // SECURITY: Verify ownership
        verifyOrderOwnership(order, request);
        
        OrderDTO orderDTO = orderService.convertToDTO(order);
        
        return ResponseEntity.ok(ApiResponse.<OrderDTO>builder()
                .success(true)
                .message("Order retrieved successfully")
                .data(orderDTO)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @GetMapping("/my-orders")
    @Operation(summary = "Get my orders", description = "Returns all orders for the authenticated customer")
    public ResponseEntity<ApiResponse<Page<OrderDTO>>> getMyOrders(
            @PageableDefault(size = 10) Pageable pageable,
            HttpServletRequest request) {
        
        Long customerId = getCustomerIdFromRequest(request);
        if (customerId == null) {
            throw new UnauthorizedAccessException("Customer ID not found in request");
        }
        
        log.debug("Getting orders for authenticated customer: {}", customerId);
        
        Page<OrderDTO> orders = orderService.getOrdersByCustomerId(customerId, pageable);
        
        return ResponseEntity.ok(ApiResponse.<Page<OrderDTO>>builder()
                .success(true)
                .message("Orders retrieved successfully")
                .data(orders)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get orders by customer ID", description = "Returns all orders for a specific customer. Customers can only view their own orders.")
    public ResponseEntity<ApiResponse<Page<OrderDTO>>> getCustomerOrders(
            @PathVariable Long customerId,
            @PageableDefault(size = 10) Pageable pageable,
            HttpServletRequest request) {
        
        // SECURITY: Verify customer is accessing their own orders
        verifyCustomerIdMatch(customerId, request);
        
        log.debug("Getting orders for customer: {}", customerId);
        
        Page<OrderDTO> orders = orderService.getOrdersByCustomerId(customerId, pageable);
        
        return ResponseEntity.ok(ApiResponse.<Page<OrderDTO>>builder()
                .success(true)
                .message("Orders retrieved successfully")
                .data(orders)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PostMapping
    @Operation(summary = "Create order", description = "Creates a new order from the customer's cart")
    public ResponseEntity<ApiResponse<OrderDTO>> createOrder(
            @Valid @RequestBody CreateOrderRequest createRequest,
            HttpServletRequest request) {
        
        Long customerId = getCustomerIdFromRequest(request);
        if (customerId == null) {
            throw new UnauthorizedAccessException("Customer ID not found in request");
        }
        
        log.debug("Creating order for customer: {}", customerId);
        
        // SECURITY: Ensure the order is created for the authenticated customer
        // Set customerId in request to match authenticated user
        createRequest.setCustomerId(customerId);
        
        Order order = orderService.createOrder(createRequest);
        OrderDTO orderDTO = orderService.convertToDTO(order);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<OrderDTO>builder()
                        .success(true)
                        .message("Order created successfully")
                        .data(orderDTO)
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel order", description = "Cancels an order. Customers can only cancel their own orders.")
    public ResponseEntity<ApiResponse<OrderDTO>> cancelOrder(
            @PathVariable Long orderId,
            @RequestParam(required = false) String reason,
            HttpServletRequest request) {
        
        log.debug("Cancel request for order: {}", orderId);
        
        // SECURITY: First fetch order and verify ownership
        Order order = orderService.getOrderById(orderId);
        verifyOrderOwnership(order, request);
        
        // Now cancel it
        Order cancelledOrder = orderService.cancelOrder(orderId, reason);
        OrderDTO orderDTO = orderService.convertToDTO(cancelledOrder);
        
        return ResponseEntity.ok(ApiResponse.<OrderDTO>builder()
                .success(true)
                .message("Order cancelled successfully")
                .data(orderDTO)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PostMapping("/{orderId}/return")
    @Operation(summary = "Request return", description = "Initiates a return for an order. Customers can only return their own orders.")
    public ResponseEntity<ApiResponse<OrderDTO>> returnOrder(
            @PathVariable Long orderId,
            @RequestParam String reason,
            HttpServletRequest request) {
        
        log.debug("Return request for order: {}", orderId);
        
        // SECURITY: First fetch order and verify ownership
        Order order = orderService.getOrderById(orderId);
        verifyOrderOwnership(order, request);
        
        // Now process return
        Order returnedOrder = orderService.processReturn(orderId, reason);
        OrderDTO orderDTO = orderService.convertToDTO(returnedOrder);
        
        return ResponseEntity.ok(ApiResponse.<OrderDTO>builder()
                .success(true)
                .message("Return initiated successfully")
                .data(orderDTO)
                .timestamp(LocalDateTime.now())
                .build());
    }

    // ========== ADMIN ENDPOINTS (protected with @PreAuthorize) ==========

    @GetMapping("/admin/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get all orders (Admin)", description = "Returns all orders in the system. Admin only.")
    public ResponseEntity<ApiResponse<Page<OrderDTO>>> getAllOrders(
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.debug("Admin: Getting all orders");
        
        Page<OrderDTO> orders = orderService.getAllOrders(pageable);
        
        return ResponseEntity.ok(ApiResponse.<Page<OrderDTO>>builder()
                .success(true)
                .message("Orders retrieved successfully")
                .data(orders)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PutMapping("/admin/{orderId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Update order status (Admin)", description = "Updates the status of an order. Admin only.")
    public ResponseEntity<ApiResponse<OrderDTO>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus newStatus,
            @RequestParam(required = false) String notes) {
        
        log.debug("Admin: Updating order {} status to {}", orderId, newStatus);
        
        Order order = orderService.updateOrderStatus(orderId, newStatus, notes);
        OrderDTO orderDTO = orderService.convertToDTO(order);
        
        return ResponseEntity.ok(ApiResponse.<OrderDTO>builder()
                .success(true)
                .message("Order status updated successfully")
                .data(orderDTO)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @GetMapping("/admin/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get customer orders (Admin)", description = "Returns all orders for a specific customer. Admin only.")
    public ResponseEntity<ApiResponse<Page<OrderDTO>>> getCustomerOrdersAdmin(
            @PathVariable Long customerId,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.debug("Admin: Getting orders for customer: {}", customerId);
        
        Page<OrderDTO> orders = orderService.getOrdersByCustomerId(customerId, pageable);
        
        return ResponseEntity.ok(ApiResponse.<Page<OrderDTO>>builder()
                .success(true)
                .message("Customer orders retrieved successfully")
                .data(orders)
                .timestamp(LocalDateTime.now())
                .build());
    }
}
