package com.perfect8.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Login Request DTO
 * Updated: 2026-01-11
 * - SOP: Changed from passwordHash to password (plaintext over HTTPS)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    /**
     * Plaintext password - will be verified against BCrypt hash in database
     * Security: Always use HTTPS in production
     */
    @NotBlank(message = "Password is required")
    private String password;
}
