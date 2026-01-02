package com.perfect8.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutPreparationRequest {

    private String sessionId;
    private Long customerId;

    // Customer information
    @Email(message = "Invalid email format")
    private String email;

    @Pattern(regexp = "^[+]?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phone;

    // Shipping address
    @NotBlank(message = "Shipping street address is required")
    private String shippingStreetAddress;

    private String shippingStreetAddress2;

    @NotBlank(message = "Shipping city is required")
    private String shippingCity;

    @NotBlank(message = "Shipping state is required")
    private String shippingState;

    @NotBlank(message = "Shipping postal code is required")
    private String shippingPostalCode;

    @NotBlank(message = "Shipping country is required")
    private String shippingCountry;

    // Billing address
    private Boolean useSameAddressForBilling;

    private String billingStreetAddress;
    private String billingStreetAddress2;
    private String billingCity;
    private String billingState;
    private String billingPostalCode;
    private String billingCountry;

    // Shipping preferences
    @NotNull(message = "Shipping method is required")
    private String shippingMethod;

    private String shippingCarrier;
    private Boolean signatureRequired;
    private Boolean insuranceRequired;
    private String deliveryInstructions;

    // Payment information
    @NotNull(message = "Payment method is required")
    private String paymentMethod;

    // Coupon/discount codes
    private String couponCode;
    private String promoCode;

    // Special requests
    private String specialInstructions;
    private Boolean isGift;
    private String giftMessage;

    // Preferences
    private Boolean subscribeToNewsletter;
    private Boolean saveAddressForFuture;
    private Boolean createAccount;

    // Validation flags
    private Boolean validateInventory;
    private Boolean validateAddresses;
    private Boolean calculateTaxes;
    private Boolean calculateShipping;

    // Metadata
    private Map<String, Object> metadata;
    private String source; // WEB, MOBILE, API
}