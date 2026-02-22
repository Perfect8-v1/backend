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
                .shippingPhone(customer.getPhone())
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

            // Calculate price (OrderItem uses 'price' not 'subtotal')
            orderItem.setPrice(
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
            default:
                break;
        }

        return orderRepository.save(order);
    }

    /**
     * Cancel an order - Core functionality
     */
    @Transactional
    public Order cancelOrder(Long orderId) {
        Order order = getOrderById(orderId);

        if (!canBeCancelled(order.getOrderStatus())) {
            throw new IllegalStateException("Order cannot be cancelled in status: " + order.getOrderStatus());
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        handleOrderCancelled(order);
        return orderRepository.save(order);
    }

    /**
     * Convert Order entity to OrderDTO
     */
    public OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setOrderId(order.getOrderId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setOrderStatus(order.getOrderStatus());
        dto.setSubtotal(order.getSubtotal());
        dto.setTaxAmount(order.getTaxAmount());
        dto.setShippingAmount(order.getShippingAmount());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setCurrency(order.getCurrency());
        dto.setCreatedDate(order.getCreatedDate());
        dto.setUpdatedDate(order.getUpdatedDate());

        // Shipping address
        dto.setShippingFirstName(order.getShippingFirstName());
        dto.setShippingLastName(order.getShippingLastName());
        dto.setShippingEmail(order.getShippingEmail());
        dto.setShippingPhone(order.getShippingPhone());
        dto.setShippingAddressLine1(order.getShippingAddressLine1());
        dto.setShippingAddressLine2(order.getShippingAddressLine2());
        dto.setShippingCity(order.getShippingCity());
        dto.setShippingState(order.getShippingState());
        dto.setShippingPostalCode(order.getShippingPostalCode());
        dto.setShippingCountry(order.getShippingCountry());

        // Billing address
        dto.setBillingAddressLine1(order.getBillingAddressLine1());
        dto.setBillingCity(order.getBillingCity());
        dto.setBillingState(order.getBillingState());
        dto.setBillingPostalCode(order.getBillingPostalCode());
        dto.setBillingCountry(order.getBillingCountry());
        dto.setBillingSameAsShipping(order.isBillingSameAsShipping());

        // Order items
        if (order.getOrderItems() != null) {
            dto.setOrderItems(order.getOrderItems().stream()
                    .map(this::convertItemToDTO)
                    .collect(Collectors.toList()));
        }

        // Customer ID
        if (order.getCustomer() != null) {
            dto.setCustomerId(order.getCustomer().getCustomerId());
        }

        return dto;
    }

    private OrderItemDTO convertItemToDTO(OrderItem item) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setOrderItemId(item.getOrderItemId());
        if (item.getProduct() != null) {
            dto.setProductId(item.getProduct().getProductId());
        }
        dto.setProductName(item.getProductName());
        dto.setProductSku(item.getProductSku());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setSubtotal(item.getPrice());  // FIXED: getSubtotal() -> getPrice()
        return dto;
    }

    /**
     * Helper method to set addresses from request.
     *
     * FIXED (2026-02-22): Reads individual fields first (shippingAddressLine1, shippingCity etc).
     * Falls back to comma-separated shippingAddress string if individual fields are missing.
     * Final fallback: customer's default shipping address.
     *
     * This makes the method compatible with Flutter sending individual fields.
     */
    private void setAddressesFromRequest(Order order, CreateOrderRequest request, Customer customer) {

        // --- SHIPPING ADDRESS ---
        // Priority 1: Individual fields (new standard from Flutter)
        if (request.getShippingAddressLine1() != null && !request.getShippingAddressLine1().isBlank()) {
            log.debug("Using individual shipping address fields");
            order.setShippingAddressLine1(request.getShippingAddressLine1());
            order.setShippingCity(request.getShippingCity() != null ? request.getShippingCity() : "");
            order.setShippingState(request.getShippingState() != null ? request.getShippingState() : "");
            order.setShippingPostalCode(request.getShippingPostalCode() != null ? request.getShippingPostalCode() : "");
            order.setShippingCountry(request.getShippingCountry() != null ? request.getShippingCountry() : "Sverige");

        // Priority 2: Comma-separated string (legacy format)
        } else if (request.getShippingAddress() != null && !request.getShippingAddress().isBlank()) {
            log.debug("Parsing shipping address from string: {}", request.getShippingAddress());
            String[] parts = request.getShippingAddress().split(",");
            if (parts.length >= 4) {
                order.setShippingAddressLine1(parts[0].trim());
                order.setShippingCity(parts[1].trim());
                order.setShippingState(parts[2].trim());
                order.setShippingPostalCode(parts[3].trim());
                order.setShippingCountry(parts.length >= 5 ? parts[4].trim() : "Sverige");
            } else {
                log.warn("shippingAddress string has fewer than 4 parts: '{}'", request.getShippingAddress());
                useCustomerDefaultAddress(order, customer);
            }

        // Priority 3: Customer's default address
        } else {
            log.debug("No shipping address in request, using customer default");
            useCustomerDefaultAddress(order, customer);
        }

        // --- BILLING ADDRESS ---
        if (request.getBillingAddress() != null && !request.getBillingAddress().isBlank()) {
            String[] parts = request.getBillingAddress().split(",");
            if (parts.length >= 4) {
                order.setBillingAddressLine1(parts[0].trim());
                order.setBillingCity(parts[1].trim());
                order.setBillingState(parts[2].trim());
                order.setBillingPostalCode(parts[3].trim());
                order.setBillingSameAsShipping(false);
            }
        } else {
            // Default: billing same as shipping
            order.setBillingAddressLine1(order.getShippingAddressLine1());
            order.setBillingCity(order.getShippingCity());
            order.setBillingState(order.getShippingState());
            order.setBillingPostalCode(order.getShippingPostalCode());
            order.setBillingCountry(order.getShippingCountry());
            order.setBillingSameAsShipping(true);
        }
    }

    /**
     * Copies customer's default shipping address to order.
     * If no default address exists, fields are set to empty string (not null) to avoid SQL constraint errors.
     */
    private void useCustomerDefaultAddress(Order order, Customer customer) {
        if (customer.getDefaultShippingAddress() != null) {
            Address addr = customer.getDefaultShippingAddress();
            order.setShippingAddressLine1(addr.getStreet() != null ? addr.getStreet() : "");
            order.setShippingCity(addr.getCity() != null ? addr.getCity() : "");
            order.setShippingState(addr.getState() != null ? addr.getState() : "");
            order.setShippingPostalCode(addr.getPostalCode() != null ? addr.getPostalCode() : "");
            order.setShippingCountry(addr.getCountry() != null ? addr.getCountry() : "Sverige");
        } else {
            log.warn("No shipping address provided and customer {} has no default address. Using empty strings.",
                    customer.getCustomerId());
            // Set empty strings instead of null to avoid NOT NULL constraint
            order.setShippingAddressLine1("");
            order.setShippingCity("");
            order.setShippingState("");
            order.setShippingPostalCode("");
            order.setShippingCountry("Sverige");
        }
    }

    /**
     * Calculate order totals
     */
    private void calculateOrderTotals(Order order) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderItem item : order.getOrderItems()) {
            subtotal = subtotal.add(item.getPrice());  // FIXED: getSubtotal() -> getPrice()
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
        for (OrderItem item : order.getOrderItems()) {
            inventoryService.confirmStock(
                    item.getProduct().getProductId(),
                    item.getQuantity());
        }

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
        for (OrderItem item : order.getOrderItems()) {
            inventoryService.releaseReservedStock(
                    item.getProduct().getProductId(),
                    item.getQuantity());
        }

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
        for (OrderItem item : order.getOrderItems()) {
            inventoryService.returnToStock(
                    item.getProduct().getProductId(),
                    item.getQuantity());
        }

        if (order.getPayment() != null) {
            paymentService.processRefund(
                    order.getPayment().getPaymentId(),
                    order.getTotalAmount());
        }
    }
}
