package com.perfect8.shop.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddToCartRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Builder.Default
    private Integer quantity = 1;

    private String customization;
    private String notes;

    // Optional: Product variant information
    private String size;
    private String color;
    private String variant;

    // Optional: Gift options
    private boolean isGift;
    private String giftMessage;
    private String giftWrapType;

    // Optional: Subscription information
    private boolean isSubscription;
    private String subscriptionFrequency;

    // Business validation methods
    public boolean hasCustomization() {
        return customization != null && !customization.trim().isEmpty();
    }

    public boolean isGiftOrder() {
        return isGift;
    }

    public boolean hasSpecialRequests() {
        return hasCustomization() || isGiftOrder() ||
                (notes != null && !notes.trim().isEmpty());
    }

    public boolean isValidQuantity() {
        return quantity != null && quantity >= 1 && quantity <= 99;
    }
}