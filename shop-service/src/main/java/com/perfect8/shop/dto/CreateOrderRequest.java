package com.perfect8.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotEmpty(message = "Order items are required")
    @Valid
    private List<OrderItemRequest> orderItems;

    @NotNull(message = "Subtotal is required")
    @DecimalMin(value = "0.0", message = "Subtotal must be positive")
    private BigDecimal subtotal;

    private BigDecimal taxAmount;
    private BigDecimal shippingCost;
    private BigDecimal discountAmount;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0", message = "Total amount must be positive")
    private BigDecimal totalAmount;

    @NotNull(message = "Currency is required")
    private String currency;

    // Addresses - individual fields (standard format from Flutter)
    private String shippingAddressLine1;
    private String shippingAddressLine2;
    private String shippingCity;
    private String shippingState;
    private String shippingPostalCode;
    private String shippingCountry;

    // Addresses - legacy comma-separated strings (fallback)
    private String shippingAddress;
    private String billingAddress;

    // Shipping details
    private String shippingMethod;
    private String trackingNumber;

    // Payment details
    private String paymentMethod;
    private String paymentReference;

    // Additional information
    private String notes;
    private String source; // WEB, MOBILE, API

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {
        @NotNull(message = "Product ID is required")
        private Long productId;

        @NotNull(message = "Quantity is required")
        @DecimalMin(value = "1", message = "Quantity must be at least 1")
        private Integer quantity;

        @NotNull(message = "Unit price is required")
        @DecimalMin(value = "0.0", message = "Unit price must be positive")
        private BigDecimal unitPrice;

        private String productName;
        private String productSku;
    }
}
