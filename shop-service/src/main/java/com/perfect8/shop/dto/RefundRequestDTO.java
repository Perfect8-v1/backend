package com.perfect8.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * Refund Request DTO - Version 1.0
 * Simplified for core refund functionality through PayPal
 *
 * Version 1.0 focuses on essential refund information:
 * - Amount to refund
 * - Reason for refund
 * - Type of refund (full/partial)
 *
 * Version 2.0 will add advanced features like:
 * - Bank transfer details
 * - Store credit options
 * - RMA (Return Merchandise Authorization)
 * - Supporting documents
 * - Item-level refunds
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequestDTO {

    @NotNull(message = "Refund amount is required")
    @DecimalMin(value = "0.01", message = "Refund amount must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Refund amount must be a valid monetary value")
    private BigDecimal amount;

    @NotBlank(message = "Refund reason is required")
    @Size(min = 10, max = 500, message = "Refund reason must be between 10 and 500 characters")
    private String reason;

    @Pattern(regexp = "FULL|PARTIAL", message = "Refund type must be FULL or PARTIAL")
    @Builder.Default
    private String refundType = "FULL";

    // Basic refund categories for v1.0 - essential for customer service
    @Pattern(regexp = "DEFECTIVE|DAMAGED|WRONG_ITEM|NOT_AS_DESCRIBED|CUSTOMER_REQUEST|OTHER",
            message = "Invalid refund category")
    private String refundCategory;

    // Additional notes for internal use
    @Size(max = 1000, message = "Additional notes too long")
    private String additionalNotes;

    // Customer contact for refund notification (PayPal uses the original payment email)
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email too long")
    private String customerEmail;

    // Processing options for v1.0
    @Builder.Default
    private Boolean sendEmailConfirmation = true;

    // Utility methods for v1.0
    public boolean isFullRefund() {
        return "FULL".equals(refundType);
    }

    public boolean isPartialRefund() {
        return "PARTIAL".equals(refundType);
    }

    public boolean isDefectiveItemRefund() {
        return "DEFECTIVE".equals(refundCategory) || "DAMAGED".equals(refundCategory);
    }

    public boolean isCustomerRequestRefund() {
        return "CUSTOMER_REQUEST".equals(refundCategory);
    }

    /* ============================================
     * VERSION 2.0 FIELDS - Reserved for future use
     * ============================================
     * These fields will be added in version 2.0:
     *
     * - String preferredRefundMethod (ORIGINAL_METHOD, BANK_TRANSFER, STORE_CREDIT)
     * - String bankName
     * - String accountNumber
     * - String routingNumber
     * - String accountHolderName
     * - List<RefundItemDTO> refundItems (for item-level refunds)
     * - Boolean expediteProcessing
     * - Boolean createStoreCredit
     * - String rmaNumber (Return Merchandise Authorization)
     * - Boolean returnRequested
     * - String returnShippingAddress
     * - List<String> supportingDocuments (URLs to uploaded evidence)
     * - String evidenceDescription
     * - String customerPhone
     *
     * Version 2.0 will also include:
     * - RefundItemDTO inner class for partial item refunds
     * - Complex validation for bank transfer details
     * - Integration with RMA system
     * - Document upload functionality
     */
}