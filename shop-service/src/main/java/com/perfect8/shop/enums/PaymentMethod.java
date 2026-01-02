package com.perfect8.shop.enums;

/**
 * Payment method enumeration for version 1.0
 * Primary method is PayPal for v1.0
 * Additional methods will be added in v2.0
 */
public enum PaymentMethod {
    PAYPAL("PayPal", true),
    CREDIT_CARD("Credit Card", false),  // Version 2.0
    DEBIT_CARD("Debit Card", false),    // Version 2.0
    BANK_TRANSFER("Bank Transfer", false), // Version 2.0
    CASH_ON_DELIVERY("Cash on Delivery", false); // Version 2.0

    private final String displayName;
    private final boolean availableInV1;

    PaymentMethod(String displayName, boolean availableInV1) {
        this.displayName = displayName;
        this.availableInV1 = availableInV1;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isAvailableInV1() {
        return availableInV1;
    }

    /**
     * Convert string to PaymentMethod
     * Defaults to PAYPAL if unknown
     */
    public static PaymentMethod fromString(String methodString) {
        if (methodString == null) {
            return PAYPAL;
        }

        // Try exact enum match first
        try {
            return PaymentMethod.valueOf(methodString.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            // Try display name match
            for (PaymentMethod method : PaymentMethod.values()) {
                if (method.displayName.equalsIgnoreCase(methodString)) {
                    return method;
                }
            }
            // Default to PayPal for v1.0
            return PAYPAL;
        }
    }

    /**
     * Check if this is the default payment method
     */
    public boolean isDefault() {
        return this == PAYPAL;
    }

    /**
     * Check if payment method requires online processing
     */
    public boolean requiresOnlineProcessing() {
        return this == PAYPAL || this == CREDIT_CARD || this == DEBIT_CARD;
    }
}