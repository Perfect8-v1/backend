package com.perfect8.shop.service;

import com.perfect8.shop.dto.*;
import com.perfect8.shop.entity.*;
import com.perfect8.common.enums.OrderStatus;
import com.perfect8.shop.exception.*;
import com.perfect8.shop.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for order management.
 * Version 1.0 - Core functionality only
 *
 * This service handles all critical order operations:
 * - Order creation and processing
 * - Status management and transitions
 * - Inventory coordination
 * - Payment processing
 * - Shipping management
 * - Customer notifications
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;
    private final PaymentService paymentService;
    private final ShippingService shippingService;
    private final EmailService emailService;

    /**
     * Create a new order - Core functionality
     * This is the main entry point for order creation
     */
    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        log.info("Creating order for customer ID: {}", request.getCustomerId());

        // Validate customer
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found: " + request.getCustomerId()));

        // Create order
        Order order = Order.builder()
                .customer(customer)
                .orderStatus(OrderStatus.PENDING)
                .subtotal(request.getSubtotal())
                .taxAmount(request.getTaxAmount() != null ? request.getTaxAmount() : BigDecimal.ZERO)
                .shippingAmount(request.getShippingCost() != null ? request.getShippingCost() : BigDecimal.ZERO)
                .totalAmount(request.getTotalAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                .shippingFirstName(customer.getFirstName())
                .shippingLastName(customer.getLastName())
                .shippingEmail(customer.getEmail())
                .shippingPhone(customer.getPhoneNumber())
                .customerNotes(request.getNotes())
                .build();

        // Parse and set addresses
        setAddressesFromRequest(order, request, customer);

        // Initialize order items list if needed
        if (order.getOrderItems() == null) {
            order.setOrderItems(new ArrayList<>());
        }

        // Process order items
        for (CreateOrderRequest.OrderItemRequest itemRequest : request.getOrderItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException("Product not found: " + itemRequest.getProductId()));

            // Check inventory
            inventoryService.checkAvailability(product.getProductId(), itemRequest.getQuantity());

            // Reserve inventory
            inventoryService.reserveStock(product.getProductId(), itemRequest.getQuantity());

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(itemRequest.getUnitPrice())
                    .productName(product.getName())
                    .productSku(product.getSku())
                    .productDescription(product.getDescription())
                    .build();

            // Calculate subtotal
            orderItem.setSubtotal(
                    orderItem.getUnitPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity()))
            );

            order.getOrderItems().add(orderItem);
        }

        // Calculate totals
        calculateOrderTotals(order);

        // Save order
        Order savedOrder = orderRepository.save(order);

        // Send confirmation email
        emailService.sendEmail(
                savedOrder.getShippingEmail(),
                "Order Confirmation - " + savedOrder.getOrderNumber(),
                buildOrderConfirmationMessage(savedOrder)
        );

        log.info("Order created successfully with number: {}", savedOrder.getOrderNumber());
        return savedOrder;
    }

    /**
     * Get order by ID - Core functionality
     */
    @Transactional(readOnly = true)
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
    }

    /**
     * Get order by order number - Core functionality
     */
    @Transactional(readOnly = true)
    public Order getOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderNumber));
    }

    /**
     * Get all orders with pagination - Core functionality
     */
    @Transactional(readOnly = true)
    public Page<OrderDTO> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(this::convertToDTO);
    }

    /**
     * Get orders by customer ID - Core functionality
     * FIXED: Changed from findByCustomer_CustomerId to findByCustomerCustomerId
     */
    @Transactional(readOnly = true)
    public Page<OrderDTO> getOrdersByCustomerId(Long customerId, Pageable pageable) {
        return orderRepository.findByCustomerCustomerId(customerId, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Update order status - Core functionality
     * Critical for order lifecycle management
     */
    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus, String notes) {
        Order order = getOrderById(orderId);
        OrderStatus currentStatus = order.getOrderStatus();

        // Validate status transition
        if (!isValidStatusTransition(currentStatus, newStatus)) {
            log.warn("Invalid status transition from {} to {} for order {}",
                    currentStatus, newStatus, orderId);
            return order; // Return unchanged order
        }

        // Update status
        order.setOrderStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());

        if (notes != null) {
            order.setInternalNotes(notes);
        }

        // Handle status-specific actions
        switch (newStatus) {
            case PAID:
                handleOrderPaid(order);
                break;
            case PROCESSING:
                emailService.sendEmail(
                        order.getShippingEmail(),
                        "Order Processing - " + order.getOrderNumber(),
                        "Your order is now being processed."
                );
                break;
            case SHIPPED:
                if (order.getShipment() != null) {
                    emailService.sendEmail(
                            order.getShippingEmail(),
                            "Order Shipped - " + order.getOrderNumber(),
                            buildShippingMessage(order)
                    );
                }
                break;
            case DELIVERED:
                emailService.sendEmail(
                        order.getShippingEmail(),
                        "Order Delivered - " + order.getOrderNumber(),
                        "Your order has been delivered successfully."
                );
                break;
            case CANCELLED:
                handleOrderCancelled(order);
                break;
            case RETURNED:
                handleOrderReturned(order);
                break;
            default:
                break;
        }

        return orderRepository.save(order);
    }

    /**
     * Cancel an order - Core functionality
     * Critical for customer service
     */
    @Transactional
    public Order cancelOrder(Long orderId, String reason) {
        Order order = getOrderById(orderId);

        if (!canBeCancelled(order.getOrderStatus())) {
            throw new InvalidOrderStatusException(
                    "Order cannot be cancelled in status: " + order.getOrderStatus());
        }

        // Update status
        order.setOrderStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        order.setInternalNotes("Cancellation reason: " + (reason != null ? reason : "Customer request"));

        // Release inventory
        for (OrderItem item : order.getOrderItems()) {
            inventoryService.releaseReservedStock(
                    item.getProduct().getProductId(),
                    item.getQuantity());
        }

        // Process refund if payment was made
        if (isOrderPaid(order)) {
            paymentService.processRefund(
                    order.getPayment().getPaymentId(),
                    order.getTotalAmount());
        }

        // Send cancellation email
        emailService.sendEmail(
                order.getShippingEmail(),
                "Order Cancelled - " + order.getOrderNumber(),
                "Your order has been cancelled. Reason: " + (reason != null ? reason : "Customer request")
        );

        return orderRepository.save(order);
    }

    /**
     * Process order return - Core functionality
     * Critical for customer service
     */
    @Transactional
    public Order processReturn(Long orderId, String reason) {
        Order order = getOrderById(orderId);

        if (!canBeReturned(order.getOrderStatus())) {
            throw new InvalidOrderStatusException(
                    "Order cannot be returned in status: " + order.getOrderStatus());
        }

        // Update status
        order.setOrderStatus(OrderStatus.RETURNED);
        order.setUpdatedAt(LocalDateTime.now());
        order.setInternalNotes("Return reason: " + reason);

        // Return items to inventory
        for (OrderItem item : order.getOrderItems()) {
            inventoryService.returnToStock(
                    item.getProduct().getProductId(),
                    item.getQuantity());
        }

        // Process refund
        if (order.getPayment() != null) {
            paymentService.processRefund(
                    order.getPayment().getPaymentId(),
                    order.getTotalAmount());
        }

        // Send return confirmation email
        emailService.sendEmail(
                order.getShippingEmail(),
                "Order Return Processed - " + order.getOrderNumber(),
                "Your return has been processed. Reason: " + reason
        );

        return orderRepository.save(order);
    }

    /**
     * Get orders by status - Core functionality
     * FIXED: Added Pageable parameter and proper conversion
     */
    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByStatus(OrderStatus status) {
        // Use unpaged to get all results for backward compatibility
        return orderRepository.findByOrderStatus(status, Pageable.unpaged()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get recent orders - Core functionality
     * FIXED: Changed from findTopNByOrderByCreatedAtDesc to findAllByOrderByCreatedAtDesc with PageRequest
     */
    @Transactional(readOnly = true)
    public List<OrderDTO> getRecentOrders(int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit);
        return orderRepository.findAllByOrderByCreatedAtDesc(pageRequest).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Search orders - Core functionality
     * FIXED: Implemented search in service layer instead of expecting it in repository
     */
    @Transactional(readOnly = true)
    public Page<OrderDTO> searchOrders(String query, Pageable pageable) {
        // Simple search implementation for v1.0
        // Searches by order number, customer email, or shipping name

        List<Order> results = new ArrayList<>();

        // Search by order number
        orderRepository.findByOrderNumber(query).ifPresent(results::add);

        // Search by customer email
        Page<Order> emailResults = orderRepository.findByCustomerEmail(query, pageable);
        results.addAll(emailResults.getContent());

        // Remove duplicates
        Set<Long> seenIds = new HashSet<>();
        List<Order> uniqueResults = results.stream()
                .filter(order -> seenIds.add(order.getOrderId()))
                .collect(Collectors.toList());

        // Convert to DTOs and return as page
        List<OrderDTO> dtoList = uniqueResults.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, dtoList.size());
    }

    /**
     * Confirm payment for an order - Core functionality
     * Critical for order processing
     */
    @Transactional
    public Order confirmPayment(Long orderId, Map<String, Object> paymentDetails) {
        Order order = getOrderById(orderId);

        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStatusException(
                    "Payment can only be confirmed for pending orders");
        }

        // Create payment record
        Payment payment = paymentService.createPayment(order, paymentDetails);
        order.setPayment(payment);

        // Confirm inventory reservation
        for (OrderItem item : order.getOrderItems()) {
            inventoryService.confirmStock(
                    item.getProduct().getProductId(),
                    item.getQuantity());
        }

        // Update order status to PAID
        order.setOrderStatus(OrderStatus.PAID);
        order.setUpdatedAt(LocalDateTime.now());

        // Send payment confirmation
        emailService.sendEmail(
                order.getShippingEmail(),
                "Payment Confirmed - " + order.getOrderNumber(),
                "Payment of " + order.getTotalAmount() + " " + order.getCurrency() + " has been confirmed."
        );

        return orderRepository.save(order);
    }

    /**
     * Mark order as shipped - Core functionality
     * Critical for fulfillment
     */
    @Transactional
    public Order markAsShipped(Long orderId, String trackingNumber, String carrier) {
        Order order = getOrderById(orderId);

        if (order.getOrderStatus() != OrderStatus.PROCESSING &&
                order.getOrderStatus() != OrderStatus.PAID) {
            throw new InvalidOrderStatusException(
                    "Order must be in PROCESSING or PAID status to ship");
        }

        // V1.0: Simplified - no ShippingOptionDTO needed for large speakers
        String shippingMethod = order.getShippingAmount() != null && 
                                order.getShippingAmount().compareTo(new BigDecimal("100")) > 0 
                                ? "EXPRESS" : "STANDARD";
        
        Shipment shipment = shippingService.createShipment(order, shippingMethod);

        // Update tracking info
        if (trackingNumber != null && !trackingNumber.isEmpty()) {
            shipment.setTrackingNumber(trackingNumber);
        }
        if (carrier != null && !carrier.isEmpty()) {
            shipment.setCarrier(carrier);
        }

        order.setShipment(shipment);

        // Update status
        order.setOrderStatus(OrderStatus.SHIPPED);
        order.setUpdatedAt(LocalDateTime.now());

        // Send shipping notification
        emailService.sendEmail(
                order.getShippingEmail(),
                "Order Shipped - " + order.getOrderNumber(),
                buildShippingMessage(order)
        );

        return orderRepository.save(order);
    }

    /**
     * Mark order as delivered - Core functionality
     * Completes the order lifecycle
     */
    @Transactional
    public Order markAsDelivered(Long orderId, String deliveryNotes) {
        Order order = getOrderById(orderId);

        if (order.getOrderStatus() != OrderStatus.SHIPPED) {
            throw new InvalidOrderStatusException(
                    "Order must be SHIPPED to mark as delivered");
        }

        // Update shipment status
        if (order.getShipment() != null) {
            Shipment shipment = order.getShipment();
            shipment.setActualDeliveryDate(LocalDateTime.now().toLocalDate());
// VERSION 2.0: deliveryNotes feature commented out
// shipment.setDeliveryNotes(orderRequest.getDeliveryNotes());
            shipment.setStatus("DELIVERED");
        }

        // Update status
        order.setOrderStatus(OrderStatus.DELIVERED);
        order.setUpdatedAt(LocalDateTime.now());

        // Send delivery confirmation
        emailService.sendEmail(
                order.getShippingEmail(),
                "Order Delivered - " + order.getOrderNumber(),
                "Your order has been successfully delivered."
        );

        return orderRepository.save(order);
    }

    /**
     * Get orders needing attention - Core functionality
     * FIXED: Added Pageable parameter to findByOrderStatusIn
     */
    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersNeedingAttention() {
        List<OrderStatus> statuses = Arrays.asList(
                OrderStatus.PENDING,
                OrderStatus.PROCESSING,
                OrderStatus.PAYMENT_FAILED);

        return orderRepository.findByOrderStatusIn(statuses, Pageable.unpaged()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get order count by status - Core functionality
     * Basic reporting for operations
     */
    @Transactional(readOnly = true)
    public Map<OrderStatus, Long> getOrderCountByStatus() {
        Map<OrderStatus, Long> counts = new HashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            counts.put(status, orderRepository.countByOrderStatus(status));
        }
        return counts;
    }

    // ========== Helper methods ==========

    /**
     * Convert Order entity to DTO
     */
    public OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setOrderId(order.getOrderId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setCustomerId(order.getCustomer().getCustomerId());
        dto.setCustomerName(order.getShippingFirstName() + " " + order.getShippingLastName());
        dto.setCustomerEmail(order.getShippingEmail());
        dto.setStatus(order.getOrderStatus().name());
        dto.setSubtotal(order.getSubtotal());
        dto.setTaxAmount(order.getTaxAmount());
        dto.setShippingAmount(order.getShippingAmount());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setCurrency(order.getCurrency());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());

        // Convert order items
        if (order.getOrderItems() != null) {
            List<OrderItemDTO> itemDTOs = order.getOrderItems().stream()
                    .map(this::convertItemToDTO)
                    .collect(Collectors.toList());
            dto.setOrderItems(itemDTOs);
        }

        return dto;
    }

    /**
     * Convert OrderItem entity to DTO
     */
    private OrderItemDTO convertItemToDTO(OrderItem item) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setOrderItemId(item.getOrderItemId());
        dto.setProductId(item.getProduct().getProductId());
        dto.setProductName(item.getProductName());
        dto.setProductSku(item.getProductSku());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setSubtotal(item.getSubtotal());
        return dto;
    }

    /**
     * Helper method to set addresses from request
     * Version 1.0 - Simplified address handling
     */
    private void setAddressesFromRequest(Order order, CreateOrderRequest request, Customer customer) {
        // For v1.0, use simple address parsing
        // In v2.0, this would handle complex address objects with validation

        if (request.getShippingAddress() != null) {
            // Parse address string (simplified for v1.0)
            String[] parts = request.getShippingAddress().split(",");
            if (parts.length >= 4) {
                order.setShippingAddressLine1(parts[0].trim());
                order.setShippingCity(parts[1].trim());
                order.setShippingState(parts[2].trim());
                order.setShippingPostalCode(parts[3].trim());
            }
        } else {
            // Use customer's default address if available
            if (customer.getDefaultShippingAddress() != null) {
                Address addr = customer.getDefaultShippingAddress();
                order.setShippingAddressLine1(addr.getStreet());
                order.setShippingCity(addr.getCity());
                order.setShippingState(addr.getState());
                order.setShippingPostalCode(addr.getPostalCode());
                order.setShippingCountry(addr.getCountry());
            }
        }

        // Set billing address
        if (request.getBillingAddress() != null) {
            String[] parts = request.getBillingAddress().split(",");
            if (parts.length >= 4) {
                order.setBillingAddressLine1(parts[0].trim());
                order.setBillingCity(parts[1].trim());
                order.setBillingState(parts[2].trim());
                order.setBillingPostalCode(parts[3].trim());
                order.setBillingSameAsShipping(false);
            }
        } else {
            order.setBillingSameAsShipping(true);
        }
    }

    /**
     * Calculate order totals
     */
    private void calculateOrderTotals(Order order) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderItem item : order.getOrderItems()) {
            subtotal = subtotal.add(item.getSubtotal());
        }
        order.setSubtotal(subtotal);

        // If total wasn't provided, calculate it
        if (order.getTotalAmount() == null || order.getTotalAmount().compareTo(BigDecimal.ZERO) == 0) {
            BigDecimal total = subtotal
                    .add(order.getTaxAmount() != null ? order.getTaxAmount() : BigDecimal.ZERO)
                    .add(order.getShippingAmount() != null ? order.getShippingAmount() : BigDecimal.ZERO);
            order.setTotalAmount(total);
        }
    }

    /**
     * Check if order can be cancelled
     */
    private boolean canBeCancelled(OrderStatus status) {
        return status == OrderStatus.PENDING ||
                status == OrderStatus.PAID ||
                status == OrderStatus.PROCESSING;
    }

    /**
     * Check if order can be returned
     */
    private boolean canBeReturned(OrderStatus status) {
        return status == OrderStatus.DELIVERED;
    }

    /**
     * Check if order is paid
     */
    private boolean isOrderPaid(Order order) {
        return order.getPayment() != null &&
                order.getOrderStatus() != OrderStatus.PENDING;
    }

    /**
     * Check if status transition is valid
     */
    private boolean isValidStatusTransition(OrderStatus from, OrderStatus to) {
        // Simple validation for v1.0
        switch (from) {
            case PENDING:
                return to == OrderStatus.PAID ||
                        to == OrderStatus.CANCELLED ||
                        to == OrderStatus.PAYMENT_FAILED;
            case PAID:
                return to == OrderStatus.PROCESSING ||
                        to == OrderStatus.CANCELLED;
            case PROCESSING:
                return to == OrderStatus.SHIPPED ||
                        to == OrderStatus.CANCELLED;
            case SHIPPED:
                return to == OrderStatus.DELIVERED;
            case DELIVERED:
                return to == OrderStatus.RETURNED;
            default:
                return false;
        }
    }

    /**
     * Build order confirmation message
     */
    private String buildOrderConfirmationMessage(Order order) {
        StringBuilder message = new StringBuilder();
        message.append("Thank you for your order!\n\n");
        message.append("Order Number: ").append(order.getOrderNumber()).append("\n");
        message.append("Total: ").append(order.getTotalAmount()).append(" ").append(order.getCurrency()).append("\n\n");
        message.append("We will notify you when your order ships.");
        return message.toString();
    }

    /**
     * Build shipping message
     */
    private String buildShippingMessage(Order order) {
        StringBuilder message = new StringBuilder();
        message.append("Your order has been shipped!\n\n");
        message.append("Order Number: ").append(order.getOrderNumber()).append("\n");

        if (order.getShipment() != null) {
            Shipment shipment = order.getShipment();
            if (shipment.getTrackingNumber() != null) {
                message.append("Tracking Number: ").append(shipment.getTrackingNumber()).append("\n");
            }
            if (shipment.getCarrier() != null) {
                message.append("Carrier: ").append(shipment.getCarrier()).append("\n");
            }
        }

        message.append("\nThank you for your order!");
        return message.toString();
    }

    /**
     * Handle order paid status
     */
    private void handleOrderPaid(Order order) {
        // Confirm inventory
        for (OrderItem item : order.getOrderItems()) {
            inventoryService.confirmStock(
                    item.getProduct().getProductId(),
                    item.getQuantity());
        }

        // Send payment confirmation
        emailService.sendEmail(
                order.getShippingEmail(),
                "Payment Confirmed - " + order.getOrderNumber(),
                "Payment of " + order.getTotalAmount() + " " + order.getCurrency() + " has been received."
        );
    }

    /**
     * Handle order cancelled status
     */
    private void handleOrderCancelled(Order order) {
        // Release inventory
        for (OrderItem item : order.getOrderItems()) {
            inventoryService.releaseReservedStock(
                    item.getProduct().getProductId(),
                    item.getQuantity());
        }

        // Process refund if needed
        if (isOrderPaid(order)) {
            paymentService.processRefund(
                    order.getPayment().getPaymentId(),
                    order.getTotalAmount());
        }
    }

    /**
     * Handle order returned status
     */
    private void handleOrderReturned(Order order) {
        // Return inventory
        for (OrderItem item : order.getOrderItems()) {
            inventoryService.returnToStock(
                    item.getProduct().getProductId(),
                    item.getQuantity());
        }

        // Process refund
        if (order.getPayment() != null) {
            paymentService.processRefund(
                    order.getPayment().getPaymentId(),
                    order.getTotalAmount());
        }
    }
}