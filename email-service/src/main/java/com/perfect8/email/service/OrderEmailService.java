package com.perfect8.email.service;

import com.perfect8.common.enums.OrderStatus;
import com.perfect8.email.dto.OrderEmailDTO;
import com.perfect8.email.dto.OrderItemDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Order Email Service - Version 1.0
 * Enkel service för ordermeddelanden via Gmail
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEmailService {

    private final EmailService emailService;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Skicka orderbekräftelse
     */
    public void sendOrderConfirmation(OrderEmailDTO orderDto) {
        log.info("Sending order confirmation for order {}", orderDto.getOrderNumber());

        String subject = "Order Confirmation #" + orderDto.getOrderNumber();
        String body = buildOrderEmailBody(orderDto);

        emailService.sendEmail(
                orderDto.getCustomerEmail(),
                subject,
                body,
                true  // HTML email
        );
    }

    /**
     * Skicka statusuppdatering
     */
    public void sendOrderStatusEmail(OrderEmailDTO orderDto) {
        OrderStatus orderStatus = orderDto.getOrderStatus();

        if (orderStatus == null) {
            log.error("Cannot send status email - status is null for order {}",
                    orderDto.getOrderNumber());
            return;
        }

        String subject = getSubjectForStatus(orderStatus, orderDto.getOrderNumber());
        String body = buildOrderEmailBody(orderDto);

        emailService.sendEmail(
                orderDto.getCustomerEmail(),
                subject,
                body,
                true
        );

        log.info("Order status email sent for order {} (status: {})",
                orderDto.getOrderNumber(), orderStatus);
    }

    /**
     * Bygger email-innehåll
     */
    private String buildOrderEmailBody(OrderEmailDTO orderDto) {
        StringBuilder html = new StringBuilder();

        // Header
        html.append("<h2>Order ").append(orderDto.getOrderNumber()).append("</h2>");
        html.append("<p>Dear ").append(orderDto.getCustomerName()).append(",</p>");

        // Status message - nu direkt med enum!
        OrderStatus orderStatus = orderDto.getOrderStatus();
        if (orderStatus != null) {
            html.append("<p>").append(getStatusMessage(orderStatus)).append("</p>");
        }

        // Order details
        html.append("<h3>Order Details:</h3>");
        html.append("<table style='width:100%; border-collapse:collapse;'>");

        // Items
        if (orderDto.getItems() != null && !orderDto.getItems().isEmpty()) {
            html.append("<tr><th>Product</th><th>Qty</th><th>Price</th></tr>");
            for (OrderItemDto item : orderDto.getItems()) {
                html.append("<tr>");
                html.append("<td>").append(item.getProductName()).append("</td>");
                html.append("<td>").append(item.getQuantity()).append("</td>");
                html.append("<td>$").append(formatBigDecimal(item.getPrice())).append("</td>");
                html.append("</tr>");
            }
        }

        // Totals
        html.append("<tr><td colspan='2'><strong>Subtotal:</strong></td>");
        html.append("<td>$").append(formatBigDecimal(orderDto.getSubtotal())).append("</td></tr>");

        html.append("<tr><td colspan='2'><strong>Shipping:</strong></td>");
        html.append("<td>$").append(formatBigDecimal(orderDto.getShippingCost())).append("</td></tr>");

        // Tax om det finns
        if (orderDto.getTaxAmount() != null && orderDto.getTaxAmount().compareTo(BigDecimal.ZERO) > 0) {
            html.append("<tr><td colspan='2'><strong>Tax:</strong></td>");
            html.append("<td>$").append(formatBigDecimal(orderDto.getTaxAmount())).append("</td></tr>");
        }

        // Discount om det finns
        if (orderDto.hasDiscount()) {
            html.append("<tr><td colspan='2'><strong>Discount:</strong></td>");
            html.append("<td>-$").append(formatBigDecimal(orderDto.getDiscountAmount())).append("</td></tr>");
        }

        html.append("<tr><td colspan='2'><strong>Total:</strong></td>");
        html.append("<td><strong>$").append(formatBigDecimal(orderDto.getTotalAmount())).append("</strong></td></tr>");
        html.append("</table>");

        // Shipping address
        String shippingAddress = orderDto.getShippingAddress();
        if (shippingAddress != null && !shippingAddress.isEmpty()) {
            html.append("<h3>Shipping Address:</h3>");
            html.append("<p>");
            html.append(shippingAddress.replace("\n", "<br>"));
            html.append("</p>");
        }

        // Tracking info
        if (orderDto.hasTracking()) {
            html.append("<p><strong>Tracking Number:</strong> ").append(orderDto.getTrackingNumber()).append("</p>");
            if (orderDto.getCarrier() != null) {
                html.append("<p><strong>Carrier:</strong> ").append(orderDto.getCarrier()).append("</p>");
            }
        }

        // Estimated delivery
        LocalDateTime estimatedDelivery = orderDto.getEstimatedDeliveryDate();
        if (estimatedDelivery != null) {
            html.append("<p><strong>Estimated Delivery:</strong> ")
                    .append(formatDateTime(estimatedDelivery))
                    .append("</p>");
        }

        html.append("<p>Thank you for your order!</p>");

        // Store info footer
        if (orderDto.getStoreName() != null) {
            html.append("<hr>");
            html.append("<p><small>");
            html.append(orderDto.getStoreName());
            if (orderDto.getStorePhone() != null) {
                html.append(" | ").append(orderDto.getStorePhone());
            }
            if (orderDto.getStoreEmail() != null) {
                html.append(" | ").append(orderDto.getStoreEmail());
            }
            html.append("</small></p>");
        }

        return html.toString();
    }

    /**
     * Formatera BigDecimal för visning
     */
    private String formatBigDecimal(BigDecimal amount) {
        if (amount == null) {
            return "0.00";
        }
        return String.format("%.2f", amount);
    }

    /**
     * Formatera datetime till datum
     */
    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.toLocalDate().format(DATE_FORMAT);
    }

    /**
     * Hämta ämnesrad baserat på status
     */
    private String getSubjectForStatus(OrderStatus status, String orderNumber) {
        return switch (status) {
            case PENDING -> "Order Pending - #" + orderNumber;
            case PAID -> "Order Confirmed - #" + orderNumber;
            case PROCESSING -> "Order Processing - #" + orderNumber;
            case SHIPPED -> "Order Shipped - #" + orderNumber;
            case DELIVERED -> "Order Delivered - #" + orderNumber;
            case CANCELLED -> "Order Cancelled - #" + orderNumber;
            case RETURNED -> "Order Returned - #" + orderNumber;
            case PAYMENT_FAILED -> "Payment Failed - #" + orderNumber;
            case ON_HOLD -> "Order On Hold - #" + orderNumber;
        };
    }

    /**
     * Hämta statusmeddelande
     */
    private String getStatusMessage(OrderStatus status) {
        return switch (status) {
            case PENDING -> "Your order is pending payment.";
            case PAID -> "Thank you! Your order has been confirmed and is being processed.";
            case PROCESSING -> "Your order is being prepared for shipment.";
            case SHIPPED -> "Great news! Your order has been shipped.";
            case DELIVERED -> "Your order has been delivered.";
            case CANCELLED -> "Your order has been cancelled.";
            case RETURNED -> "Your return has been processed.";
            case PAYMENT_FAILED -> "Payment failed for your order. Please contact us.";
            case ON_HOLD -> "Your order is currently on hold. We'll update you soon.";
        };
    }
}