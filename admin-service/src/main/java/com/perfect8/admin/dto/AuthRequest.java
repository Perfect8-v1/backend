package com.perfect8.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Authentication Request DTO
 * Used for user login
 * 
 * Created: 2025-11-20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
}
