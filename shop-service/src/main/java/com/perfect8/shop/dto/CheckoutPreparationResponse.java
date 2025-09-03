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

    // Cart information
    private Long cartId;  // FIXED: Changed from 'id' to 'cartId' to match CartService usage
    private Long customerId;
    private Integer itemCount;
    private Integer totalQuantity;

    // Cart items
    private List<CartItemResponse> items;

    // Pricing breakdown
    private BigDecimal subtotal;
    private BigDecimal shippingCost;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;

    // Shipping information
    private AddressDTO shippingAddress;
    private AddressDTO billingAddress;
    private ShippingOptionDTO selectedShippingOption;
    private List<ShippingOptionDTO> availableShippingOptions;

    // Payment information
    private String paymentMethod;
    private List<String> availablePaymentMethods;

    // Tax information
    private BigDecimal taxRate;
    private String taxJurisdiction;

    // Validation and status
    private boolean isValid;
    private List<String> validationErrors;
    private List<String> warnings;

    // Session and timing
    private String checkoutSessionId;
    private LocalDateTime sessionExpiresAt;
    private LocalDateTime preparedAt;

    // Additional information
    private String currency;
    private String orderNotes;
    private boolean giftOrder;
    private String giftMessage;

    // Discounts and promotions
    private String appliedCouponCode;
    private BigDecimal couponDiscount;
    private List<String> appliedPromotions;

    // Estimated delivery
    private String estimatedDeliveryDate;
    private String estimatedDeliveryTimeframe;

    // Terms and conditions
    private boolean termsAccepted;
    private boolean newsletterOptIn;

    // Helper methods for validation
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

    public boolean hasErrors() {
        return validationErrors != null && !validationErrors.isEmpty();
    }

    public boolean hasWarnings() {
        return warnings != null && !warnings.isEmpty();
    }

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

        if (couponDiscount != null) {
            total = total.subtract(couponDiscount);
        }

        return total;
    }

    public boolean isReadyForCheckout() {
        return isValid &&
                shippingAddress != null &&
                paymentMethod != null &&
                items != null &&
                !items.isEmpty();
    }

    public boolean requiresShipping() {
        // Can be enhanced to check if any items are digital/downloadable
        return true;
    }

    public boolean hasBillingAddress() {
        return billingAddress != null;
    }

    public boolean hasShippingAddress() {
        return shippingAddress != null;
    }

    public boolean hasSelectedShipping() {
        return selectedShippingOption != null;
    }

    public boolean hasPaymentMethod() {
        return paymentMethod != null && !paymentMethod.trim().isEmpty();
    }
}