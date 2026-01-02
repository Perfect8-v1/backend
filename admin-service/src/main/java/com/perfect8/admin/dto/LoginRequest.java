package com.perfect8.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Login Request DTO
 * Updated: 2025-12-19
 * - Changed from password to passwordHash (client-side hashing)
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
     * BCrypt hash of the password (hashed on client-side using salt from /api/auth/salt)
     * Format: $2a$10$... (60 characters)
     */
    @NotBlank(message = "Password hash is required")
    private String passwordHash;
}
