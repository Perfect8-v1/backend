package com.perfect8.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutPreparationResponse {

    private Long cartId;
    private Long customerId;
    private Integer itemCount;
    private Integer totalQuantity;
    private List<CartItemResponse> items;

    // Amounts
    private BigDecimal subtotal;
    private BigDecimal shippingCost;
    private BigDecimal taxAmount;
    private BigDecimal taxRate;
    private BigDecimal totalAmount;
    private String currency;

    // Address information
    private AddressDTO shippingAddress;
    private AddressDTO billingAddress;

    // Payment information
    private String paymentMethod;
    private String paymentStatus;

    // Shipping information
    private String shippingMethod;
    private String estimatedDeliveryDate;

    // Validation status
    private Boolean isValid;
    private List<String> validationErrors;
    private List<String> warnings;

    // Timestamps
    private LocalDateTime preparedAt;
    private LocalDateTime expiresAt;

    // Additional information
    private String notes;
    private String promoCode;
    private BigDecimal discountAmount;

    /* ============================================
     * VERSION 2.0 FIELDS - Commented out for v1.0
     * ============================================
     * These fields will be added in version 2.0:
     * - Advanced analytics
     * - Loyalty points
     * - Gift wrapping options
     */

    // Version 2.0 fields (commented out)
    // private BigDecimal loyaltyPointsEarned;
    // private BigDecimal loyaltyPointsUsed;
    // private Boolean giftWrapRequested;
    // private String giftMessage;
    // private BigDecimal giftWrapCost;
    // private List<RecommendedProductDTO> recommendedProducts;
    // private CustomerSegmentDTO customerSegment;
    // private BigDecimal estimatedProfitMargin;

    // Helper methods for adding validation issues
    public void addValidationError(String error) {
        if (this.validationErrors == null) {
            this.validationErrors = new java.util.ArrayList<>();
        }
        this.validationErrors.add(error);
        this.isValid = false;
    }

    public void addWarning(String warning) {
        if (this.warnings == null) {
            this.warnings = new java.util.ArrayList<>();
        }
        this.warnings.add(warning);
    }

    public boolean hasValidationErrors() {
        return validationErrors != null && !validationErrors.isEmpty();
    }

    public boolean hasWarnings() {
        return warnings != null && !warnings.isEmpty();
    }

    // Calculate final total with all adjustments
    public BigDecimal calculateFinalTotal() {
        BigDecimal total = subtotal != null ? subtotal : BigDecimal.ZERO;

        if (shippingCost != null) {
            total = total.add(shippingCost);
        }

        if (taxAmount != null) {
            total = total.add(taxAmount);
        }

        if (discountAmount != null) {
            total = total.subtract(discountAmount);
        }

        return total;
    }

    // Check if checkout is ready to proceed
    public boolean isReadyForCheckout() {
        return Boolean.TRUE.equals(isValid)
                && shippingAddress != null
                && paymentMethod != null
                && items != null
                && !items.isEmpty();
    }
}