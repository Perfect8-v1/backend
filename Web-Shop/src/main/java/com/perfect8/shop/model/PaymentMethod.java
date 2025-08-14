package com.perfect8.shop.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Payment method enumeration for supported payment types.
 *
 * Currently supports PayPal as primary method (as per Perfect8 requirements)
 * but prepared for future expansion.
 */
public enum PaymentMethod {

    PAYPAL("paypal", "PayPal", "PayPal secure payment", true, 2.9),
    CREDIT_CARD("credit_card", "Credit Card", "Visa, MasterCard, American Express", false, 2.4),
    DEBIT_CARD("debit_card", "Debit Card", "Direct debit from bank account", false, 1.5),
    BANK_TRANSFER("bank_transfer", "Bank Transfer", "Direct bank transfer", false, 0.5),
    APPLE_PAY("apple_pay", "Apple Pay", "Pay with Apple Pay", false, 2.9),
    GOOGLE_PAY("google_pay", "Google Pay", "Pay with Google Pay", false, 2.9),
    CRYPTOCURRENCY("crypto", "Cryptocurrency", "Bitcoin, Ethereum payments", false, 1.0);

    private final String code;
    private final String displayName;
    private final String description;
    private final boolean enabled;
    private final double processingFeePercent;

    PaymentMethod(String code, String displayName, String description, boolean enabled, double processingFeePercent) {
        this.code = code;
        this.displayName = displayName;
        this.description = description;
        this.enabled = enabled;
        this.processingFeePercent = processingFeePercent;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public double getProcessingFeePercent() {
        return processingFeePercent;
    }

    public boolean requiresRedirect() {
        return this == PAYPAL || this == APPLE_PAY || this == GOOGLE_PAY;
    }

    public boolean supportsRefund() {
        return this == PAYPAL || this == CREDIT_CARD || this == DEBIT_CARD;
    }

    public boolean requiresGateway() {
        return this == PAYPAL || this == CREDIT_CARD || this == DEBIT_CARD
                || this == APPLE_PAY || this == GOOGLE_PAY;
    }

    public String getIcon() {
        return switch (this) {
            case PAYPAL -> "fab fa-paypal";
            case CREDIT_CARD -> "far fa-credit-card";
            case DEBIT_CARD -> "fas fa-credit-card";
            case BANK_TRANSFER -> "fas fa-university";
            case APPLE_PAY -> "fab fa-apple-pay";
            case GOOGLE_PAY -> "fab fa-google-pay";
            case CRYPTOCURRENCY -> "fab fa-bitcoin";
        };
    }

    public static PaymentMethod fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return PAYPAL; // Default to PayPal
        }

        for (PaymentMethod method : values()) {
            if (method.code.equalsIgnoreCase(code.trim())) {
                return method;
            }
        }

        throw new IllegalArgumentException("Invalid payment method code: " + code);
    }

    public static PaymentMethod[] getEnabledMethods() {
        return java.util.Arrays.stream(values())
                .filter(PaymentMethod::isEnabled)
                .toArray(PaymentMethod[]::new);
    }

    @Override
    public String toString() {
        return displayName;
    }
}