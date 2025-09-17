package com.perfect8.email.service;

import com.perfect8.email.dto.EmailResponseDto;
import com.perfect8.email.entity.EmailLog;
import com.perfect8.email.enums.EmailStatus;
import com.perfect8.email.model.EmailMessage;
import com.perfect8.email.repository.EmailLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Email Service - Version 1.0
 * Core email functionality
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailLogRepository emailLogRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${spring.mail.from-name:Perfect8}")
    private String fromName;

    /**
     * Send email (legacy method for compatibility)
     */
    @Transactional
    public boolean sendEmail(String recipientEmail, String subject, String content, boolean isHtml) {
        EmailLog emailLog = new EmailLog();
        emailLog.setTrackingId(UUID.randomUUID().toString());
        emailLog.setRecipient(recipientEmail);
        emailLog.setSubject(subject);
        emailLog.setBody(content);
        emailLog.setHtml(isHtml);
        emailLog.setStatus(EmailLog.Status.PENDING);
        emailLog.setRetryCount(0);
        emailLog.setCreatedAt(LocalDateTime.now());
        emailLog.setFromEmail(fromEmail);
        emailLog.setMessageType("DIRECT");

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(recipientEmail);
            helper.setSubject(subject);
            helper.setFrom(fromEmail, fromName);
            helper.setText(content, isHtml);

            mailSender.send(message);

            emailLog.setStatus(EmailLog.Status.SENT);
            emailLog.setSentAt(LocalDateTime.now());
            emailLogRepository.save(emailLog);

            log.info("Email sent successfully to {}", recipientEmail);
            return true;

        } catch (Exception e) {
            emailLog.setStatus(EmailLog.Status.FAILED);
            emailLog.setErrorMessage(e.getMessage());
            emailLog.setRetryCount(emailLog.getRetryCount() + 1);
            emailLogRepository.save(emailLog);

            log.error("Failed to send email to {}: {}", recipientEmail, e.getMessage());
            return false;
        }
    }

    /**
     * Three-parameter version for compatibility
     */
    public boolean sendEmail(String recipientEmail, String subject, String content) {
        return sendEmail(recipientEmail, subject, content, false);
    }

    /**
     * Send HTML email
     */
    public boolean sendHtmlEmail(String recipientEmail, String subject, String htmlContent) {
        return sendEmail(recipientEmail, subject, htmlContent, true);
    }

    /**
     * Send text email
     */
    public boolean sendTextEmail(String recipientEmail, String subject, String textContent) {
        return sendEmail(recipientEmail, subject, textContent, false);
    }

    /**
     * Send bulk emails (for newsletter service)
     */
    public int sendBulkEmails(List<String> recipients, String subject, String content) {
        if (recipients == null || recipients.isEmpty()) {
            return 0;
        }

        int successCount = 0;
        for (String recipientEmail : recipients) {
            if (sendHtmlEmail(recipientEmail, subject, content)) {
                successCount++;
            }
            // Small delay to avoid overwhelming mail server
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        log.info("Bulk email sent to {}/{} recipients", successCount, recipients.size());
        return successCount;
    }

    /**
     * Send email using EmailMessage object (future v2.0)
     */
    public boolean sendEmail(EmailMessage emailMessage) {
        if (emailMessage == null) {
            return false;
        }

        String recipientEmail = emailMessage.getRecipientEmail();
        String content = emailMessage.getContent() != null ?
                emailMessage.getContent() : emailMessage.getBody();

        return sendEmail(
                recipientEmail,
                emailMessage.getSubject(),
                content,
                emailMessage.isHtml()
        );
    }

    /**
     * Get recent emails
     */
    public List<EmailResponseDto> getRecentEmails() {
        List<EmailLog> logs = emailLogRepository.findTop100ByOrderByCreatedAtDesc();

        return logs.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get email logs (alias for getRecentEmails)
     */
    public List<EmailResponseDto> getEmailLogs() {
        return getRecentEmails();
    }

    /**
     * Convert EmailLog to DTO
     */
    private EmailResponseDto convertToDto(EmailLog log) {
        EmailResponseDto dto = new EmailResponseDto();
        dto.setEmailId(log.getEmailId());
        dto.setTrackingId(log.getTrackingId());
        dto.setRecipient(log.getRecipient());
        dto.setSubject(log.getSubject());

        // Convert EmailLog.Status to EmailStatus enum
        EmailStatus emailStatus = convertStatus(log.getStatus());
        dto.setStatus(emailStatus);

        dto.setCreatedAt(log.getCreatedAt());
        dto.setSentAt(log.getSentAt());
        return dto;
    }

    /**
     * Convert EmailLog.Status to EmailStatus
     */
    private EmailStatus convertStatus(EmailLog.Status status) {
        if (status == null) {
            return EmailStatus.PENDING;
        }

        switch (status) {
            case SENT:
                return EmailStatus.SENT;
            case FAILED:
                return EmailStatus.FAILED;
            case QUEUED:
                return EmailStatus.QUEUED;
            case DELIVERED:
                return EmailStatus.DELIVERED;
            case BOUNCED:
                return EmailStatus.BOUNCED;
            case PENDING:
            default:
                return EmailStatus.PENDING;
        }
    }

    /**
     * Check if email was recently sent
     */
    public boolean wasRecentlySent(String recipientEmail, String subject, int minutesAgo) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(minutesAgo);
        return emailLogRepository.existsByRecipientAndSubjectAndCreatedAtAfter(
                recipientEmail, subject, since);
    }
}