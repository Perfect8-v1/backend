package com.perfect8.email.controller;

import com.perfect8.email.dto.BulkEmailRequestDto;
import com.perfect8.email.dto.EmailRequestDto;
import com.perfect8.email.dto.EmailResponseDto;
import com.perfect8.email.dto.EmailStatusDto;
import com.perfect8.email.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/emails")
@RequiredArgsConstructor
@Tag(name = "Email Controller", description = "Email management endpoints")
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/send")
    @Operation(summary = "Send single email")
    public ResponseEntity<EmailResponseDto> sendEmail(@Valid @RequestBody EmailRequestDto request) {
        EmailResponseDto response = emailService.sendEmail(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/send-order-confirmation")
    @Operation(summary = "Send order confirmation email")
    public ResponseEntity<EmailResponseDto> sendOrderConfirmation(
            @RequestParam Long orderId,
            @RequestParam String customerEmail) {
        EmailResponseDto response = emailService.sendOrderConfirmation(orderId, customerEmail);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/send-shipping-notification")
    @Operation(summary = "Send shipping notification email")
    public ResponseEntity<EmailResponseDto> sendShippingNotification(
            @RequestParam Long orderId,
            @RequestParam String trackingNumber,
            @RequestParam String customerEmail) {
        EmailResponseDto response = emailService.sendShippingNotification(orderId, trackingNumber, customerEmail);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/send-password-reset")
    @Operation(summary = "Send password reset email")
    public ResponseEntity<EmailResponseDto> sendPasswordReset(
            @RequestParam String email,
            @RequestParam String resetToken) {
        EmailResponseDto response = emailService.sendPasswordReset(email, resetToken);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/send-bulk")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Send bulk emails to multiple recipients")
    public ResponseEntity<List<EmailResponseDto>> sendBulkEmails(@Valid @RequestBody BulkEmailRequestDto request) {
        List<EmailResponseDto> responses = emailService.sendBulkEmails(request);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/status/{emailId}")
    @Operation(summary = "Get email status by ID")
    public ResponseEntity<EmailStatusDto> getEmailStatus(@PathVariable String emailId) {
        EmailStatusDto status = emailService.getEmailStatus(emailId);
        return ResponseEntity.ok(status);
    }

    @GetMapping("/templates")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all email templates")
    public ResponseEntity<List<String>> getEmailTemplates() {
        List<String> templates = emailService.getAvailableTemplates();
        return ResponseEntity.ok(templates);
    }

    @PostMapping("/test")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Send test email")
    public ResponseEntity<EmailResponseDto> sendTestEmail(@RequestParam String recipientEmail) {
        EmailResponseDto response = emailService.sendTestEmail(recipientEmail);
        return ResponseEntity.ok(response);
    }
}