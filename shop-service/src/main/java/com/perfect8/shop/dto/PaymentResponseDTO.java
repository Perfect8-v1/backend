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
 * FIXED: Changed createdAt/processedAt to createdDate/processedDate for consistency with entity naming
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

    /**
     * FIXED: Changed from createdAt to createdDate (Magnum Opus principle - consistency with entities)
     */
    private LocalDateTime createdDate;

    /**
     * FIXED: Changed from processedAt to processedDate (Magnum Opus principle - consistency with entities)
     */
    private LocalDateTime processedDate;

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

    /**
     * FIXED: Updated to use createdDate instead of createdAt
     */
    public LocalDateTime getExpiryTime() {
        if (expiresInMinutes != null && createdDate != null) {
            return createdDate.plusMinutes(expiresInMinutes);
        }
        return null;
    }

    public boolean isExpired() {
        LocalDateTime expiryTime = getExpiryTime();
        return expiryTime != null && LocalDateTime.now().isAfter(expiryTime);
    }

    /**
     * Create a successful response
     * FIXED: Updated to use createdDate and processedDate
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
                .createdDate(LocalDateTime.now())
                .processedDate(LocalDateTime.now())
                .message("Payment processed successfully")
                .build();
    }

    /**
     * Create a failed response
     * FIXED: Updated to use createdDate
     */
    public static PaymentResponseDTO failedResponse(String errorCode,
                                                    String errorMessage) {
        return PaymentResponseDTO.builder()
                .success(false)
                .status("FAILED")
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .createdDate(LocalDateTime.now())
                .message("Payment failed: " + errorMessage)
                .build();
    }

    /**
     * Create a pending response
     * FIXED: Updated to use createdDate
     */
    public static PaymentResponseDTO pendingResponse(String transactionId,
                                                     String approvalUrl) {
        return PaymentResponseDTO.builder()
                .success(true)
                .status("PENDING")
                .transactionId(transactionId)
                .approvalUrl(approvalUrl)
                .requiresVerification(true)
                .createdDate(LocalDateTime.now())
                .message("Payment pending approval")
                .build();
    }
}