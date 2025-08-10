package com.perfect8.webshop.service.impl;

import com.perfect8.webshop.dto.EmailRequest;
import com.perfect8.webshop.model.Order;
import com.perfect8.webshop.model.Customer;
import com.perfect8.webshop.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.company.name:Perfect8}")
    private String companyName;

    @Value("${app.company.support-email:support@perfect8.com}")
    private String supportEmail;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
            logger.info("Simple email sent successfully to: {}", to);
        } catch (MailException e) {
            logger.error("Failed to send simple email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Override
    public void sendHtmlEmail(EmailRequest emailRequest) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(emailRequest.getTo());
            helper.setSubject(emailRequest.getSubject());
            helper.setText(emailRequest.getContent(), true);

            mailSender.send(mimeMessage);
            logger.info("HTML email sent successfully to: {}", emailRequest.getTo());
        } catch (MessagingException | MailException e) {
            logger.error("Failed to send HTML email to: {}", emailRequest.getTo(), e);
            throw new RuntimeException("Failed to send HTML email", e);
        }
    }

    @Override
    public void sendOrderConfirmationEmail(Order order) {
        try {
            Customer customer = order.getCustomer();
            if (customer == null || !StringUtils.hasText(customer.getEmail())) {
                logger.warn("Cannot send order confirmation: customer or email is null for order {}", order.getId());
                return;
            }

            Context context = createOrderContext(order);
            String htmlContent = templateEngine.process("order-confirmation", context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(customer.getEmail());
            helper.setSubject("Order Confirmation #" + order.getId());
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            logger.info("Order confirmation email sent successfully for order {} to: {}",
                    order.getId(), customer.getEmail());
        } catch (MessagingException | MailException e) {
            logger.error("Failed to send order confirmation email for order: {}", order.getId(), e);
            throw new RuntimeException("Failed to send order confirmation email", e);
        }
    }

    @Override
    public void sendOrderShippedEmail(Order order, String trackingNumber) {
        try {
            Customer customer = order.getCustomer();
            if (customer == null || !StringUtils.hasText(customer.getEmail())) {
                logger.warn("Cannot send shipping notification: customer or email is null for order {}", order.getId());
                return;
            }

            Context context = createOrderContext(order);
            context.setVariable("trackingNumber", trackingNumber);
            context.setVariable("shippedDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

            String htmlContent = templateEngine.process("order-shipped", context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(customer.getEmail());
            helper.setSubject("Your Order #" + order.getId() + " Has Been Shipped!");
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            logger.info("Order shipped email sent successfully for order {} to: {}",
                    order.getId(), customer.getEmail());
        } catch (MessagingException | MailException e) {
            logger.error("Failed to send order shipped email for order: {}", order.getId(), e);
            throw new RuntimeException("Failed to send order shipped email", e);
        }
    }

    @Override
    public void sendOrderCancelledEmail(Order order, String reason) {
        try {
            Customer customer = order.getCustomer();
            if (customer == null || !StringUtils.hasText(customer.getEmail())) {
                logger.warn("Cannot send cancellation notification: customer or email is null for order {}", order.getId());
                return;
            }

            Context context = createOrderContext(order);
            context.setVariable("cancellationReason", reason);
            context.setVariable("cancelledDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

            String htmlContent = templateEngine.process("order-cancelled", context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(customer.getEmail());
            helper.setSubject("Order Cancellation Notice #" + order.getId());
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            logger.info("Order cancelled email sent successfully for order {} to: {}",
                    order.getId(), customer.getEmail());
        } catch (MessagingException | MailException e) {
            logger.error("Failed to send order cancelled email for order: {}", order.getId(), e);
            throw new RuntimeException("Failed to send order cancelled email", e);
        }
    }

    @Override
    public void sendPasswordResetEmail(String email, String resetToken) {
        try {
            Context context = new Context();
            context.setVariable("resetLink", frontendUrl + "/reset-password?token=" + resetToken);
            context.setVariable("companyName", companyName);
            context.setVariable("supportEmail", supportEmail);
            context.setVariable("expirationTime", "24 hours");

            String htmlContent = templateEngine.process("password-reset", context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Password Reset Request - " + companyName);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            logger.info("Password reset email sent successfully to: {}", email);
        } catch (MessagingException | MailException e) {
            logger.error("Failed to send password reset email to: {}", email, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    @Override
    public void sendWelcomeEmail(Customer customer) {
        try {
            if (customer == null || !StringUtils.hasText(customer.getEmail())) {
                logger.warn("Cannot send welcome email: customer or email is null");
                return;
            }

            Context context = new Context();
            context.setVariable("customerName", customer.getFirstName());
            context.setVariable("companyName", companyName);
            context.setVariable("supportEmail", supportEmail);
            context.setVariable("websiteUrl", frontendUrl);

            String htmlContent = templateEngine.process("welcome", context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(customer.getEmail());
            helper.setSubject("Welcome to " + companyName + "!");
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            logger.info("Welcome email sent successfully to: {}", customer.getEmail());
        } catch (MessagingException | MailException e) {
            logger.error("Failed to send welcome email to: {}", customer.getEmail(), e);
            throw new RuntimeException("Failed to send welcome email", e);
        }
    }

    /**
     * Creates a Thymeleaf context with common order-related variables
     */
    private Context createOrderContext(Order order) {
        Context context = new Context();
        Customer customer = order.getCustomer();

        // Customer information
        context.setVariable("customerName", customer.getFirstName() + " " + customer.getLastName());
        context.setVariable("customerEmail", customer.getEmail());

        // Order information
        context.setVariable("orderId", order.getId());
        context.setVariable("orderDate", order.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        context.setVariable("orderStatus", order.getStatus().toString());
        context.setVariable("orderItems", order.getOrderItems());

        // Calculate totals
        BigDecimal subtotal = order.getOrderItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        context.setVariable("subtotal", subtotal);
        context.setVariable("shipping", order.getShippingCost());
        context.setVariable("tax", order.getTaxAmount());
        context.setVariable("totalAmount", order.getTotalAmount());

        // Shipping information
        if (order.getShippingAddress() != null) {
            context.setVariable("shippingAddress", order.getShippingAddress());
        }

        // Billing information
        if (order.getBillingAddress() != null) {
            context.setVariable("billingAddress", order.getBillingAddress());
        }

        // Company information
        context.setVariable("companyName", companyName);
        context.setVariable("supportEmail", supportEmail);
        context.setVariable("websiteUrl", frontendUrl);

        return context;
    }

    @Override
    public boolean isEmailServiceEnabled() {
        try {
            return mailSender != null && StringUtils.hasText(fromEmail);
        } catch (Exception e) {
            logger.error("Error checking email service availability", e);
            return false;
        }
    }

    @Override
    public void testEmailConnection() {
        try {
            // Test by creating a simple message without sending
            SimpleMailMessage testMessage = new SimpleMailMessage();
            testMessage.setFrom(fromEmail);
            testMessage.setTo("test@example.com");
            testMessage.setSubject("Connection Test");
            testMessage.setText("This is a connection test.");

            logger.info("Email service connection test successful");
        } catch (Exception e) {
            logger.error("Email service connection test failed", e);
            throw new RuntimeException("Email service connection test failed", e);
        }
    }
}