package com.perfect8.email.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.util.List;

/**
 * Email Service - Version 1.0
 * Simple email sending - Gmail handles all logging
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${spring.mail.from-name:Perfect8}")
    private String fromName;

    /**
     * Send email - simple version, Gmail logs everything
     */
    public boolean sendEmail(String recipientEmail, String subject, String content, boolean isHtml) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(recipientEmail);
            helper.setSubject(subject);
            helper.setFrom(fromEmail, fromName);
            helper.setText(content, isHtml);

            mailSender.send(message);

            log.info("Email sent to {} with subject: {}", recipientEmail, subject);
            return true;

        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", recipientEmail, e.getMessage());
            return false;
        }
    }

    /**
     * Three-parameter version for text emails
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
     * Send bulk emails
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
            try {
                Thread.sleep(100); // Small delay to avoid overwhelming Gmail
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        log.info("Bulk email sent to {}/{} recipients", successCount, recipients.size());
        return successCount;
    }
}