package com.perfect8.email.service.impl;

import com.perfect8.email.dto.*;
import com.perfect8.email.entity.EmailLog;
import com.perfect8.email.enums.EmailStatus;
import com.perfect8.email.exception.EmailException;
import com.perfect8.email.repository.EmailLogRepository;
import com.perfect8.email.service.EmailService;
import com.perfect8.email.service.TemplateService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Email service implementation
 * Version 1.0 - Core email functionality
 */
@Slf4j
@Service
@Transactional
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private EmailLogRepository emailLogRepository;

    @Autowired(required = false)
    private TemplateService templateService;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.company.name:Perfect8}")
    private String companyName;

    @Value("${app.company.support-email:support@perfect8.com}")
    private String supportEmail;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${email.max-retries:3}")
    private int maxRetries;

    @Override
    public EmailResponseDto sendEmail(EmailRequestDto request) {
        String emailId = UUID.randomUUID().toString();

        try {
            validateEmailRequest(request);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(request.getFromEmail() != null ? request.getFromEmail() : fromEmail);
            helper.setTo(request.getTo());

            if (StringUtils.hasText(request.getCc())) {
                helper.setCc(request.getCc());
            }
            if (StringUtils.hasText(request.getBcc())) {
                helper.setBcc(request.getBcc());
            }
            if (StringUtils.hasText(request.getReplyTo())) {
                helper.setReplyTo(request.getReplyTo());
            }

            helper.setSubject(request.getSubject());

            // Determine content to send
            String content = determineContent(request);
            boolean isHtml = request.hasHtmlContent() || request.hasTemplate();
            helper.setText(content, isHtml);

            // Add attachments if any
            if (request.hasAttachments()) {
                processAttachments(helper, request.getAttachments());
            }

            mailSender.send(mimeMessage);

            logEmail(emailId, request, EmailLog.Status.SENT, null);

            log.info("Email sent successfully to: {} with ID: {}", request.getTo(), emailId);
            return EmailResponseDto.success(emailId, request.getTo(), EmailStatus.SENT);

        } catch (MessagingException | MailException e) {
            log.error("Failed to send email to: {}", request.getTo(), e);
            logEmail(emailId, request, EmailLog.Status.FAILED, e.getMessage());
            return EmailResponseDto.failed(request.getTo(), e.getMessage(), "EMAIL_SEND_FAILED");
        }
    }

    @Override
    public EmailResponseDto sendOrderConfirmation(Long orderId, String customerEmail) {
        try {
            // In a real implementation, this would fetch from shop-service
            Map<String, Object> orderData = new HashMap<>();
            orderData.put("orderId", orderId);
            orderData.put("customerEmail", customerEmail);
            orderData.put("orderDate", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            String htmlContent = createOrderConfirmationHtml(orderData);

            EmailRequestDto request = EmailRequestDto.builder()
                    .to(customerEmail)
                    .subject("Order Confirmation #" + orderId)
                    .htmlBody(htmlContent)
                    .emailType("ORDER_CONFIRMATION")
                    .orderId(orderId.toString())
                    .isHighPriority(true)
                    .build();

            return sendEmail(request);
        } catch (Exception e) {
            log.error("Failed to send order confirmation for order: {}", orderId, e);
            return EmailResponseDto.failed(customerEmail, e.getMessage(), "ORDER_CONFIRMATION_FAILED");
        }
    }

    @Override
    public EmailResponseDto sendShippingNotification(Long orderId, String trackingNumber, String customerEmail) {
        try {
            Map<String, Object> shippingData = new HashMap<>();
            shippingData.put("orderId", orderId);
            shippingData.put("trackingNumber", trackingNumber);
            shippingData.put("customerEmail", customerEmail);

            String htmlContent = createOrderShippedHtml(shippingData);

            EmailRequestDto request = EmailRequestDto.builder()
                    .to(customerEmail)
                    .subject("Your Order #" + orderId + " Has Been Shipped!")
                    .htmlBody(htmlContent)
                    .emailType("SHIPPING_NOTIFICATION")
                    .orderId(orderId.toString())
                    .build();

            return sendEmail(request);
        } catch (Exception e) {
            log.error("Failed to send shipping notification for order: {}", orderId, e);
            return EmailResponseDto.failed(customerEmail, e.getMessage(), "SHIPPING_NOTIFICATION_FAILED");
        }
    }

    @Override
    public EmailResponseDto sendPasswordReset(String email, String resetToken) {
        try {
            String htmlContent = createPasswordResetHtml(email, resetToken);

            EmailRequestDto request = EmailRequestDto.builder()
                    .to(email)
                    .subject("Password Reset Request - " + companyName)
                    .htmlBody(htmlContent)
                    .emailType("PASSWORD_RESET")
                    .isHighPriority(true)
                    .build();

            return sendEmail(request);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", email, e);
            return EmailResponseDto.failed(email, e.getMessage(), "PASSWORD_RESET_FAILED");
        }
    }

    @Override
    public List<EmailResponseDto> sendBulkEmails(BulkEmailRequestDto request) {
        List<EmailResponseDto> responses = new ArrayList<>();

        int batchSize = request.getEffectiveBatchSize();
        int delaySeconds = request.getEffectiveDelay();

        List<List<String>> batches = partition(request.getRecipients(), batchSize);

        for (List<String> batch : batches) {
            for (String recipient : batch) {
                try {
                    EmailRequestDto emailRequest = EmailRequestDto.builder()
                            .to(recipient)
                            .subject(request.getSubject())
                            .body(request.getBody())
                            .htmlBody(request.getHtmlBody())
                            .templateName(request.getTemplateName())
                            .templateVariables(request.getCommonVariables())
                            .emailType(request.getEmailType())
                            .build();

                    EmailResponseDto response = sendEmail(emailRequest);
                    responses.add(response);

                } catch (Exception e) {
                    log.error("Failed to send bulk email to: {}", recipient, e);
                    responses.add(EmailResponseDto.failed(recipient, e.getMessage(), "BULK_EMAIL_FAILED"));
                }
            }

            // Delay between batches
            if (delaySeconds > 0) {
                try {
                    Thread.sleep(delaySeconds * 1000L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("Bulk email batch delay interrupted");
                }
            }
        }

        return responses;
    }

    @Override
    public EmailStatusDto getEmailStatus(String emailId) {
        Optional<EmailLog> emailLog = emailLogRepository.findByEmailId(emailId);

        if (emailLog.isPresent()) {
            EmailLog log = emailLog.get();
            return EmailStatusDto.builder()
                    .emailId(log.getEmailId())
                    .status(mapLogStatusToEmailStatus(log.getStatus()))
                    .recipient(log.getRecipient())
                    .subject(log.getSubject())
                    .sentAt(log.getSentAt())
                    .lastStatusUpdate(log.getUpdatedAt())
                    .attemptCount(log.getRetryCount())
                    .build();
        }

        return EmailStatusDto.builder()
                .emailId(emailId)
                .status("UNKNOWN")
                .build();
    }

    @Override
    public List<String> getAvailableTemplates() {
        if (templateService != null) {
            return templateService.getAvailableTemplates();
        }
        return Arrays.asList(
                "order-confirmation",
                "order-shipped",
                "order-cancelled",
                "password-reset",
                "welcome",
                "newsletter"
        );
    }

    @Override
    public EmailResponseDto sendTestEmail(String recipientEmail) {
        String testContent = String.format("""
            <html>
            <body>
                <h2>Test Email from %s</h2>
                <p>This is a test email to verify email service configuration.</p>
                <p>Timestamp: %s</p>
                <p>If you received this, the email service is working correctly!</p>
            </body>
            </html>
            """, companyName, LocalDateTime.now());

        EmailRequestDto request = EmailRequestDto.builder()
                .to(recipientEmail)
                .subject("Test Email - " + companyName)
                .htmlBody(testContent)
                .emailType("TEST")
                .build();

        return sendEmail(request);
    }

    // Helper methods

    private void validateEmailRequest(EmailRequestDto request) {
        if (!StringUtils.hasText(request.getTo())) {
            throw new EmailException(EmailException.ErrorCode.INVALID_RECIPIENT,
                    "Recipient email is required");
        }
        if (!StringUtils.hasText(request.getSubject())) {
            throw new IllegalArgumentException("Email subject is required");
        }
        if (!StringUtils.hasText(request.getContent()) &&
                !StringUtils.hasText(request.getBody()) &&
                !StringUtils.hasText(request.getHtmlBody()) &&
                !request.hasTemplate()) {
            throw new IllegalArgumentException("Email content is required");
        }
    }

    private String determineContent(EmailRequestDto request) {
        if (request.hasTemplate() && templateService != null) {
            return templateService.processTemplate(
                    request.getTemplateName(),
                    request.getTemplateVariables()
            );
        }
        if (StringUtils.hasText(request.getHtmlBody())) {
            return request.getHtmlBody();
        }
        if (StringUtils.hasText(request.getBody())) {
            return request.getBody();
        }
        return request.getContent();
    }

    private void processAttachments(MimeMessageHelper helper, List<AttachmentDto> attachments)
            throws MessagingException {
        for (AttachmentDto attachment : attachments) {
            if (attachment.isInline() && StringUtils.hasText(attachment.getContentId())) {
                helper.addInline(attachment.getContentId(),
                        new jakarta.mail.util.ByteArrayDataSource(
                                attachment.getContent(),
                                attachment.getContentType()
                        ));
            } else {
                helper.addAttachment(attachment.getFilename(),
                        new jakarta.mail.util.ByteArrayDataSource(
                                attachment.getContent(),
                                attachment.getContentType()
                        ));
            }
        }
    }

    private void logEmail(String emailId, EmailRequestDto request, EmailLog.Status status, String failureReason) {
        try {
            EmailLog log = EmailLog.builder()
                    .emailId(emailId)
                    .recipient(request.getTo())
                    .subject(request.getSubject())
                    .status(status)
                    .failureReason(failureReason)
                    .sentAt(LocalDateTime.now())
                    .templateName(request.getTemplateName())
                    .build();

            emailLogRepository.save(log);
        } catch (Exception e) {
            log.error("Failed to log email: {}", emailId, e);
        }
    }

    private String mapLogStatusToEmailStatus(EmailLog.Status status) {
        return status != null ? status.name() : "UNKNOWN";
    }

    private <T> List<List<T>> partition(List<T> list, int size) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            partitions.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return partitions;
    }

    // HTML content generators (simplified versions)

    private String createOrderConfirmationHtml(Map<String, Object> orderData) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <h2 style="color: #007bff;">Order Confirmation</h2>
                <p>Thank you for your order!</p>
                <div style="background-color: #f8f9fa; padding: 20px; border-radius: 5px; margin: 20px 0;">
                    <h3>Order Details:</h3>
                    <p><strong>Order ID:</strong> %s</p>
                    <p><strong>Order Date:</strong> %s</p>
                </div>
                <p>We'll send you another email when your order ships.</p>
                <p>Best regards,<br>%s Team</p>
            </body>
            </html>
            """,
                orderData.get("orderId"),
                orderData.get("orderDate"),
                companyName
        );
    }

    private String createOrderShippedHtml(Map<String, Object> shippingData) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <h2 style="color: #007bff;">Your Order Has Shipped!</h2>
                <p>Great news! Your order #%s has been shipped.</p>
                <div style="background-color: #e7f3ff; padding: 20px; border-radius: 5px; margin: 20px 0;">
                    <h3>Tracking Information:</h3>
                    <p><strong>Tracking Number:</strong> %s</p>
                </div>
                <p>Best regards,<br>%s Team</p>
            </body>
            </html>
            """,
                shippingData.get("orderId"),
                shippingData.get("trackingNumber"),
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
                <p>We received a request to reset your password.</p>
                <div style="background-color: #fff3cd; padding: 20px; border-radius: 5px; margin: 20px 0;">
                    <p>Click the link below to reset your password:</p>
                    <a href="%s" style="background-color: #dc3545; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">Reset Password</a>
                </div>
                <p>This link expires in 24 hours.</p>
                <p>Best regards,<br>%s Team</p>
            </body>
            </html>
            """,
                resetLink,
                companyName
        );
    }
}