package com.perfect8.shop.model;

import com.fasterxml.jackson.annotation.JsonValue;
import java.math.BigDecimal;

/**
 * Shipping method enumeration for supported shipping options.
 *
 * Focused on USA market as per Perfect8 requirements, but expandable.
 */
public enum ShippingMethod {

    STANDARD("standard", "Standard Shipping", "5-7 business days",
            BigDecimal.valueOf(9.99), true, 1, 5),

    EXPRESS("express", "Express Shipping", "2-3 business days",
            BigDecimal.valueOf(19.99), true, 2, 2),

    OVERNIGHT("overnight", "Overnight Shipping", "Next business day",
            BigDecimal.valueOf(39.99), true, 3, 1),

    FREE_SHIPPING("free", "Free Shipping", "7-10 business days",
            BigDecimal.ZERO, true, 0, 7),

    SAME_DAY("same_day", "Same Day Delivery", "Same day",
            BigDecimal.valueOf(24.99), false, 4, 0),

    IN_STORE_PICKUP("pickup", "In-Store Pickup", "Ready in 2 hours",
            BigDecimal.ZERO, false, 5, 0),

    INTERNATIONAL_STANDARD("intl_standard", "International Standard", "10-21 business days",
            BigDecimal.valueOf(29.99), false, 6, 14),

    INTERNATIONAL_EXPRESS("intl_express", "International Express", "3-7 business days",
            BigDecimal.valueOf(59.99), false, 7, 5);

    private final String code;
    private final String displayName;
    private final String description;
    private final BigDecimal cost;
    private final boolean enabled;
    private final int priority; // Lower number = higher priority
    private final int deliveryDays;

    ShippingMethod(String code, String displayName, String description,
                   BigDecimal cost, boolean enabled, int priority, int deliveryDays) {
        this.code = code;
        this.displayName = displayName;
        this.description = description;
        this.cost = cost;
        this.enabled = enabled;
        this.priority = priority;
        this.deliveryDays = deliveryDays;
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

    public BigDecimal getCost() {
        return cost;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getPriority() {
        return priority;
    }

    public int getDeliveryDays() {
        return deliveryDays;
    }

    public boolean isFree() {
        return cost.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean isInternational() {
        return this == INTERNATIONAL_STANDARD || this == INTERNATIONAL_EXPRESS;
    }

    public boolean requiresAddress() {
        return this != IN_STORE_PICKUP;
    }

    public boolean isExpedited() {
        return this == EXPRESS || this == OVERNIGHT || this == SAME_DAY;
    }

    public boolean isAvailableForCountry(String countryCode) {
        if ("US".equalsIgnoreCase(countryCode)) {
            return !isInternational();
        } else {
            return isInternational();
        }
    }

    public BigDecimal calculateCostForWeight(BigDecimal weight) {
        if (isFree()) {
            return BigDecimal.ZERO;
        }

        // Add weight-based surcharge for heavy items (over 10kg)
        if (weight != null && weight.compareTo(BigDecimal.valueOf(10)) > 0) {
            BigDecimal extraWeight = weight.subtract(BigDecimal.valueOf(10));
            BigDecimal surcharge = extraWeight.multiply(BigDecimal.valueOf(2.50)); // $2.50 per kg
            return cost.add(surcharge);
        }

        return cost;
    }

    public String getIcon() {
        return switch (this) {
            case STANDARD -> "fas fa-truck";
            case EXPRESS -> "fas fa-shipping-fast";
            case OVERNIGHT -> "fas fa-plane";
            case FREE_SHIPPING -> "fas fa-gift";
            case SAME_DAY -> "fas fa-rocket";
            case IN_STORE_PICKUP -> "fas fa-store";
            case INTERNATIONAL_STANDARD -> "fas fa-globe";
            case INTERNATIONAL_EXPRESS -> "fas fa-globe-americas";
        };
    }

    public String getCarrier() {
        return switch (this) {
            case STANDARD, FREE_SHIPPING -> "USPS";
            case EXPRESS, OVERNIGHT -> "FedEx";
            case SAME_DAY -> "Local Courier";
            case IN_STORE_PICKUP -> "Store";
            case INTERNATIONAL_STANDARD, INTERNATIONAL_EXPRESS -> "DHL";
        };
    }

    public static ShippingMethod fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return STANDARD; // Default shipping method
        }

        for (ShippingMethod method : values()) {
            if (method.code.equalsIgnoreCase(code.trim())) {
                return method;
            }
        }

        throw new IllegalArgumentException("Invalid shipping method code: " + code);
    }

    public static ShippingMethod[] getEnabledMethods() {
        return java.util.Arrays.stream(values())
                .filter(ShippingMethod::isEnabled)
                .sorted((a, b) -> Integer.compare(a.priority, b.priority))
                .toArray(ShippingMethod[]::new);
    }

    public static ShippingMethod[] getMethodsForCountry(String countryCode) {
        return java.util.Arrays.stream(getEnabledMethods())
                .filter(method -> method.isAvailableForCountry(countryCode))
                .toArray(ShippingMethod[]::new);
    }

    public static ShippingMethod getCheapestMethod() {
        return FREE_SHIPPING.enabled ? FREE_SHIPPING : STANDARD;
    }

    public static ShippingMethod getFastestMethod() {
        return java.util.Arrays.stream(getEnabledMethods())
                .min((a, b) -> Integer.compare(a.deliveryDays, b.deliveryDays))
                .orElse(STANDARD);
    }

    @Override
    public String toString() {
        return displayName;
    }
}