package com.perfect8.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for saved cart
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavedCartResponse {

    private Long cartId;
    private String name;
    private String description;
    private Integer itemCount;
    private BigDecimal totalAmount;
    private LocalDateTime savedAt;
    private LocalDateTime updatedAt;
    private Boolean isDefault;

    // Business methods
    public boolean isEmpty() {
        return itemCount == null || itemCount == 0;
    }
}