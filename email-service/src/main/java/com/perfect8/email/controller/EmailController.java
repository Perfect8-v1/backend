package com.perfect8.email.controller;

import com.perfect8.email.dto.*;
import com.perfect8.email.model.EmailMessage;
import com.perfect8.email.service.EmailService;
import com.perfect8.email.service.OrderEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Email Controller - Version 1.0
 * Core email endpoints only
 */
@Slf4j
@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;
    private final OrderEmailService orderEmailService;

    /**
     * Send email using object-oriented approach
     */
    @PostMapping("/send")
    public ResponseEntity<EmailResponseDto> sendEmail(@Valid @RequestBody EmailRequestDto request) {
        log.info("Received email request to: {}", request.getTo());

        try {
            // Create recipient list
            List<String> recipients = new ArrayList<>();
            recipients.add(request.getTo());

            // Convert DTO to EmailMessage object
            EmailMessage emailMessage = EmailMessage.builder()
                    .recipients(recipients)
                    .subject(request.getSubject())
                    .contentObject(request.getBody())
                    .messageType("API_DIRECT")
                    .build();

            // Send using object-oriented method
            boolean sent = emailService.sendEmail(emailMessage);

            if (sent) {
                EmailResponseDto response = EmailResponseDto.success(
                        null, // emailId will be set by service
                        null, // trackingId will be set by service
                        request.getTo(),
                        com.perfect8.email.enums.EmailStatus.SENT
                );
                return ResponseEntity.ok(response);
            } else {
                EmailResponseDto response = EmailResponseDto.failed(
                        request.getTo(),
                        EmailResponseDto.ErrorInfo.builder()
                                .errorMessage("Failed to send email")
                                .errorCategory("SEND_FAILURE")
                                .build()
                );
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
            }

        } catch (Exception e) {
            log.error("Error sending email to {}: {}", request.getTo(), e.getMessage(), e);
            EmailResponseDto response = EmailResponseDto.failed(
                    request.getTo(),
                    EmailResponseDto.ErrorInfo.builder()
                            .errorMessage(e.getMessage())
                            .errorCategory("EXCEPTION")
                            .build()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Send simple email (text only)
     */
    @PostMapping("/send-simple")
    public ResponseEntity<EmailResponseDto> sendSimpleEmail(@Valid @RequestBody EmailRequest request) {
        log.info("Sending simple email to: {}", request.getTo());

        try {
            // Determine if content is HTML
            boolean isHtml = request.getContent() != null &&
                    request.getContent().contains("<") &&
                    request.getContent().contains(">");

            // Send email
            boolean sent = emailService.sendEmail(
                    request.getTo(),
                    request.getSubject(),
                    request.getContent(),
                    isHtml
            );

            if (sent) {
                EmailResponseDto response = EmailResponseDto.success(
                        null,
                        null,
                        request.getTo(),
                        com.perfect8.email.enums.EmailStatus.SENT
                );
                return ResponseEntity.ok(response);
            } else {
                EmailResponseDto response = EmailResponseDto.failed(
                        request.getTo(),
                        EmailResponseDto.ErrorInfo.builder()
                                .errorMessage("Failed to send simple email")
                                .errorCategory("SEND_FAILURE")
                                .build()
                );
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
            }

        } catch (Exception e) {
            log.error("Error sending simple email: {}", e.getMessage(), e);
            EmailResponseDto response = EmailResponseDto.failed(
                    request.getTo(),
                    EmailResponseDto.ErrorInfo.builder()
                            .errorMessage(e.getMessage())
                            .errorCategory("EXCEPTION")
                            .build()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Send HTML email
     */
    @PostMapping("/send-html")
    public ResponseEntity<EmailResponseDto> sendHtmlEmail(@Valid @RequestBody EmailRequest request) {
        log.info("Sending HTML email to: {}", request.getTo());

        try {
            boolean sent = emailService.sendHtmlEmail(
                    request.getTo(),
                    request.getSubject(),
                    request.getContent()
            );

            if (sent) {
                EmailResponseDto response = EmailResponseDto.success(
                        null,
                        null,
                        request.getTo(),
                        com.perfect8.email.enums.EmailStatus.SENT
                );
                return ResponseEntity.ok(response);
            } else {
                EmailResponseDto response = EmailResponseDto.failed(
                        request.getTo(),
                        EmailResponseDto.ErrorInfo.builder()
                                .errorMessage("Failed to send HTML email")
                                .errorCategory("SEND_FAILURE")
                                .build()
                );
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
            }

        } catch (Exception e) {
            log.error("Error sending HTML email: {}", e.getMessage(), e);
            EmailResponseDto response = EmailResponseDto.failed(
                    request.getTo(),
                    EmailResponseDto.ErrorInfo.builder()
                            .errorMessage(e.getMessage())
                            .errorCategory("EXCEPTION")
                            .build()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get recent email logs
     */
    @GetMapping("/logs")
    public ResponseEntity<List<EmailResponseDto>> getRecentEmails() {
        try {
            List<EmailResponseDto> logs = emailService.getRecentEmails();
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            log.error("Error fetching email logs: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(List.of());
        }
    }

    /**
     * Send order status email
     * This is ACTIVE in v1.0 - critical for customer satisfaction!
     */
    @PostMapping("/order/status")
    public ResponseEntity<Map<String, String>> sendOrderStatusEmail(@Valid @RequestBody OrderEmailDTO orderDto) {
        try {
            orderEmailService.sendOrderStatusEmail(orderDto);

            Map<String, String> response = new HashMap<>();
            response.put("status", "sent");
            response.put("orderId", String.valueOf(orderDto.getOrderId()));
            response.put("orderStatus", orderDto.getOrderStatus().name());
            response.put("recipient", orderDto.getCustomerEmail());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error sending order email: {}", e.getMessage(), e);

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "failed");
            errorResponse.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "email-service");
        health.put("version", "1.0");
        return ResponseEntity.ok(health);
    }

    /**
     * Check if email was recently sent (duplicate prevention)
     */
    @GetMapping("/check-duplicate")
    public ResponseEntity<Map<String, Boolean>> checkDuplicate(
            @RequestParam String recipientEmail,
            @RequestParam String subject,
            @RequestParam(defaultValue = "5") int minutesAgo) {

        boolean wasSent = emailService.wasRecentlySent(recipientEmail, subject, minutesAgo);

        Map<String, Boolean> response = new HashMap<>();
        response.put("isDuplicate", wasSent);

        return ResponseEntity.ok(response);
    }

    // ============== Version 2.0 Features - COMMENTED OUT ==============

    /*
    @PostMapping("/bulk")
    public ResponseEntity<Map<String, Object>> sendBulkEmail(@RequestBody BulkEmailRequestDto request) {
        // Version 2.0 - Analytics and metrics
        return null;
    }

    @PostMapping("/campaign")
    public ResponseEntity<Map<String, Object>> sendCampaignEmail(@RequestBody CampaignEmailDto request) {
        // Version 2.0 - Marketing campaigns
        return null;
    }

    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getEmailAnalytics() {
        // Version 2.0 - Analytics dashboard
        return null;
    }

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getEmailMetrics() {
        // Version 2.0 - Performance metrics
        return null;
    }

    @PostMapping("/templates")
    public ResponseEntity<String> createTemplate(@RequestBody EmailTemplateDto template) {
        // Version 2.0 - Template management
        return null;
    }
    */
}