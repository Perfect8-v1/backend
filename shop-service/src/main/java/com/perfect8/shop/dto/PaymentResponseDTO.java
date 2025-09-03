package com.perfect8.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment Response DTO
 * Version 1.0 - Core payment response structure
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDTO {

    private Long paymentId;
    private String status;
    private String transactionId;
    private String providerTransactionId;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;

    // Provider-specific response data
    private String approvalUrl;
    private String providerResponse;
    private String errorCode;
    private String errorMessage;
    private String message;

    // Additional response information
    private String redirectUrl;
    private String confirmationCode;
    private String receiptUrl;
    private String instructions;
    private Integer expiresInMinutes;

    // Fraud and security
    private String riskScore;
    private String securityCode;
    private Boolean requiresVerification;

    // Success indicator
    private Boolean success;

    // Utility methods
    public boolean isSuccessful() {
        if (success != null) {
            return success;
        }
        return "SUCCESS".equals(status) ||
                "COMPLETED".equals(status) ||
                "APPROVED".equals(status) ||
                "PENDING".equals(status);
    }

    public boolean isFailed() {
        if (success != null && !success) {
            return true;
        }
        return "FAILED".equals(status) ||
                "REJECTED".equals(status) ||
                "ERROR".equals(status) ||
                "CANCELLED".equals(status);
    }

    public boolean isPending() {
        return "PENDING".equals(status) || "PROCESSING".equals(status);
    }

    public boolean requiresUserAction() {
        return approvalUrl != null ||
                (requiresVerification != null && requiresVerification);
    }

    public LocalDateTime getExpiryTime() {
        if (expiresInMinutes != null && createdAt != null) {
            return createdAt.plusMinutes(expiresInMinutes);
        }
        return null;
    }

    public boolean isExpired() {
        LocalDateTime expiryTime = getExpiryTime();
        return expiryTime != null && LocalDateTime.now().isAfter(expiryTime);
    }

    /**
     * Create a successful response
     */
    public static PaymentResponseDTO successResponse(String transactionId,
                                                     BigDecimal amount,
                                                     String paymentMethod) {
        return PaymentResponseDTO.builder()
                .success(true)
                .status("COMPLETED")
                .transactionId(transactionId)
                .amount(amount)
                .paymentMethod(paymentMethod)
                .currency("USD")
                .createdAt(LocalDateTime.now())
                .processedAt(LocalDateTime.now())
                .message("Payment processed successfully")
                .build();
    }

    /**
     * Create a failed response
     */
    public static PaymentResponseDTO failedResponse(String errorCode,
                                                    String errorMessage) {
        return PaymentResponseDTO.builder()
                .success(false)
                .status("FAILED")
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .createdAt(LocalDateTime.now())
                .message("Payment failed: " + errorMessage)
                .build();
    }

    /**
     * Create a pending response
     */
    public static PaymentResponseDTO pendingResponse(String transactionId,
                                                     String approvalUrl) {
        return PaymentResponseDTO.builder()
                .success(true)
                .status("PENDING")
                .transactionId(transactionId)
                .approvalUrl(approvalUrl)
                .requiresVerification(true)
                .createdAt(LocalDateTime.now())
                .message("Payment pending approval")
                .build();
    }
}