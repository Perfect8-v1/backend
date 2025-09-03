package com.perfect8.email.controller;

import com.perfect8.email.dto.*;
import com.perfect8.email.enums.EmailStatus;
import com.perfect8.email.service.EmailService;
import com.perfect8.email.service.NewsletterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for email operations
 * Version 1.0 - Core email functionality
 */
@Slf4j
@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;
    private final NewsletterService newsletterService;

    @PostMapping("/send")
    public ResponseEntity<EmailResponseDto> sendEmail(@Valid @RequestBody EmailRequestDto request) {
        try {
            EmailResponseDto response = emailService.sendEmail(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to send email", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(EmailResponseDto.failed(request.getTo(), e.getMessage(), "EMAIL_SEND_FAILED"));
        }
    }

    @PostMapping("/send-order-confirmation")
    public ResponseEntity<EmailResponseDto> sendOrderConfirmation(
            @RequestParam Long orderId,
            @RequestParam String customerEmail) {
        try {
            EmailResponseDto response = emailService.sendOrderConfirmation(orderId, customerEmail);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to send order confirmation for order: {}", orderId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(EmailResponseDto.failed(customerEmail, e.getMessage(), "ORDER_CONFIRMATION_FAILED"));
        }
    }

    @PostMapping("/send-shipping-notification")
    public ResponseEntity<EmailResponseDto> sendShippingNotification(
            @RequestParam Long orderId,
            @RequestParam String trackingNumber,
            @RequestParam String customerEmail) {
        try {
            EmailResponseDto response = emailService.sendShippingNotification(orderId, trackingNumber, customerEmail);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to send shipping notification for order: {}", orderId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(EmailResponseDto.failed(customerEmail, e.getMessage(), "SHIPPING_NOTIFICATION_FAILED"));
        }
    }

    @PostMapping("/send-password-reset")
    public ResponseEntity<EmailResponseDto> sendPasswordReset(
            @RequestParam String email,
            @RequestParam String resetToken) {
        try {
            EmailResponseDto response = emailService.sendPasswordReset(email, resetToken);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", email, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(EmailResponseDto.failed(email, e.getMessage(), "PASSWORD_RESET_FAILED"));
        }
    }

    @PostMapping("/send-bulk")
    public ResponseEntity<List<EmailResponseDto>> sendBulkEmails(@Valid @RequestBody BulkEmailRequestDto request) {
        try {
            List<EmailResponseDto> responses = emailService.sendBulkEmails(request);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Failed to send bulk emails", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/status/{emailId}")
    public ResponseEntity<EmailStatusDto> getEmailStatus(@PathVariable String emailId) {
        try {
            EmailStatusDto status = emailService.getEmailStatus(emailId);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("Failed to get email status for: {}", emailId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/templates")
    public ResponseEntity<List<String>> getAvailableTemplates() {
        try {
            List<String> templates = emailService.getAvailableTemplates();
            return ResponseEntity.ok(templates);
        } catch (Exception e) {
            log.error("Failed to get available templates", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/test")
    public ResponseEntity<EmailResponseDto> sendTestEmail(@RequestParam String recipientEmail) {
        try {
            EmailResponseDto response = emailService.sendTestEmail(recipientEmail);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to send test email to: {}", recipientEmail, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(EmailResponseDto.failed(recipientEmail, e.getMessage(), "TEST_EMAIL_FAILED"));
        }
    }

    @PostMapping("/newsletter")
    public ResponseEntity<String> sendNewsletter(@Valid @RequestBody NewsletterRequestDto request) {
        try {
            newsletterService.sendNewsletter(request);
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body("Newsletter queued for " + request.getRecipientCount() + " recipients");
        } catch (Exception e) {
            log.error("Failed to send newsletter", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send newsletter: " + e.getMessage());
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Email service is running");
    }
}