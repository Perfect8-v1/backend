package perfect8.email.service.impl;

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

import java.time.format.DateTimeFormatter;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired
    private JavaMailSender mailSender;

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

            String htmlContent = createOrderConfirmationHtml(order);

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

            String htmlContent = createOrderShippedHtml(order, trackingNumber);

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

            String htmlContent = createOrderCancelledHtml(order, reason);

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
            String htmlContent = createPasswordResetHtml(email, resetToken);

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

            String htmlContent = createWelcomeHtml(customer);

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

    // Enkla HTML-generators (kan senare ersättas med Flutter-genererat innehåll)
    private String createOrderConfirmationHtml(Order order) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <h2 style="color: #007bff;">Order Confirmation</h2>
                <p>Dear %s,</p>
                <p>Thank you for your order! Your order #%s has been confirmed.</p>
                <div style="background-color: #f8f9fa; padding: 20px; border-radius: 5px; margin: 20px 0;">
                    <h3>Order Details:</h3>
                    <p><strong>Order ID:</strong> %s</p>
                    <p><strong>Order Date:</strong> %s</p>
                    <p><strong>Total Amount:</strong> $%.2f</p>
                </div>
                <p>We'll send you another email when your order ships.</p>
                <p>Best regards,<br>%s Team</p>
            </body>
            </html>
            """,
                order.getCustomer().getFirstName(),
                order.getId(),
                order.getId(),
                order.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                order.getTotalAmount(),
                companyName
        );
    }

    private String createOrderShippedHtml(Order order, String trackingNumber) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <h2 style="color: #007bff;">Your Order Has Shipped!</h2>
                <p>Dear %s,</p>
                <p>Great news! Your order #%s has been shipped.</p>
                <div style="background-color: #e7f3ff; padding: 20px; border-radius: 5px; margin: 20px 0;">
                    <h3>Tracking Information:</h3>
                    <p><strong>Tracking Number:</strong> %s</p>
                    <p><strong>Expected Delivery:</strong> 2-3 business days</p>
                </div>
                <p>Best regards,<br>%s Team</p>
            </body>
            </html>
            """,
                order.getCustomer().getFirstName(),
                order.getId(),
                trackingNumber,
                companyName
        );
    }

    private String createOrderCancelledHtml(Order order, String reason) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <h2 style="color: #dc3545;">Order Cancelled</h2>
                <p>Dear %s,</p>
                <p>We're sorry to inform you that your order #%s has been cancelled.</p>
                <div style="background-color: #f8d7da; padding: 20px; border-radius: 5px; margin: 20px 0;">
                    <h3>Cancellation Details:</h3>
                    <p><strong>Reason:</strong> %s</p>
                    <p><strong>Refund:</strong> Your refund of $%.2f will be processed within 3-5 business days.</p>
                </div>
                <p>Best regards,<br>%s Team</p>
            </body>
            </html>
            """,
                order.getCustomer().getFirstName(),
                order.getId(),
                reason,
                order.getTotalAmount(),
                companyName
        );
    }

    private String createPasswordResetHtml(String email, String resetToken) {
        String resetLink = frontendUrl + "/reset-password?token=" + resetToken;
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <h2 style="color: #dc3545;">Password Reset Request</h2>
                <p>Hello,</p>
                <p>We received a request to reset your password for your %s account.</p>
                <div style="background-color: #fff3cd; padding: 20px; border-radius: 5px; margin: 20px 0;">
                    <p>Click the link below to reset your password:</p>
                    <a href="%s" style="background-color: #dc3545; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">Reset Password</a>
                </div>
                <p>This link expires in 24 hours.</p>
                <p>If you didn't request this, please ignore this email.</p>
                <p>Best regards,<br>%s Team</p>
            </body>
            </html>
            """,
                companyName,
                resetLink,
                companyName
        );
    }

    private String createWelcomeHtml(Customer customer) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <h2 style="color: #28a745;">Welcome to %s!</h2>
                <p>Dear %s,</p>
                <p>Thank you for joining %s! We're excited to have you as part of our community.</p>
                <div style="background-color: #d4edda; padding: 20px; border-radius: 5px; margin: 20px 0;">
                    <h3>What's Next?</h3>
                    <ul>
                        <li>Browse our latest products</li>
                        <li>Enjoy member-exclusive discounts</li>
                        <li>Get personalized recommendations</li>
                    </ul>
                </div>
                <p>Use code <strong>WELCOME10</strong> for 10%% off your first order!</p>
                <p>Happy shopping!<br>%s Team</p>
            </body>
            </html>
            """,
                companyName,
                customer.getFirstName(),
                companyName,
                companyName
        );
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