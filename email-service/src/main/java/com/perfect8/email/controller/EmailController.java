package com.perfect8.email.controller;

import com.perfect8.common.enums.OrderStatus;
import com.perfect8.email.dto.EmailRequest;
import com.perfect8.email.dto.OrderEmailDTO;
import com.perfect8.email.service.EmailService;
import com.perfect8.email.service.OrderEmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Email Controller - Version 1.2
 * 
 * Security:
 * - Order emails: SERVICE (shop-service) or ADMIN
 * - Marketing/custom emails: ADMIN only
 * - Test endpoint: Public
 * 
 * v1.2: Lagt till @Valid för email-validering
 */
@Slf4j
@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;
    private final OrderEmailService orderEmailService;

    // ========================================================================
    // TRANSACTIONAL EMAILS (shop-service or admin)
    // ========================================================================

    /**
     * Send order confirmation email
     * Called by: shop-service when order is created/paid
     */
    @PostMapping("/order/confirmation")
    @PreAuthorize("hasAnyRole('SERVICE', 'INTERNAL', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<String> sendOrderConfirmation(@Valid @RequestBody OrderEmailDTO orderDto) {
        log.info("Sending order confirmation for order: {}", orderDto.getOrderNumber());

        try {
            if (orderDto.getOrderStatus() == null) {
                orderDto.setOrderStatus(OrderStatus.PAID);
            }

            orderEmailService.sendOrderConfirmation(orderDto);
            return ResponseEntity.ok("Order confirmation email sent");
        } catch (Exception e) {
            log.error("Failed to send order confirmation", e);
            return ResponseEntity.internalServerError()
                    .body("Failed to send order confirmation: " + e.getMessage());
        }
    }

    /**
     * Send order status update email
     * Called by: shop-service when order status changes
     */
    @PostMapping("/order/status")
    @PreAuthorize("hasAnyRole('SERVICE', 'INTERNAL', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<String> sendOrderStatusUpdate(@Valid @RequestBody OrderEmailDTO orderDto) {
        log.info("Sending order status update for order: {}", orderDto.getOrderNumber());

        try {
            if (orderDto.getOrderStatus() == null) {
                return ResponseEntity.badRequest()
                        .body("Order status is required");
            }

            orderEmailService.sendOrderStatusEmail(orderDto);
            return ResponseEntity.ok("Order status email sent");
        } catch (Exception e) {
            log.error("Failed to send order status email", e);
            return ResponseEntity.internalServerError()
                    .body("Failed to send status email: " + e.getMessage());
        }
    }

    /**
     * Send order shipped notification
     * Called by: shop-service when order ships
     */
    @PostMapping("/order/shipped")
    @PreAuthorize("hasAnyRole('SERVICE', 'INTERNAL', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<String> sendOrderShipped(@Valid @RequestBody OrderEmailDTO orderDto) {
        log.info("Sending shipped notification for order: {}", orderDto.getOrderNumber());

        try {
            orderDto.setOrderStatus(OrderStatus.SHIPPED);

            if (orderDto.getTrackingNumber() == null || orderDto.getTrackingNumber().isEmpty()) {
                log.warn("No tracking number provided for shipped order {}", orderDto.getOrderNumber());
            }

            orderEmailService.sendOrderStatusEmail(orderDto);
            return ResponseEntity.ok("Shipping notification sent");
        } catch (Exception e) {
            log.error("Failed to send shipping notification", e);
            return ResponseEntity.internalServerError()
                    .body("Failed to send shipping notification: " + e.getMessage());
        }
    }

    /**
     * Send order cancelled notification
     * Called by: shop-service when order is cancelled
     */
    @PostMapping("/order/cancelled")
    @PreAuthorize("hasAnyRole('SERVICE', 'INTERNAL', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<String> sendOrderCancelled(@Valid @RequestBody OrderEmailDTO orderDto) {
        log.info("Sending cancellation notification for order: {}", orderDto.getOrderNumber());

        try {
            orderDto.setOrderStatus(OrderStatus.CANCELLED);

            orderEmailService.sendOrderStatusEmail(orderDto);
            return ResponseEntity.ok("Cancellation notification sent");
        } catch (Exception e) {
            log.error("Failed to send cancellation notification", e);
            return ResponseEntity.internalServerError()
                    .body("Failed to send cancellation notification: " + e.getMessage());
        }
    }

    // ========================================================================
    // ADMIN-ONLY EMAILS (marketing, custom)
    // ========================================================================

    /**
     * Send custom email (marketing, notifications)
     * Called by: Admin only
     */
    @PostMapping("/send")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<String> sendEmail(@Valid @RequestBody EmailRequest request) {
        log.info("Admin sending email to: {}", request.getRecipientEmail());

        boolean sent = emailService.sendEmail(
                request.getRecipientEmail(),
                request.getSubject(),
                request.getContent(),
                request.isHtml()
        );

        if (sent) {
            return ResponseEntity.ok("Email sent successfully");
        } else {
            return ResponseEntity.internalServerError().body("Failed to send email");
        }
    }

    // ========================================================================
    // PUBLIC ENDPOINTS
    // ========================================================================

    /**
     * Test endpoint - public
     */
    @GetMapping("/test")
    public ResponseEntity<String> testEmail() {
        return ResponseEntity.ok("Email service is running");
    }
}
