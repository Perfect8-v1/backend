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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

    @Value("${app.support.email:support@perfect8shop.com}")
    private String supportEmail;

    /**
     * Generic email sending method - used by OrderService
     * This is the main method that OrderService calls
     */
    public void sendEmail(String toEmail, String subject, String body) {
        if (!emailEnabled) {
            log.info("Email disabled - would send email to {} with subject: {}", toEmail, subject);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("Email sent to {} with subject: {}", toEmail, subject);

        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
            // Don't throw - email failure shouldn't stop business operations
        }
    }

    /**
     * Send password reset email - CRITICAL for account recovery!
     */
    public void sendPasswordResetEmail(Customer customer, String resetToken) {
        if (!emailEnabled) {
            log.info("Email disabled - would send password reset email to {}", customer.getEmail());
            return;
        }

        try {
            String subject = String.format("%s - Password Reset Request", appName);
            String content = buildPasswordResetEmail(
                    customer.getEmail(),
                    resetToken,
                    customer.getCustomerDisplayName()
            );

            sendHtmlEmail(customer.getEmail(), subject, content);
            log.info("Password reset email sent to {}", customer.getEmail());

        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", customer.getEmail(), e.getMessage());
            // Don't throw - but this is critical, should be monitored
        }
    }

    /**
     * Send email verification - IMPORTANT for account security!
     */
    public void sendEmailVerification(Customer customer) {
        if (!emailEnabled) {
            log.info("Email disabled - would send verification email to {}", customer.getEmail());
            return;
        }

        try {
            String subject = String.format("Verify your email address - %s", appName);
            String content = buildEmailVerificationEmail(
                    customer.getEmail(),
                    customer.getEmailVerificationToken(),
                    customer.getCustomerDisplayName()
            );

            sendHtmlEmail(customer.getEmail(), subject, content);
            log.info("Email verification sent to {}", customer.getEmail());

        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", customer.getEmail(), e.getMessage());
            // Don't throw - but log for monitoring
        }
    }

    /**
     * Send password change confirmation
     */
    public void sendPasswordChangeConfirmation(Customer customer) {
        if (!emailEnabled) {
            log.info("Email disabled - would send password change confirmation to {}", customer.getEmail());
            return;
        }

        try {
            String subject = String.format("%s - Password Changed Successfully", appName);
            String content = buildPasswordChangeConfirmationEmail(
                    customer.getEmail(),
                    customer.getCustomerDisplayName()
            );

            sendHtmlEmail(customer.getEmail(), subject, content);
            log.info("Password change confirmation sent to {}", customer.getEmail());

        } catch (Exception e) {
            log.error("Failed to send password change confirmation to {}: {}",
                    customer.getEmail(), e.getMessage());
        }
    }

    /**
     * Send order confirmation email - CRITICAL for customer trust!
     */
    public void sendOrderConfirmation(Order order) {
        if (!emailEnabled) {
            log.info("Email disabled - would send order confirmation for order {}", order.getOrderId());
            return;
        }

        try {
            String toEmail = order.getShippingEmail();
            String subject = String.format("Order Confirmation - #%s", order.getOrderNumber());

            String content = buildOrderConfirmationEmail(order);

            sendHtmlEmail(toEmail, subject, content);
            log.info("Order confirmation email sent to {} for order {}", toEmail, order.getOrderId());

        } catch (Exception e) {
            log.error("Failed to send order confirmation email for order {}: {}",
                    order.getOrderId(), e.getMessage());
            // Don't throw - email failure shouldn't stop order processing
        }
    }

    /**
     * Send shipping notification - Important for customer satisfaction!
     */
    public void sendShippingNotification(Shipment shipment) {
        if (!emailEnabled) {
            log.info("Email disabled - would send shipping notification for shipment {}",
                    shipment.getShipmentId());
            return;
        }

        try {
            Order order = shipment.getOrder();
            String toEmail = order.getShippingEmail();
            String subject = String.format("Your Order #%s Has Shipped!", order.getOrderNumber());

            String content = buildShippingNotificationEmail(shipment);

            sendHtmlEmail(toEmail, subject, content);
            log.info("Shipping notification sent to {} for order {}", toEmail, order.getOrderId());

        } catch (Exception e) {
            log.error("Failed to send shipping notification: {}", e.getMessage());
        }
    }

    /**
     * Send delivery confirmation
     */
    public void sendDeliveryConfirmation(Order order) {
        if (!emailEnabled) {
            log.info("Email disabled - would send delivery confirmation for order {}", order.getOrderId());
            return;
        }

        try {
            String toEmail = order.getShippingEmail();
            String subject = String.format("Order #%s Delivered Successfully", order.getOrderNumber());

            String content = buildDeliveryConfirmationEmail(order);

            sendHtmlEmail(toEmail, subject, content);
            log.info("Delivery confirmation sent to {} for order {}", toEmail, order.getOrderId());

        } catch (Exception e) {
            log.error("Failed to send delivery confirmation: {}", e.getMessage());
        }
    }

    /**
     * Send order cancellation email
     */
    public void sendOrderCancellation(Order order, String reason) {
        if (!emailEnabled) {
            log.info("Email disabled - would send cancellation email for order {}", order.getOrderId());
            return;
        }

        try {
            String toEmail = order.getShippingEmail();
            String subject = String.format("Order #%s Cancelled", order.getOrderNumber());

            String content = buildCancellationEmail(order, reason);

            sendHtmlEmail(toEmail, subject, content);
            log.info("Cancellation email sent to {} for order {}", toEmail, order.getOrderId());

        } catch (Exception e) {
            log.error("Failed to send cancellation email: {}", e.getMessage());
        }
    }

    /**
     * Send payment confirmation
     */
    public void sendPaymentConfirmation(Order order, BigDecimal amount) {
        if (!emailEnabled) {
            log.info("Email disabled - would send payment confirmation for order {}", order.getOrderId());
            return;
        }

        try {
            String toEmail = order.getShippingEmail();
            String subject = String.format("Payment Received - Order #%s", order.getOrderNumber());

            String content = buildPaymentConfirmationEmail(order, amount);

            sendHtmlEmail(toEmail, subject, content);
            log.info("Payment confirmation sent to {} for order {}", toEmail, order.getOrderId());

        } catch (Exception e) {
            log.error("Failed to send payment confirmation: {}", e.getMessage());
        }
    }

    /**
     * Send refund notification
     */
    public void sendRefundNotification(Order order, BigDecimal refundAmount) {
        if (!emailEnabled) {
            log.info("Email disabled - would send refund notification for order {}", order.getOrderId());
            return;
        }

        try {
            String toEmail = order.getShippingEmail();
            String subject = String.format("Refund Processed - Order #%s", order.getOrderNumber());

            String content = buildRefundEmail(order, refundAmount);

            sendHtmlEmail(toEmail, subject, content);
            log.info("Refund notification sent to {} for order {}", toEmail, order.getOrderId());

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
            String toEmail = customer.getEmail();
            String subject = String.format("Welcome to %s!", appName);

            String content = buildWelcomeEmail(customer);

            sendHtmlEmail(toEmail, subject, content);
            log.info("Welcome email sent to {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send welcome email: {}", e.getMessage());
        }
    }

    // ========== Private helper methods ==========

    private void sendHtmlEmail(String toEmail, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    // ========== Email template builders - simplified for v1.0 ==========

    private String buildPasswordResetEmail(String email, String resetToken, String displayName) {
        String resetLink = String.format("%s/reset-password?token=%s&email=%s", appUrl, resetToken, email);

        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif;">
                <h2>Password Reset Request</h2>
                <p>Dear %s,</p>
                <p>We received a request to reset your password for your %s account.</p>
                <p>To reset your password, click the link below:</p>
                <p><a href="%s" style="background-color: #4CAF50; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; display: inline-block;">Reset Password</a></p>
                <p>Or copy and paste this link into your browser:</p>
                <p style="word-break: break-all;">%s</p>
                <p><strong>This link will expire in 24 hours for security reasons.</strong></p>
                <p>If you didn't request this password reset, please ignore this email. Your password won't be changed.</p>
                <p>For security reasons, if you didn't make this request, please contact our support team immediately at %s</p>
                <p>Best regards,<br>%s Team</p>
                <hr>
                <p style="font-size: 12px; color: #666;">This is an automated message. Please do not reply to this email.</p>
            </body>
            </html>
            """,
                displayName,
                appName,
                resetLink,
                resetLink,
                supportEmail,
                appName
        );
    }

    private String buildEmailVerificationEmail(String email, String verificationToken, String displayName) {
        String verificationLink = String.format("%s/verify-email?token=%s&email=%s",
                appUrl, verificationToken, email);

        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif;">
                <h2>Verify Your Email Address</h2>
                <p>Dear %s,</p>
                <p>Thank you for registering with %s!</p>
                <p>Please verify your email address by clicking the button below:</p>
                <p><a href="%s" style="background-color: #2196F3; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; display: inline-block;">Verify Email Address</a></p>
                <p>Or copy and paste this link into your browser:</p>
                <p style="word-break: break-all;">%s</p>
                <p><strong>This verification link will expire in 48 hours.</strong></p>
                <p>Verifying your email address helps us ensure that we can:</p>
                <ul>
                    <li>Send you important order updates</li>
                    <li>Help you recover your account if needed</li>
                    <li>Keep your account secure</li>
                </ul>
                <p>If you didn't create an account with us, please ignore this email.</p>
                <p>Best regards,<br>%s Team</p>
                <hr>
                <p style="font-size: 12px; color: #666;">This is an automated message. Please do not reply to this email.</p>
            </body>
            </html>
            """,
                displayName,
                appName,
                verificationLink,
                verificationLink,
                appName
        );
    }

    private String buildPasswordChangeConfirmationEmail(String email, String displayName) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif;">
                <h2>Password Changed Successfully</h2>
                <p>Dear %s,</p>
                <p>Your password for %s has been successfully changed.</p>
                <p><strong>Changed on:</strong> %s</p>
                <p>If you made this change, no further action is required.</p>
                <p style="color: #d32f2f;"><strong>If you did NOT make this change:</strong></p>
                <ul style="color: #d32f2f;">
                    <li>Your account may be compromised</li>
                    <li>Contact our support team immediately at %s</li>
                    <li>Request a password reset using the "Forgot Password" link</li>
                </ul>
                <p>For your security, we recommend:</p>
                <ul>
                    <li>Using a unique password for each online account</li>
                    <li>Enabling two-factor authentication when available</li>
                    <li>Regularly updating your passwords</li>
                </ul>
                <p>Best regards,<br>%s Security Team</p>
                <hr>
                <p style="font-size: 12px; color: #666;">This is a security notification. Please do not reply to this email.</p>
            </body>
            </html>
            """,
                displayName,
                appName,
                timestamp,
                supportEmail,
                appName
        );
    }

    private String buildOrderConfirmationEmail(Order order) {
        // Use Order's helper method to get customer name
        String customerName = order.getCustomerFullName();

        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif;">
                <h2>Order Confirmation</h2>
                <p>Dear %s,</p>
                <p>Thank you for your order! We've received your order and it's being processed.</p>
                <h3>Order Details:</h3>
                <ul>
                    <li>Order Number: <strong>%s</strong></li>
                    <li>Order Date: %s</li>
                    <li>Total Amount: %s %.2f</li>
                    <li>Status: %s</li>
                </ul>
                <h3>Shipping Address:</h3>
                <p>%s</p>
                <p>You can track your order at: <a href="%s/orders/%s">Track Order</a></p>
                <p>Thank you for shopping with us!</p>
                <p>Best regards,<br>%s Team</p>
            </body>
            </html>
            """,
                customerName,
                order.getOrderNumber(),
                order.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                order.getCurrency(),
                order.getTotalAmount(),
                order.getOrderStatus(),
                order.getFormattedShippingAddress().replace("\n", "<br>"),
                appUrl, order.getOrderNumber(),
                appName
        );
    }

    private String buildShippingNotificationEmail(Shipment shipment) {
        Order order = shipment.getOrder();
        String customerName = order.getCustomerFullName();

        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif;">
                <h2>Your Order Has Shipped!</h2>
                <p>Dear %s,</p>
                <p>Great news! Your order has been shipped and is on its way to you.</p>
                <h3>Shipping Details:</h3>
                <ul>
                    <li>Order Number: <strong>%s</strong></li>
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
                customerName,
                order.getOrderNumber(),
                shipment.getTrackingNumber(),
                shipment.getCarrier(),
                shipment.getEstimatedDeliveryDate() != null ?
                        shipment.getEstimatedDeliveryDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) :
                        "To be determined",
                appUrl, shipment.getTrackingNumber(),
                appName
        );
    }

    private String buildDeliveryConfirmationEmail(Order order) {
        String customerName = order.getCustomerFullName();

        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif;">
                <h2>Order Delivered Successfully!</h2>
                <p>Dear %s,</p>
                <p>Your order %s has been delivered successfully.</p>
                <p>We hope you're happy with your purchase!</p>
                <p>If you have any questions or concerns, please don't hesitate to contact us.</p>
                <p>Best regards,<br>%s Team</p>
            </body>
            </html>
            """,
                customerName,
                order.getOrderNumber(),
                appName
        );
    }

    private String buildCancellationEmail(Order order, String reason) {
        String customerName = order.getCustomerFullName();

        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif;">
                <h2>Order Cancellation Confirmation</h2>
                <p>Dear %s,</p>
                <p>Your order %s has been cancelled.</p>
                %s
                <p>If you paid for this order, a refund will be processed within 3-5 business days.</p>
                <p>If you have any questions, please contact our customer service.</p>
                <p>Best regards,<br>%s Team</p>
            </body>
            </html>
            """,
                customerName,
                order.getOrderNumber(),
                reason != null ? "<p>Reason: " + reason + "</p>" : "",
                appName
        );
    }

    private String buildPaymentConfirmationEmail(Order order, BigDecimal amount) {
        String customerName = order.getCustomerFullName();

        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif;">
                <h2>Payment Received</h2>
                <p>Dear %s,</p>
                <p>We've received your payment of %s %.2f for order %s.</p>
                <p>Your order will be processed and shipped soon.</p>
                <p>Thank you!</p>
                <p>Best regards,<br>%s Team</p>
            </body>
            </html>
            """,
                customerName,
                order.getCurrency(),
                amount,
                order.getOrderNumber(),
                appName
        );
    }

    private String buildRefundEmail(Order order, BigDecimal refundAmount) {
        String customerName = order.getCustomerFullName();

        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif;">
                <h2>Refund Processed</h2>
                <p>Dear %s,</p>
                <p>A refund of %s %.2f has been processed for order %s.</p>
                <p>The refund should appear in your account within 3-5 business days.</p>
                <p>If you have any questions, please contact us.</p>
                <p>Best regards,<br>%s Team</p>
            </body>
            </html>
            """,
                customerName,
                order.getCurrency(),
                refundAmount,
                order.getOrderNumber(),
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
                customer.getCustomerFullName(),
                appUrl,
                appName
        );
    }

    /* VERSION 2.0 - ADVANCED EMAIL FEATURES (kommenterat bort för v1.0)
     *
     * I v2.0 kommer denna service inkludera:
     * - Template engine integration (Thymeleaf/Freemarker)
     * - Marketing emails och nyhetsbrev
     * - Påminnelser om övergivna kundvagnar
     * - Produktrekommendationer
     * - Kundsegmentering
     * - Email tracking och analytics
     * - A/B-testning för email-kampanjer
     * - Hantering av avprenumerationer
     * - Email-kö med retry-logik
     * - Rich HTML-mallar med bilder
     * - Flerspråksstöd
     * - SMS-notifieringar
     * - Push-notifieringar
     * - Two-factor authentication emails
     * - Account activity alerts
     * - Order status change notifications
     * - Back-in-stock notifications
     * - Review request emails
     * - Birthday/anniversary emails
     */
}