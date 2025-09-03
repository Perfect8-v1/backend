package com.perfect8.shop.service;

import com.perfect8.shop.dto.*;
import com.perfect8.shop.entity.*;
import com.perfect8.shop.enums.OrderStatus;
import com.perfect8.shop.exception.*;
import com.perfect8.shop.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
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
     * Create a new order
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

            orderItem.calculateSubtotal();
            order.addOrderItem(orderItem);
        }

        // Calculate totals
        order.calculateTotals();

        // Save order
        Order savedOrder = orderRepository.save(order);

        // Send confirmation email
        emailService.sendOrderConfirmation(savedOrder);

        log.info("Order created successfully with number: {}", savedOrder.getOrderNumber());
        return savedOrder;
    }

    /**
     * Get order by ID
     */
    @Transactional(readOnly = true)
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
    }

    /**
     * Get order by order number
     */
    @Transactional(readOnly = true)
    public Order getOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderNumber));
    }

    /**
     * Get all orders with pagination
     */
    @Transactional(readOnly = true)
    public Page<OrderDTO> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(this::convertToDTO);
    }

    /**
     * Get orders by customer ID
     */
    @Transactional(readOnly = true)
    public Page<OrderDTO> getOrdersByCustomerId(Long customerId, Pageable pageable) {
        return orderRepository.findByCustomer_CustomerId(customerId, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Update order status
     */
    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus, String notes) {
        Order order = getOrderById(orderId);
        OrderStatus currentStatus = order.getOrderStatus();

        // Validate status transition
        if (!currentStatus.canTransitionTo(newStatus)) {
            log.warn("Invalid status transition from {} to {} for order {}",
                    currentStatus, newStatus, orderId);
            return order; // Return unchanged order
        }

        // Update status
        order.updateStatus(newStatus);
        if (notes != null) {
            order.setInternalNotes(notes);
        }

        // Handle status-specific actions
        switch (newStatus) {
            case CONFIRMED:
                handleOrderConfirmed(order);
                break;
            case PROCESSING:
                emailService.sendOrderProcessing(order);
                break;
            case SHIPPED:
                emailService.sendShippingNotification(order);
                break;
            case DELIVERED:
                emailService.sendDeliveryConfirmation(order);
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
     * Cancel an order
     */
    @Transactional
    public Order cancelOrder(Long orderId, String reason) {
        Order order = getOrderById(orderId);

        if (!order.isCancellable()) {
            throw new InvalidOrderStatusException(
                    "Order cannot be cancelled in status: " + order.getOrderStatus());
        }

        // Update status
        order.updateStatus(OrderStatus.CANCELLED);
        order.setInternalNotes("Cancellation reason: " + (reason != null ? reason : "Customer request"));

        // Release inventory
        for (OrderItem item : order.getOrderItems()) {
            inventoryService.releaseReservedStock(
                    item.getProduct().getProductId(),
                    item.getQuantity());
        }

        // Process refund if payment was made
        if (order.isPaid() && order.getPayment() != null) {
            paymentService.processRefund(
                    order.getPayment().getPaymentId(),
                    order.getTotalAmount());
        }

        // Send cancellation email
        emailService.sendOrderCancellation(order, reason != null ? reason : "Customer request");

        return orderRepository.save(order);
    }

    /**
     * Process order return
     */
    @Transactional
    public Order processReturn(Long orderId, String reason) {
        Order order = getOrderById(orderId);

        if (!order.isReturnable()) {
            throw new InvalidOrderStatusException(
                    "Order cannot be returned in status: " + order.getOrderStatus());
        }

        // Update status
        order.updateStatus(OrderStatus.RETURNED);
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
        emailService.sendReturnConfirmation(order);

        return orderRepository.save(order);
    }

    /**
     * Get orders by status
     */
    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByOrderStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get recent orders
     */
    @Transactional(readOnly = true)
    public List<OrderDTO> getRecentOrders(int limit) {
        return orderRepository.findTopNByOrderByCreatedAtDesc(limit).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Search orders
     */
    @Transactional(readOnly = true)
    public Page<OrderDTO> searchOrders(String query, Pageable pageable) {
        return orderRepository.searchOrders(query, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Confirm payment for an order
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

        // Update order status
        order.updateStatus(OrderStatus.CONFIRMED);

        // Send confirmation email
        emailService.sendPaymentConfirmation(order);

        return orderRepository.save(order);
    }

    /**
     * Mark order as shipped
     */
    @Transactional
    public Order markAsShipped(Long orderId, String trackingNumber, String carrier) {
        Order order = getOrderById(orderId);

        if (order.getOrderStatus() != OrderStatus.PROCESSING &&
                order.getOrderStatus() != OrderStatus.CONFIRMED) {
            throw new InvalidOrderStatusException(
                    "Order must be in PROCESSING or CONFIRMED status to ship");
        }

        // Create shipment
        Shipment shipment = shippingService.createShipment(order, trackingNumber, carrier);
        order.setShipment(shipment);

        // Update status
        order.updateStatus(OrderStatus.SHIPPED);

        // Send shipping notification
        emailService.sendShippingNotification(order);

        return orderRepository.save(order);
    }

    /**
     * Mark order as delivered
     */
    @Transactional
    public Order markAsDelivered(Long orderId, String deliveryNotes) {
        Order order = getOrderById(orderId);

        if (order.getOrderStatus() != OrderStatus.SHIPPED) {
            throw new InvalidOrderStatusException(
                    "Order must be SHIPPED to mark as delivered");
        }

        // Update shipment
        if (order.getShipment() != null) {
            shippingService.markAsDelivered(order.getShipment(), deliveryNotes);
        }

        // Update status
        order.updateStatus(OrderStatus.DELIVERED);

        // Send delivery confirmation
        emailService.sendDeliveryConfirmation(order);

        return orderRepository.save(order);
    }

    /**
     * Get orders needing attention
     */
    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersNeedingAttention() {
        List<OrderStatus> statuses = Arrays.asList(
                OrderStatus.PENDING,
                OrderStatus.PROCESSING,
                OrderStatus.PAYMENT_FAILED);

        return orderRepository.findByOrderStatusIn(statuses).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get order count by status
     */
    @Transactional(readOnly = true)
    public Map<OrderStatus, Long> getOrderCountByStatus() {
        Map<OrderStatus, Long> counts = new HashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            counts.put(status, orderRepository.countByOrderStatus(status));
        }
        return counts;
    }

    /**
     * Convert Order entity to DTO
     */
    public OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getOrderId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setCustomerId(order.getCustomer().getCustomerId());
        dto.setCustomerName(order.getCustomerFullName());
        dto.setCustomerEmail(order.getShippingEmail());
        dto.setStatus(order.getOrderStatus().name());
        dto.setSubtotal(order.getSubtotal());
        dto.setTax(order.getTaxAmount());
        dto.setShipping(order.getShippingAmount());
        dto.setTotal(order.getTotalAmount());
        dto.setCurrency(order.getCurrency());
        dto.setOrderDate(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());

        // Convert order items
        List<OrderItemDTO> itemDTOs = order.getOrderItems().stream()
                .map(this::convertItemToDTO)
                .collect(Collectors.toList());
        dto.setItems(itemDTOs);

        return dto;
    }

    /**
     * Convert OrderItem entity to DTO
     */
    private OrderItemDTO convertItemToDTO(OrderItem item) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(item.getOrderItemId());
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
     */
    private void setAddressesFromRequest(Order order, CreateOrderRequest request, Customer customer) {
        // For now, use simple address parsing
        // In version 2.0, this would handle complex address objects

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
     * Handle order confirmed status
     */
    private void handleOrderConfirmed(Order order) {
        // Confirm inventory
        for (OrderItem item : order.getOrderItems()) {
            inventoryService.confirmStock(
                    item.getProduct().getProductId(),
                    item.getQuantity());
        }

        // Send confirmation email
        emailService.sendPaymentConfirmation(order);
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
        if (order.isPaid() && order.getPayment() != null) {
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