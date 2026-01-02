package com.perfect8.shop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for saving a cart for later
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveCartRequest {

    @NotBlank(message = "Cart name is required")
    @Size(max = 100, message = "Cart name cannot exceed 100 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    private Boolean makeDefault;

    // Business methods
    public boolean shouldMakeDefault() {
        return Boolean.TRUE.equals(makeDefault);
    }
}