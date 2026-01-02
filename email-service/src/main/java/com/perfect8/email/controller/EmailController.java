package com.perfect8.email.controller;

import com.perfect8.common.enums.OrderStatus;
import com.perfect8.email.dto.EmailRequest;
import com.perfect8.email.dto.OrderEmailDTO;
import com.perfect8.email.service.EmailService;
import com.perfect8.email.service.OrderEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Email Controller - Version 1.0
 * Enkel controller för email-hantering
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;
    private final OrderEmailService orderEmailService;

    /**
     * Skicka enkel email
     */
    @PostMapping("/send")
    public ResponseEntity<String> sendEmail(@RequestBody EmailRequest request) {
        log.info("Received email request to: {}", request.getRecipientEmail());

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

    /**
     * Skicka order-bekräftelse
     */
    @PostMapping("/order/confirmation")
    public ResponseEntity<String> sendOrderConfirmation(@RequestBody OrderEmailDTO orderDto) {
        log.info("Sending order confirmation for order: {}", orderDto.getOrderNumber());

        try {
            // Sätt status till PAID för bekräftelse om den saknas
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
     * Skicka order status-uppdatering
     */
    @PostMapping("/order/status")
    public ResponseEntity<String> sendOrderStatusUpdate(@RequestBody OrderEmailDTO orderDto) {
        log.info("Sending order status update for order: {}", orderDto.getOrderNumber());

        try {
            // Validera att vi har en status
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
     * Skicka order shipped notification
     */
    @PostMapping("/order/shipped")
    public ResponseEntity<String> sendOrderShipped(@RequestBody OrderEmailDTO orderDto) {
        log.info("Sending shipped notification for order: {}", orderDto.getOrderNumber());

        try {
            // Sätt status till SHIPPED
            orderDto.setOrderStatus(OrderStatus.SHIPPED);

            // Validera tracking info
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
     * Skicka order cancelled notification
     */
    @PostMapping("/order/cancelled")
    public ResponseEntity<String> sendOrderCancelled(@RequestBody OrderEmailDTO orderDto) {
        log.info("Sending cancellation notification for order: {}", orderDto.getOrderNumber());

        try {
            // Sätt status till CANCELLED
            orderDto.setOrderStatus(OrderStatus.CANCELLED);

            orderEmailService.sendOrderStatusEmail(orderDto);
            return ResponseEntity.ok("Cancellation notification sent");
        } catch (Exception e) {
            log.error("Failed to send cancellation notification", e);
            return ResponseEntity.internalServerError()
                    .body("Failed to send cancellation notification: " + e.getMessage());
        }
    }

    /**
     * Test endpoint - v1.0
     */
    @GetMapping("/test")
    public ResponseEntity<String> testEmail() {
        return ResponseEntity.ok("Email service is running");
    }
}