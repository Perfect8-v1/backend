package com.perfect8.shop.service;

import com.perfect8.common.enums.OrderStatus;
import com.perfect8.shop.entity.Order;
import com.perfect8.shop.entity.OrderItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Email Service for Shop Service
 * Version 1.1 - HTTP calls to email-service microservice
 * 
 * Sends transactional emails via email-service using API key authentication.
 * Maintains backward compatibility with existing OrderService calls.
 */
@Service
@Slf4j
public class EmailService {

    private final RestTemplate restTemplate;

    @Value("${email.service.url:http://email-service:8083}")
    private String emailServiceUrl;

    @Value("${SHOP_API_KEY:p8shop_1Lm3pV6bC9fK2hW4}")
    private String shopApiKey;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public EmailService() {
        this.restTemplate = new RestTemplate();
    }

    // ========================================================================
    // BACKWARD COMPATIBLE METHODS (used by OrderService)
    // ========================================================================

    /**
     * Send simple text email
     * Used by OrderService for all email notifications
     * 
     * @param to Recipient email address
     * @param subject Email subject
     * @param body Email body content
     */
    public void sendEmail(String to, String subject, String body) {
        log.info("Sending email to: {}, subject: {}", to, subject);

        try {
            Map<String, Object> emailDto = new HashMap<>();
            emailDto.put("recipientEmail", to);
            emailDto.put("subject", subject);
            emailDto.put("content", body);
            emailDto.put("html", false);

            sendToEmailService("/api/email/send", emailDto);
            log.info("Email sent successfully to: {}", to);

        } catch (Exception e) {
            log.error("Failed to send email to: {} - {}", to, e.getMessage());
            // Don't throw - email failure shouldn't break order flow
        }
    }

    /**
     * Send HTML email
     * 
     * @param to Recipient email address
     * @param subject Email subject
     * @param htmlBody HTML email body
     */
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        log.info("Sending HTML email to: {}, subject: {}", to, subject);

