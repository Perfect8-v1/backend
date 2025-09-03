package com.perfect8.shop.service;

import com.perfect8.shop.entity.Order;
import com.perfect8.shop.entity.Customer;
import com.perfect8.shop.entity.Shipment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Email Service - Version 1.0
 * Basic email functionality for order confirmations and shipping notifications
 *
 * IMPORTANT: For v1.0, we're using simplified email templates.
 * Advanced templating and marketing emails will come in v2.0.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:noreply@perfect8shop.com}")
    private String fromEmail;

    @Value("${app.name:Perfect8 Shop}")
    private String appName;

    @Value("${app.url:http://localhost:8082}")
    private String appUrl;

    @Value("${spring.mail.enabled:false}")
    private boolean emailEnabled;

    /**
     * Send order confirmation email - CRITICAL for customer trust!
     */
    public void sendOrderConfirmation(Order order) {
        if (!emailEnabled) {
            log.info("Email disabled - would send order confirmation for order {}", order.getId());
            return;
        }

        try {
            String to = order.getCustomer().getEmail();
            String subject = String.format("Order Confirmation - #%d", order.getId());

            String content = buildOrderConfirmationEmail(order);

            sendHtmlEmail(to, subject, content);
            log.info("Order confirmation email sent to {} for order {}", to, order.getId());

        } catch (Exception e) {
            log.error("Failed to send order confirmation email for order {}: {}",
                    order.getId(), e.getMessage());
            // Don't throw - email failure shouldn't stop order processing
        }
    }

    /**
     * Send shipping notification - Important for customer satisfaction!
     */
    public void sendShippingNotification(Shipment shipment) {
        if (!emailEnabled) {
            log.info("Email disabled - would send shipping notification for shipment {}",
                    shipment.getId());
            return;
        }

        try {
            Order order = shipment.getOrder();
            String to = order.getCustomer().getEmail();
            String subject = String.format("Your Order #%d Has Shipped!", order.getId());

            String content = buildShippingNotificationEmail(shipment);

            sendHtmlEmail(to, subject, content);
            log.info("Shipping notification sent to {} for order {}", to, order.getId());

        } catch (Exception e) {
            log.error("Failed to send shipping notification: {}", e.getMessage());
        }
    }

    /**
     * Send delivery confirmation
     */
    public void sendDeliveryConfirmation(Order order) {
        if (!emailEnabled) {
            log.info("Email disabled - would send delivery confirmation for order {}", order.getId());
            return;
        }

        try {
            String to = order.getCustomer().getEmail();
            String subject = String.format("Order #%d Delivered Successfully", order.getId());

            String content = buildDeliveryConfirmationEmail(order);

            sendHtmlEmail(to, subject, content);
            log.info("Delivery confirmation sent to {} for order {}", to, order.getId());

        } catch (Exception e) {
            log.error("Failed to send delivery confirmation: {}", e.getMessage());
        }
    }

    /**
     * Send order cancellation email
     */
    public void sendOrderCancellation(Order order, String reason) {
        if (!emailEnabled) {
            log.info("Email disabled - would send cancellation email for order {}", order.getId());
            return;
        }

        try {
            String to = order.getCustomer().getEmail();
            String subject = String.format("Order #%d Cancelled", order.getId());

            String content = buildCancellationEmail(order, reason);

            sendHtmlEmail(to, subject, content);
            log.info("Cancellation email sent to {} for order {}", to, order.getId());

        } catch (Exception e) {
            log.error("Failed to send cancellation email: {}", e.getMessage());
        }
    }

    /**
     * Send payment confirmation
     */
    public void sendPaymentConfirmation(Order order, BigDecimal amount) {
        if (!emailEnabled) {
            log.info("Email disabled - would send payment confirmation for order {}", order.getId());
            return;
        }

        try {
            String to = order.getCustomer().getEmail();
            String subject = String.format("Payment Received - Order #%d", order.getId());

            String content = buildPaymentConfirmationEmail(order, amount);

            sendHtmlEmail(to, subject, content);
            log.info("Payment confirmation sent to {} for order {}", to, order.getId());

        } catch (Exception e) {
            log.error("Failed to send payment confirmation: {}", e.getMessage());
        }
    }

    /**
     * Send refund notification
     */
    public void sendRefundNotification(Order order, BigDecimal refundAmount) {
        if (!emailEnabled) {
            log.info("Email disabled - would send refund notification for order {}", order.getId());
            return;
        }

        try {
            String to = order.getCustomer().getEmail();
            String subject = String.format("Refund Processed - Order #%d", order.getId());

            String content = buildRefundEmail(order, refundAmount);

            sendHtmlEmail(to, subject, content);
            log.info("Refund notification sent to {} for order {}", to, order.getId());

        } catch (Exception e) {
            log.error("Failed to send refund notification: {}", e.getMessage());
        }
    }

    /**
     * Send welcome email for new customer
     */
    public void sendWelcomeEmail(Customer customer) {
        if (!emailEnabled) {
            log.info("Email disabled - would send welcome email to {}", customer.getEmail());
            return;
        }

        try {
            String to = customer.getEmail();
            String subject = String.format("Welcome to %s!", appName);

            String content = buildWelcomeEmail(customer);

            sendHtmlEmail(to, subject, content);
            log.info("Welcome email sent to {}", to);

        } catch (Exception e) {
            log.error("Failed to send welcome email: {}", e.getMessage());
        }
    }

    // Private helper methods

    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    private void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }

    // Email template builders - simplified for v1.0

    private String buildOrderConfirmationEmail(Order order) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif;">
                <h2>Order Confirmation</h2>
                <p>Dear %s,</p>
                <p>Thank you for your order! We've received your order and it's being processed.</p>
                <h3>Order Details:</h3>
                <ul>
                    <li>Order Number: <strong>#%d</strong></li>
                    <li>Order Date: %s</li>
                    <li>Total Amount: $%.2f</li>
                    <li>Status: %s</li>
                </ul>
                <p>You can track your order at: <a href="%s/orders/%d">Track Order</a></p>
                <p>Thank you for shopping with us!</p>
                <p>Best regards,<br>%s Team</p>
            </body>
            </html>
            """,
                order.getCustomer().getFullName(),
                order.getId(),
                order.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                order.getTotalAmount(),
                order.getStatus(),
                appUrl, order.getId(),
                appName
        );
    }

    private String buildShippingNotificationEmail(Shipment shipment) {
        Order order = shipment.getOrder();
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif;">
                <h2>Your Order Has Shipped!</h2>
                <p>Dear %s,</p>
                <p>Great news! Your order has been shipped and is on its way to you.</p>
                <h3>Shipping Details:</h3>
                <ul>
                    <li>Order Number: <strong>#%d</strong></li>
                    <li>Tracking Number: <strong>%s</strong></li>
                    <li>Carrier: %s</li>
                    <li>Estimated Delivery: %s</li>
                </ul>
                <p>Track your package: <a href="%s/track/%s">Track Package</a></p>
                <p>Thank you for your patience!</p>
                <p>Best regards,<br>%s Team</p>
            </body>
            </html>
            """,
                order.getCustomer().getFullName(),
                order.getId(),
                shipment.getTrackingNumber(),
                shipment.getCarrier(),
                shipment.getEstimatedDeliveryDate(),
                appUrl, shipment.getTrackingNumber(),
                appName
        );
    }

    private String buildDeliveryConfirmationEmail(Order order) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif;">
                <h2>Order Delivered Successfully!</h2>
                <p>Dear %s,</p>
                <p>Your order #%d has been delivered successfully.</p>
                <p>We hope you're happy with your purchase!</p>
                <p>If you have any questions or concerns, please don't hesitate to contact us.</p>
                <p>Best regards,<br>%s Team</p>
            </body>
            </html>
            """,
                order.getCustomer().getFullName(),
                order.getId(),
                appName
        );
    }

    private String buildCancellationEmail(Order order, String reason) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif;">
                <h2>Order Cancellation Confirmation</h2>
                <p>Dear %s,</p>
                <p>Your order #%d has been cancelled.</p>
                %s
                <p>If you paid for this order, a refund will be processed within 3-5 business days.</p>
                <p>If you have any questions, please contact our customer service.</p>
                <p>Best regards,<br>%s Team</p>
            </body>
            </html>
            """,
                order.getCustomer().getFullName(),
                order.getId(),
                reason != null ? "<p>Reason: " + reason + "</p>" : "",
                appName
        );
    }

    private String buildPaymentConfirmationEmail(Order order, BigDecimal amount) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif;">
                <h2>Payment Received</h2>
                <p>Dear %s,</p>
                <p>We've received your payment of $%.2f for order #%d.</p>
                <p>Your order will be processed and shipped soon.</p>
                <p>Thank you!</p>
                <p>Best regards,<br>%s Team</p>
            </body>
            </html>
            """,
                order.getCustomer().getFullName(),
                amount,
                order.getId(),
                appName
        );
    }

    private String buildRefundEmail(Order order, BigDecimal refundAmount) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif;">
                <h2>Refund Processed</h2>
                <p>Dear %s,</p>
                <p>A refund of $%.2f has been processed for order #%d.</p>
                <p>The refund should appear in your account within 3-5 business days.</p>
                <p>If you have any questions, please contact us.</p>
                <p>Best regards,<br>%s Team</p>
            </body>
            </html>
            """,
                order.getCustomer().getFullName(),
                refundAmount,
                order.getId(),
                appName
        );
    }

    private String buildWelcomeEmail(Customer customer) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif;">
                <h2>Welcome to %s!</h2>
                <p>Dear %s,</p>
                <p>Thank you for creating an account with us!</p>
                <p>You can now:</p>
                <ul>
                    <li>Track your orders</li>
                    <li>Save your shipping addresses</li>
                    <li>View your order history</li>
                    <li>Manage your account settings</li>
                </ul>
                <p>Start shopping: <a href="%s">Visit our store</a></p>
                <p>Best regards,<br>%s Team</p>
            </body>
            </html>
            """,
                appName,
                customer.getFullName(),
                appUrl,
                appName
        );
    }

    /* VERSION 2.0 - ADVANCED EMAIL FEATURES
     *
     * In v2.0, this service will include:
     * - Template engine integration (Thymeleaf/Freemarker)
     * - Marketing emails and newsletters
     * - Abandoned cart reminders
     * - Product recommendations
     * - Customer segmentation
     * - Email tracking and analytics
     * - A/B testing for email campaigns
     * - Unsubscribe management
     * - Email queue with retry logic
     * - Rich HTML templates with images
     * - Multi-language support
     * - SMS notifications
     * - Push notifications
     */
}