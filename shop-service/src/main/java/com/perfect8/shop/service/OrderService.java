package com.perfect8.shop.service;

import com.perfect8.shop.dto.*;
import com.perfect8.shop.entity.*;
import com.perfect8.common.enums.OrderStatus;
import com.perfect8.shop.exception.*;
import com.perfect8.shop.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for order management.
 * Version 1.0 - Core functionality only
 *
 * FIXED 2026-02-22:
 * - convertToDTO uses dto.setStatus() (String) not setOrderStatus()
 * - Billing fields removed from convertToDTO (v2.0 - commented out in OrderDTO)
 * - cancelOrder(Long, String) overload added for OrderController
 * - processReturn(Long, String) added for OrderController
 * - setAddressesFromRequest reads individual fields first (address-parsing fix)
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

    // ========== CREATE ==========

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        log.info("Creating order for customer ID: {}", request.getCustomerId());

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found: " + request.getCustomerId()));

        Order order = Order.builder()
                .customer(customer)
                .orderStatus(OrderStatus.PENDING)
                .subtotal(request.getSubtotal())
                .taxAmount(request.getTaxAmount() != null ? request.getTaxAmount() : BigDecimal.ZERO)
                .shippingAmount(request.getShippingCost() != null ? request.getShippingCost() : BigDecimal.ZERO)
                .totalAmount(request.getTotalAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "SEK")
                .shippingFirstName(customer.getFirstName())
                .shippingLastName(customer.getLastName())
                .shippingEmail(customer.getEmail())
                .shippingPhone(customer.getPhone())
                .customerNotes(request.getNotes())
                .build();

        setAddressesFromRequest(order, request, customer);

        if (order.getOrderItems() == null) {
            order.setOrderItems(new ArrayList<>());
        }

        for (CreateOrderRequest.OrderItemRequest itemRequest : request.getOrderItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException("Product not found: " + itemRequest.getProductId()));

            inventoryService.checkAvailability(product.getProductId(), itemRequest.getQuantity());
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

            orderItem.setPrice(
                    orderItem.getUnitPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity()))
            );

            order.getOrderItems().add(orderItem);
        }

        calculateOrderTotals(order);

        Order savedOrder = orderRepository.save(order);

        emailService.sendEmail(
                savedOrder.getShippingEmail(),
                "Order Confirmation - " + savedOrder.getOrderNumber(),
                buildOrderConfirmationMessage(savedOrder)
        );

        log.info("Order created successfully with number: {}", savedOrder.getOrderNumber());
        return savedOrder;
    }

    // ========== READ ==========

    @Transactional(readOnly = true)
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
    }

    @Transactional(readOnly = true)
    public Order getOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderNumber));
    }

    @Transactional(readOnly = true)
    public Page<OrderDTO> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<OrderDTO> getOrdersByCustomerId(Long customerId, Pageable pageable) {
        return orderRepository.findByCustomerCustomerId(customerId, pageable)
                .map(this::convertToDTO);
    }

    // ========== UPDATE ==========

    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus, String notes) {
        Order order = getOrderById(orderId);
        OrderStatus currentStatus = order.getOrderStatus();

        if (!isValidStatusTransition(currentStatus, newStatus)) {
            log.warn("Invalid status transition from {} to {} for order {}", currentStatus, newStatus, orderId);
            return order;
        }

        order.setOrderStatus(newStatus);
        if (notes != null) {
            order.setInternalNotes(notes);
        }

        switch (newStatus) {
            case PAID:
                handleOrderPaid(order);
                break;
            case PROCESSING:
                emailService.sendEmail(order.getShippingEmail(),
                        "Order Processing - " + order.getOrderNumber(),
                        "Your order is now being processed.");
                break;
            case SHIPPED:
                if (order.getShipment() != null) {
                    emailService.sendEmail(order.getShippingEmail(),
                            "Order Shipped - " + order.getOrderNumber(),
                            buildShippingMessage(order));
                }
                break;
            default:
                break;
        }

        return orderRepository.save(order);
    }

    // ========== CANCEL & RETURN ==========

    /**
     * Cancel order with optional reason - called from OrderController.
     */
    @Transactional
    public Order cancelOrder(Long orderId, String reason) {
        Order order = getOrderById(orderId);

        if (!canBeCancelled(order.getOrderStatus())) {
            throw new IllegalStateException("Order cannot be cancelled in status: " + order.getOrderStatus());
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        if (reason != null) {
            order.setInternalNotes(reason);
        }
        handleOrderCancelled(order);
        return orderRepository.save(order);
    }

    /**
     * Cancel order without reason.
     */
    @Transactional
    public Order cancelOrder(Long orderId) {
        return cancelOrder(orderId, null);
    }

    /**
     * Process return - called from OrderController.
     */
    @Transactional
    public Order processReturn(Long orderId, String reason) {
        Order order = getOrderById(orderId);

        if (!canBeReturned(order.getOrderStatus())) {
            throw new IllegalStateException("Order cannot be returned in status: " + order.getOrderStatus());
        }

        order.setOrderStatus(OrderStatus.RETURNED);
        if (reason != null) {
            order.setInternalNotes(reason);
        }
        handleOrderReturned(order);
        return orderRepository.save(order);
    }

    // ========== DTO CONVERSION ==========

    /**
     * Convert Order entity to OrderDTO.
     *
     * IMPORTANT: OrderDTO uses 'status' (String), not 'orderStatus' (enum).
     * Billing fields are v2.0 - not in OrderDTO yet.
     */
    public OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setOrderId(order.getOrderId());
        dto.setOrderNumber(order.getOrderNumber());

        // OrderDTO.status is String, not enum
        if (order.getOrderStatus() != null) {
            dto.setStatus(order.getOrderStatus().name());
        }

        dto.setSubtotal(order.getSubtotal() != null ? order.getSubtotal() : BigDecimal.ZERO);
        dto.setTaxAmount(order.getTaxAmount() != null ? order.getTaxAmount() : BigDecimal.ZERO);
        dto.setShippingAmount(order.getShippingAmount() != null ? order.getShippingAmount() : BigDecimal.ZERO);
        dto.setTotalAmount(order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO);
        dto.setCurrency(order.getCurrency() != null ? order.getCurrency() : "SEK");
        dto.setCreatedDate(order.getCreatedDate());
        dto.setUpdatedDate(order.getUpdatedDate());

        // Shipping address
        dto.setShippingFirstName(nvl(order.getShippingFirstName()));
        dto.setShippingLastName(nvl(order.getShippingLastName()));
        dto.setShippingEmail(nvl(order.getShippingEmail()));
        dto.setShippingPhone(nvl(order.getShippingPhone()));
        dto.setShippingAddressLine1(nvl(order.getShippingAddressLine1()));
        dto.setShippingAddressLine2(nvl(order.getShippingAddressLine2()));
        dto.setShippingCity(nvl(order.getShippingCity()));
        dto.setShippingState(nvl(order.getShippingState()));
        dto.setShippingPostalCode(nvl(order.getShippingPostalCode()));
        dto.setShippingCountry(nvl(order.getShippingCountry()));

        // Customer info
        if (order.getCustomer() != null) {
            dto.setCustomerId(order.getCustomer().getCustomerId());
            dto.setCustomerEmail(nvl(order.getCustomer().getEmail()));
            dto.setCustomerName(nvl(order.getCustomer().getFirstName()) + " " + nvl(order.getCustomer().getLastName()));
        }

        // Order items
        if (order.getOrderItems() != null) {
            dto.setOrderItems(order.getOrderItems().stream()
                    .map(this::convertItemToDTO)
                    .collect(Collectors.toList()));
        }

        /* VERSION 2.0 - billing address (commented out in OrderDTO)
        dto.setBillingAddressLine1(order.getBillingAddressLine1());
        dto.setBillingCity(order.getBillingCity());
        dto.setBillingState(order.getBillingState());
        dto.setBillingPostalCode(order.getBillingPostalCode());
        dto.setBillingCountry(order.getBillingCountry());
        // NOTE: Boolean wrapper → getBillingSameAsShipping(), not isBillingSameAsShipping()
        dto.setBillingSameAsShipping(order.getBillingSameAsShipping());
        */

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
        dto.setSubtotal(item.getPrice());
        return dto;
    }

    /** Null-safe string helper */
    private String nvl(String value) {
        return value != null ? value : "";
    }

    // ========== ADDRESS HANDLING ==========

    /**
     * Set addresses from request.
     *
     * Priority 1: Individual fields (shippingAddressLine1 etc) - Flutter standard
     * Priority 2: Comma-separated shippingAddress string - legacy
     * Priority 3: Customer default address - final fallback
     *
     * NOTE: shippingCountry column is VARCHAR(2) → always use "SE"
     */
    private void setAddressesFromRequest(Order order, CreateOrderRequest request, Customer customer) {

        if (request.getShippingAddressLine1() != null && !request.getShippingAddressLine1().isBlank()) {
            log.debug("Using individual shipping address fields");
            order.setShippingAddressLine1(request.getShippingAddressLine1());
            order.setShippingCity(nvl(request.getShippingCity()));
            order.setShippingState(nvl(request.getShippingState()));
            order.setShippingPostalCode(nvl(request.getShippingPostalCode()));
            order.setShippingCountry("SE");

        } else if (request.getShippingAddress() != null && !request.getShippingAddress().isBlank()) {
            log.debug("Parsing shipping address string: {}", request.getShippingAddress());
            String[] parts = request.getShippingAddress().split(",");
            if (parts.length >= 4) {
                order.setShippingAddressLine1(parts[0].trim());
                order.setShippingCity(parts[1].trim());
                order.setShippingState(parts[2].trim());
                order.setShippingPostalCode(parts[3].trim());
                order.setShippingCountry("SE");
            } else {
                log.warn("shippingAddress has fewer than 4 parts: '{}'", request.getShippingAddress());
                useCustomerDefaultAddress(order, customer);
            }

        } else {
            log.debug("No shipping address in request, using customer default");
            useCustomerDefaultAddress(order, customer);
        }

        // Billing = same as shipping for v1.0
        order.setBillingAddressLine1(order.getShippingAddressLine1());
        order.setBillingCity(order.getShippingCity());
        order.setBillingState(order.getShippingState());
        order.setBillingPostalCode(order.getShippingPostalCode());
        order.setBillingCountry(order.getShippingCountry());
        order.setBillingSameAsShipping(true);
    }

    private void useCustomerDefaultAddress(Order order, Customer customer) {
        if (customer.getDefaultShippingAddress() != null) {
            Address addr = customer.getDefaultShippingAddress();
            order.setShippingAddressLine1(nvl(addr.getStreet()));
            order.setShippingCity(nvl(addr.getCity()));
            order.setShippingState(nvl(addr.getState()));
            order.setShippingPostalCode(nvl(addr.getPostalCode()));
            order.setShippingCountry("SE");
        } else {
            log.warn("Customer {} has no default address. Using empty strings.", customer.getCustomerId());
            order.setShippingAddressLine1("");
            order.setShippingCity("");
            order.setShippingState("");
            order.setShippingPostalCode("");
            order.setShippingCountry("SE");
        }
    }

    // ========== PRIVATE HELPERS ==========

    private void calculateOrderTotals(Order order) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderItem item : order.getOrderItems()) {
            subtotal = subtotal.add(item.getPrice());
        }
        order.setSubtotal(subtotal);

        if (order.getTotalAmount() == null || order.getTotalAmount().compareTo(BigDecimal.ZERO) == 0) {
            BigDecimal total = subtotal
                    .add(order.getTaxAmount() != null ? order.getTaxAmount() : BigDecimal.ZERO)
                    .add(order.getShippingAmount() != null ? order.getShippingAmount() : BigDecimal.ZERO);
            order.setTotalAmount(total);
        }
    }

    private boolean canBeCancelled(OrderStatus status) {
        return status == OrderStatus.PENDING ||
                status == OrderStatus.PAID ||
                status == OrderStatus.PROCESSING;
    }

    private boolean canBeReturned(OrderStatus status) {
        return status == OrderStatus.DELIVERED;
    }

    private boolean isOrderPaid(Order order) {
        return order.getPayment() != null &&
                order.getOrderStatus() != OrderStatus.PENDING;
    }

    private boolean isValidStatusTransition(OrderStatus from, OrderStatus to) {
        switch (from) {
            case PENDING:
                return to == OrderStatus.PAID || to == OrderStatus.CANCELLED || to == OrderStatus.PAYMENT_FAILED;
            case PAID:
                return to == OrderStatus.PROCESSING || to == OrderStatus.CANCELLED;
            case PROCESSING:
                return to == OrderStatus.SHIPPED || to == OrderStatus.CANCELLED;
            case SHIPPED:
                return to == OrderStatus.DELIVERED;
            case DELIVERED:
                return to == OrderStatus.RETURNED;
            default:
                return false;
        }
    }

    private String buildOrderConfirmationMessage(Order order) {
        return "Thank you for your order!\n\n" +
                "Order Number: " + order.getOrderNumber() + "\n" +
                "Total: " + order.getTotalAmount() + " " + order.getCurrency() + "\n\n" +
                "We will notify you when your order ships.";
    }

    private String buildShippingMessage(Order order) {
        StringBuilder msg = new StringBuilder();
        msg.append("Your order has been shipped!\n\nOrder Number: ").append(order.getOrderNumber()).append("\n");
        if (order.getShipment() != null) {
            if (order.getShipment().getTrackingNumber() != null) {
                msg.append("Tracking Number: ").append(order.getShipment().getTrackingNumber()).append("\n");
            }
            if (order.getShipment().getCarrier() != null) {
                msg.append("Carrier: ").append(order.getShipment().getCarrier()).append("\n");
            }
        }
        msg.append("\nThank you for your order!");
        return msg.toString();
    }

    private void handleOrderPaid(Order order) {
        for (OrderItem item : order.getOrderItems()) {
            inventoryService.confirmStock(item.getProduct().getProductId(), item.getQuantity());
        }
        emailService.sendEmail(order.getShippingEmail(),
                "Payment Confirmed - " + order.getOrderNumber(),
                "Payment of " + order.getTotalAmount() + " " + order.getCurrency() + " has been received.");
    }

    private void handleOrderCancelled(Order order) {
        for (OrderItem item : order.getOrderItems()) {
            inventoryService.releaseReservedStock(item.getProduct().getProductId(), item.getQuantity());
        }
        if (isOrderPaid(order)) {
            paymentService.processRefund(order.getPayment().getPaymentId(), order.getTotalAmount());
        }
    }

    private void handleOrderReturned(Order order) {
        for (OrderItem item : order.getOrderItems()) {
            inventoryService.returnToStock(item.getProduct().getProductId(), item.getQuantity());
        }
        if (order.getPayment() != null) {
            paymentService.processRefund(order.getPayment().getPaymentId(), order.getTotalAmount());
        }
    }
}
