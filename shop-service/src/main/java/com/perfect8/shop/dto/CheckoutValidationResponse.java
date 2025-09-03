package com.perfect8.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO for checkout validation
 * Version 1.0 - Core checkout validation
 *
 * Critical: Ensures orders can be fulfilled before payment!
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutValidationResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Overall validation result
     */
    @Builder.Default
    private boolean isValid = true;

    /**
     * List of validation issues/errors
     */
    @Builder.Default
    private List<String> issues = new ArrayList<>();

    /**
     * Cart ID being validated
     */
    private Long cartId;

    /**
     * Customer ID
     */
    private Long customerId;

    /**
     * Total items in cart
     */
    private Integer itemCount;

    /**
     * Subtotal amount
     */
    private BigDecimal subtotal;

    /**
     * Tax amount
     */
    private BigDecimal tax;

    /**
     * Shipping amount
     */
    private BigDecimal shipping;

    /**
     * Final total amount
     */
    private BigDecimal total;

    /**
     * Currency
     */
    @Builder.Default
    private String currency = "USD";

    /**
     * Inventory validation details
     */
    @Builder.Default
    private InventoryValidation inventoryValidation = new InventoryValidation();

    /**
     * Shipping validation
     */
    private ShippingValidation shippingValidation;

    /**
     * Payment validation
     */
    private PaymentValidation paymentValidation;

    /**
     * Timestamp of validation
     */
    @Builder.Default
    private LocalDateTime validatedAt = LocalDateTime.now();

    /**
     * Helper method to add an issue
     */
    public void addIssue(String issue) {
        if (this.issues == null) {
            this.issues = new ArrayList<>();
        }
        this.issues.add(issue);
        this.isValid = false;
    }

    /**
     * Inventory validation details
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InventoryValidation {
        @Builder.Default
        private boolean isValid = true;

        @Builder.Default
        private List<OutOfStockItem> outOfStockItems = new ArrayList<>();

        @Builder.Default
        private List<LowStockItem> lowStockItems = new ArrayList<>();

        @Builder.Default
        private List<String> warnings = new ArrayList<>();
    }

    /**
     * Out of stock item details
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OutOfStockItem {
        private Long productId;
        private String productName;
        private String sku;
        private Integer requestedQuantity;
        private Integer availableQuantity;
    }

    /**
     * Low stock item details
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LowStockItem {
        private Long productId;
        private String productName;
        private String sku;
        private Integer requestedQuantity;
        private Integer availableQuantity;
        private String warning;
    }

    /**
     * Shipping validation details
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShippingValidation {
        @Builder.Default
        private boolean isValid = true;

        private boolean hasShippingAddress;
        private boolean shippingMethodAvailable;
        private String shippingMethod;
        private BigDecimal shippingCost;

        @Builder.Default
        private List<String> issues = new ArrayList<>();
    }

    /**
     * Payment validation details
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentValidation {
        @Builder.Default
        private boolean isValid = true;

        private boolean hasPaymentMethod;
        private String paymentMethod;
        private boolean paymentMethodAvailable;

        @Builder.Default
        private List<String> issues = new ArrayList<>();
    }

    /**
     * Check if checkout can proceed
     */
    public boolean canProceedToPayment() {
        return isValid &&
                (inventoryValidation == null || inventoryValidation.isValid) &&
                (shippingValidation == null || shippingValidation.isValid) &&
                (paymentValidation == null || paymentValidation.isValid);
    }

    /**
     * Get all issues combined
     */
    public List<String> getAllIssues() {
        List<String> allIssues = new ArrayList<>();

        if (issues != null) {
            allIssues.addAll(issues);
        }

        if (inventoryValidation != null) {
            for (OutOfStockItem item : inventoryValidation.getOutOfStockItems()) {
                allIssues.add(String.format("%s is out of stock (requested: %d, available: %d)",
                        item.getProductName(), item.getRequestedQuantity(), item.getAvailableQuantity()));
            }
            if (inventoryValidation.getWarnings() != null) {
                allIssues.addAll(inventoryValidation.getWarnings());
            }
        }

        if (shippingValidation != null && shippingValidation.getIssues() != null) {
            allIssues.addAll(shippingValidation.getIssues());
        }

        if (paymentValidation != null && paymentValidation.getIssues() != null) {
            allIssues.addAll(paymentValidation.getIssues());
        }

        return allIssues;
    }

    /**
     * Check if only warnings (no blocking issues)
     */
    public boolean hasOnlyWarnings() {
        return isValid &&
                inventoryValidation != null &&
                inventoryValidation.getOutOfStockItems().isEmpty() &&
                !inventoryValidation.getLowStockItems().isEmpty();
    }

    /**
     * Get severity level
     */
    public String getSeverity() {
        if (!isValid) {
            return "ERROR";
        }
        if (hasOnlyWarnings()) {
            return "WARNING";
        }
        return "OK";
    }
}