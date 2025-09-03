package com.perfect8.shop.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating items in shopping cart
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCartItemRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    private String notes;
    private String customization;

    // Gift options update
    private Boolean isGift;
    private String giftMessage;
    private Boolean giftWrap;

    // Personalization update
    private String personalizationText;

    // Business methods
    public boolean isRemoval() {
        return quantity != null && quantity == 0;
    }

    public boolean hasChanges() {
        return quantity != null || notes != null || customization != null ||
                isGift != null || giftMessage != null || giftWrap != null ||
                personalizationText != null;
    }
}