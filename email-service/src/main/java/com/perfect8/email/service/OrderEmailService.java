package com.perfect8.email.service;

import com.perfect8.common.enums.OrderStatus;
import com.perfect8.email.dto.OrderEmailDTO;
import com.perfect8.email.dto.OrderItemDto;
import com.perfect8.email.model.EmailMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Order Email Service - Version 1.0
 * Handles order-related email notifications based on OrderStatus
 * Uses object-oriented approach with EmailMessage
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEmailService {

    private final EmailService emailService;
    private final TemplateService templateService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    /**
     * Send order status email based on current order status
     * This is the main method called by shop-service when order status changes
     */
    @Transactional
    public void sendOrderStatusEmail(OrderEmailDTO orderDto) {
        if (orderDto == null || orderDto.getOrderStatus() == null) {
            log.error("Cannot send order email - order data is null");
            return;
        }

        String customerEmail = orderDto.getCustomerEmail();
        if (customerEmail == null || customerEmail.trim().isEmpty()) {
            log.error("Cannot send order email - customer email is missing for order {}",
                    orderDto.getOrderId());
            return;
        }

        OrderStatus status = orderDto.getOrderStatus();
        String templateName = getTemplateNameForStatus(status);
        String subject = generateSubjectForStatus(status, orderDto.getOrderNumber());

        if (templateName == null) {
            log.info("No email template for status {} - skipping email for order {}",
                    status, orderDto.getOrderId());
            return;
        }

        try {
            // Prepare template variables
            Map<String, Object> templateVars = prepareTemplateVariables(orderDto);

            // Generate email body from template
            String emailBody = templateService.processTemplate(templateName, templateVars);

            // Create EmailMessage object
            EmailMessage emailMessage = EmailMessage.builder()
                    .toEmail(customerEmail)
                    .subject(subject)
                    .htmlContent(emailBody)
                    .textContent(stripHtml(emailBody))
                    .metadata(Map.of(
                            "orderId", String.valueOf(orderDto.getOrderId()),
                            "orderStatus", status.name(),
                            "emailType", "ORDER_STATUS"
                    ))
                    .build();

            // Send the email using object-oriented approach
            boolean sent = emailService.sendEmail(emailMessage);

            if (sent) {
                log.info("Order status email sent successfully to {} for order {} (status: {})",
                        customerEmail, orderDto.getOrderNumber(), status);
            } else {
                log.error("Failed to send order status email to {} for order {} (status: {})",
                        customerEmail, orderDto.getOrderNumber(), status);
            }

        } catch (Exception e) {
            log.error("Error sending order email for order {}: {}",
                    orderDto.getOrderNumber(), e.getMessage(), e);
            // Don't throw - email failure shouldn't break order processing
        }
    }

    /**
     * Send order confirmation email (specifically for new orders)
     */
    public void sendOrderConfirmation(OrderEmailDTO orderDto) {
        if (orderDto.getOrderStatus() == null) {
            orderDto.setOrderStatus(OrderStatus.PAID);
        }
        sendOrderStatusEmail(orderDto);
    }

    /**
     * Send shipping notification
     */
    public void sendShippingNotification(OrderEmailDTO orderDto) {
        orderDto.setOrderStatus(OrderStatus.SHIPPED);
        sendOrderStatusEmail(orderDto);
    }

    /**
     * Send cancellation notification
     */
    public void sendCancellationNotification(OrderEmailDTO orderDto) {
        orderDto.setOrderStatus(OrderStatus.CANCELLED);
        sendOrderStatusEmail(orderDto);
    }

    /**
     * Prepare template variables from order DTO
     */
    private Map<String, Object> prepareTemplateVariables(OrderEmailDTO orderDto) {
        Map<String, Object> vars = new HashMap<>();

        // Basic order info
        vars.put("orderId", orderDto.getOrderId());
        vars.put("orderNumber", orderDto.getOrderNumber());
        vars.put("orderStatus", orderDto.getOrderStatus().name());
        vars.put("orderStatusDisplay", formatStatusForDisplay(orderDto.getOrderStatus()));
        vars.put("customerName", orderDto.getCustomerName());
        vars.put("customerEmail", orderDto.getCustomerEmail());

        // Format dates properly
        if (orderDto.getOrderDate() != null) {
            vars.put("orderDate", formatDate(orderDto.getOrderDate()));
        }

        vars.put("totalAmount", formatCurrency(orderDto.getTotalAmount()));
        vars.put("subtotal", formatCurrency(orderDto.getSubtotal()));
        vars.put("shippingCost", formatCurrency(orderDto.getShippingCost()));
        vars.put("tax", formatCurrency(orderDto.getTax()));

        // Handle shipping address - it's a List<String> in OrderEmailDTO
        List<String> shippingAddress = orderDto.getShippingAddress();
        if (shippingAddress != null && !shippingAddress.isEmpty()) {
            vars.put("shippingAddress", shippingAddress);
            vars.put("shippingAddressFormatted", formatAddress(shippingAddress));
        }

        // Tracking info for shipped orders
        if (orderDto.getTrackingNumber() != null) {
            vars.put("trackingNumber", orderDto.getTrackingNumber());
            vars.put("trackingUrl", orderDto.getTrackingUrl());
            vars.put("hasTracking", true);
        } else {
            vars.put("hasTracking", false);
        }

        // Order items - now using the items list from OrderEmailDTO
        List<OrderItemDto> items = orderDto.getItems();
        if (items != null && !items.isEmpty()) {
            vars.put("orderItems", items);
            vars.put("itemCount", items.size());

            // Calculate total items quantity
            int totalQuantity = items.stream()
                    .mapToInt(OrderItemDto::getQuantity)
                    .sum();
            vars.put("totalQuantity", totalQuantity);
        }

        // Estimated delivery
        LocalDate estimatedDelivery = orderDto.getEstimatedDeliveryDate();
        if (estimatedDelivery != null) {
            vars.put("estimatedDelivery", formatDate(estimatedDelivery));
            vars.put("hasEstimatedDelivery", true);
        } else {
            vars.put("hasEstimatedDelivery", false);
        }

        // Payment method
        if (orderDto.getPaymentMethod() != null) {
            vars.put("paymentMethod", orderDto.getPaymentMethod());
        }

        return vars;
    }

    /**
     * Generate subject line based on order status
     */
    private String generateSubjectForStatus(OrderStatus status, String orderNumber) {
        String orderRef = orderNumber != null ? orderNumber : "Your Order";

        return switch (status) {
            case PENDING -> "Order Received - Awaiting Payment #" + orderRef;
            case PAID -> "Order Confirmed - Thank You! #" + orderRef;
            case PROCESSING -> "Your Order is Being Processed #" + orderRef;
            case SHIPPED -> "Your Order Has Been Shipped! #" + orderRef;
            case DELIVERED -> "Your Order Has Been Delivered #" + orderRef;
            case CANCELLED -> "Order Cancelled #" + orderRef;
            case RETURNED -> "Return Processed #" + orderRef;
            case PAYMENT_FAILED -> "Payment Failed - Action Required #" + orderRef;
            case ON_HOLD -> "Order On Hold #" + orderRef;
        };
    }

    /**
     * Get email template name for status
     */
    public String getTemplateNameForStatus(OrderStatus status) {
        return switch (status) {
            case PENDING -> "order-pending";
            case PAID -> "order-confirmation";
            case PROCESSING -> "order-processing";
            case SHIPPED -> "order-shipped";
            case DELIVERED -> "order-delivered";
            case CANCELLED -> "order-cancelled";
            case RETURNED -> "order-returned";
            case PAYMENT_FAILED -> "payment-failed";
            case ON_HOLD -> null; // No automatic email for ON_HOLD in v1.0
        };
    }

    /**
     * Check if email should be sent for status
     */
    public boolean shouldSendEmailForStatus(OrderStatus status) {
        // In v1.0, we send emails for all statuses except ON_HOLD
        return status != OrderStatus.ON_HOLD;
    }

    /**
     * Format address list into HTML string
     */
    private String formatAddress(List<String> addressLines) {
        if (addressLines == null || addressLines.isEmpty()) {
            return "";
        }
        return addressLines.stream()
                .filter(line -> line != null && !line.trim().isEmpty())
                .collect(Collectors.joining("<br>"));
    }

    /**
     * Format currency for display
     */
    private String formatCurrency(Double amount) {
        if (amount == null) {
            return "$0.00";
        }
        return String.format("$%.2f", amount);
    }

    /**
     * Format date for display
     */
    private String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DATETIME_FORMATTER);
    }

    /**
     * Format date for display
     */
    private String formatDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(DATE_FORMATTER);
    }

    /**
     * Format order status for customer-friendly display
     */
    private String formatStatusForDisplay(OrderStatus status) {
        return switch (status) {
            case PENDING -> "Pending Payment";
            case PAID -> "Payment Confirmed";
            case PROCESSING -> "Processing";
            case SHIPPED -> "Shipped";
            case DELIVERED -> "Delivered";
            case CANCELLED -> "Cancelled";
            case RETURNED -> "Returned";
            case PAYMENT_FAILED -> "Payment Failed";
            case ON_HOLD -> "On Hold";
        };
    }

    /**
     * Simple HTML stripper for text-only version
     */
    private String stripHtml(String html) {
        if (html == null) {
            return "";
        }
        return html.replaceAll("<[^>]*>", "")
                .replaceAll("&nbsp;", " ")
                .replaceAll("&amp;", "&")
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("&quot;", "\"")
                .trim();
    }
}