        try {
            Map<String, Object> emailDto = new HashMap<>();
            emailDto.put("recipientEmail", to);
            emailDto.put("subject", subject);
            emailDto.put("content", htmlBody);
            emailDto.put("html", true);

            sendToEmailService("/api/email/send", emailDto);
            log.info("HTML email sent successfully to: {}", to);

        } catch (Exception e) {
            log.error("Failed to send HTML email to: {} - {}", to, e.getMessage());
        }
    }

    // ========================================================================
    // ORDER TEMPLATE EMAILS (use email-service templates)
    // ========================================================================

    /**
     * Send order confirmation using template
     */
    public void sendOrderConfirmation(Order order) {
        log.info("Sending order confirmation for order: {}", order.getOrderNumber());

        try {
            Map<String, Object> emailDto = buildOrderEmailDto(order);
            emailDto.put("orderStatus", OrderStatus.PAID.name());

            sendToEmailService("/api/email/order/confirmation", emailDto);
            log.info("Order confirmation sent for order: {}", order.getOrderNumber());

        } catch (Exception e) {
            log.error("Failed to send order confirmation for order: {}", order.getOrderNumber(), e);
        }
    }

    /**
     * Send shipping notification using template
     */
    public void sendShippingNotification(Order order, String trackingNumber, String carrier) {
        log.info("Sending shipping notification for order: {}", order.getOrderNumber());

        try {
            Map<String, Object> emailDto = buildOrderEmailDto(order);
            emailDto.put("orderStatus", OrderStatus.SHIPPED.name());
            emailDto.put("trackingNumber", trackingNumber);
            emailDto.put("carrier", carrier);

            sendToEmailService("/api/email/order/shipped", emailDto);
            log.info("Shipping notification sent for order: {}", order.getOrderNumber());

        } catch (Exception e) {
            log.error("Failed to send shipping notification for order: {}", order.getOrderNumber(), e);
        }
    }

    /**
     * Send order cancellation using template
     */
    public void sendOrderCancellation(Order order, String reason) {
        log.info("Sending cancellation notification for order: {}", order.getOrderNumber());

        try {
            Map<String, Object> emailDto = buildOrderEmailDto(order);
            emailDto.put("orderStatus", OrderStatus.CANCELLED.name());
            emailDto.put("cancellationReason", reason);

            sendToEmailService("/api/email/order/cancelled", emailDto);
            log.info("Cancellation notification sent for order: {}", order.getOrderNumber());

        } catch (Exception e) {
            log.error("Failed to send cancellation notification for order: {}", order.getOrderNumber(), e);
        }
    }

    /**
     * Send order status update using template
     */
    public void sendOrderStatusUpdate(Order order) {
        log.info("Sending status update for order: {}", order.getOrderNumber());

        try {
            Map<String, Object> emailDto = buildOrderEmailDto(order);
            emailDto.put("orderStatus", order.getOrderStatus().name());

            sendToEmailService("/api/email/order/status", emailDto);
            log.info("Status update sent for order: {}", order.getOrderNumber());

        } catch (Exception e) {
            log.error("Failed to send status update for order: {}", order.getOrderNumber(), e);
        }
    }

    // ========================================================================
    // CUSTOMER EMAILS
    // ========================================================================

    /**
     * Send order confirmation email (simple version)
     */
    public void sendOrderConfirmation(String customerEmail, String orderNumber, String orderDetails) {
        String subject = "Order Confirmation - " + orderNumber;
        String body = "Thank you for your order!\n\n" +
                "Order Number: " + orderNumber + "\n\n" +
                orderDetails + "\n\n" +
                "We'll notify you when your order ships.";

        sendEmail(customerEmail, subject, body);
    }

    /**
     * Send shipping notification (simple version)
     */
    public void sendShippingNotification(String customerEmail, String orderNumber,
                                         String trackingNumber, String carrier) {
        String subject = "Your Order Has Shipped - " + orderNumber;
        String body = "Good news! Your order has been shipped.\n\n" +
                "Order Number: " + orderNumber + "\n" +
                "Tracking Number: " + trackingNumber + "\n" +
                "Carrier: " + carrier + "\n\n" +
                "You can track your package using the tracking number above.";

        sendEmail(customerEmail, subject, body);
    }

    /**
     * Send order cancellation email (simple version)
     */
    public void sendOrderCancellation(String customerEmail, String orderNumber, String reason) {
        String subject = "Order Cancelled - " + orderNumber;
        String body = "Your order has been cancelled.\n\n" +
                "Order Number: " + orderNumber + "\n" +
                "Reason: " + (reason != null ? reason : "Customer request") + "\n\n" +
                "If you have any questions, please contact customer service.";

        sendEmail(customerEmail, subject, body);
    }

    /**
     * Send welcome email for new customer registration
     */
    public void sendWelcomeEmail(String customerEmail, String customerName) {
        String subject = "Welcome to Perfect8 Shop!";
        String body = "Welcome " + customerName + "!\n\n" +
                "Thank you for creating an account with Perfect8 Shop.\n\n" +
                "You can now enjoy:\n" +
                "- Fast checkout\n" +
                "- Order tracking\n" +
                "- Exclusive offers\n\n" +
                "Happy shopping!";

        sendEmail(customerEmail, subject, body);
    }

    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(String customerEmail, String resetToken) {
        String subject = "Password Reset Request";
        String body = "You have requested to reset your password.\n\n" +
                "Your reset token is: " + resetToken + "\n\n" +
                "This token will expire in 1 hour.\n\n" +
                "If you did not request this, please ignore this email.";

        sendEmail(customerEmail, subject, body);
    }

    /**
     * Send email verification
     */
    public void sendEmailVerification(String customerEmail, String verificationToken) {
        String subject = "Verify Your Email Address";
        String body = "Please verify your email address.\n\n" +
                "Your verification token is: " + verificationToken + "\n\n" +
                "This token will expire in 24 hours.\n\n" +
                "Thank you!";

        sendEmail(customerEmail, subject, body);
    }

    /**
     * Send payment confirmation
     */
    public void sendPaymentConfirmation(String customerEmail, String orderNumber,
                                        String amount, String currency) {
        String subject = "Payment Confirmed - " + orderNumber;
        String body = "Your payment has been confirmed.\n\n" +
                "Order Number: " + orderNumber + "\n" +
                "Amount: " + amount + " " + currency + "\n\n" +
                "Thank you for your purchase!";

        sendEmail(customerEmail, subject, body);
    }

    /**
     * Send return confirmation
     */
    public void sendReturnConfirmation(String customerEmail, String orderNumber, String returnReason) {
        String subject = "Return Processed - " + orderNumber;
        String body = "Your return has been processed.\n\n" +
                "Order Number: " + orderNumber + "\n" +
                "Reason: " + returnReason + "\n\n" +
                "Your refund will be processed within 5-7 business days.";

        sendEmail(customerEmail, subject, body);
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    /**
     * Build OrderEmailDTO from Order entity
     * Uses Order's separate shipping fields (not Address object)
     */
    private Map<String, Object> buildOrderEmailDto(Order order) {
        Map<String, Object> dto = new HashMap<>();

        // Basic order info
        dto.put("orderId", order.getOrderId());
        dto.put("orderNumber", order.getOrderNumber());
        dto.put("orderDate", order.getCreatedDate() != null 
                ? order.getCreatedDate().format(DATE_FORMATTER) : null);
        dto.put("totalAmount", order.getTotalAmount());

        // Customer info
        if (order.getCustomer() != null) {
            dto.put("customerName", order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName());
            dto.put("customerEmail", order.getCustomer().getEmail());
        } else {
            dto.put("customerName", order.getShippingFirstName() + " " + order.getShippingLastName());
            dto.put("customerEmail", order.getShippingEmail());
        }

        // Order items
        if (order.getOrderItems() != null) {
            List<Map<String, Object>> items = order.getOrderItems().stream()
                    .map(this::buildOrderItemDto)
                    .collect(Collectors.toList());
            dto.put("orderItems", items);
        }

        // Shipping address (from Order's separate fields)
        Map<String, Object> address = new HashMap<>();
        address.put("fullName", order.getShippingFullName());
        address.put("addressLine1", order.getShippingAddressLine1());
        address.put("addressLine2", order.getShippingAddressLine2());
        address.put("city", order.getShippingCity());
        address.put("state", order.getShippingState());
        address.put("zipCode", order.getShippingPostalCode());
        address.put("country", order.getShippingCountry());
        dto.put("shippingAddress", address);

        return dto;
    }

    /**
     * Build order item map
     */
    private Map<String, Object> buildOrderItemDto(OrderItem item) {
        Map<String, Object> dto = new HashMap<>();
        
        Map<String, Object> product = new HashMap<>();
        if (item.getProduct() != null) {
            product.put("name", item.getProduct().getName());
            product.put("description", item.getProduct().getDescription());
        } else {
            // Use stored product info if product reference is null
            product.put("name", item.getProductName());
            product.put("description", item.getProductDescription());
        }
        dto.put("product", product);
        dto.put("quantity", item.getQuantity());
        dto.put("price", item.getUnitPrice());
        
        return dto;
    }

    /**
     * Send HTTP request to email-service
     */
    private void sendToEmailService(String endpoint, Map<String, Object> body) {
        String url = emailServiceUrl + endpoint;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Api-Key", shopApiKey);
        headers.set("X-Service-Name", "shop-service");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn("Email service returned non-success status: {} for endpoint: {}",
                        response.getStatusCode(), endpoint);
            }

        } catch (Exception e) {
            log.error("Failed to call email service at {}: {}", url, e.getMessage());
            throw e;
        }
    }

    /**
     * Check if email service is available
     */
    public boolean isServiceAvailable() {
        try {
            String url = emailServiceUrl + "/api/email/test";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.warn("Email service not available: {}", e.getMessage());
            return false;
        }
    }
}
