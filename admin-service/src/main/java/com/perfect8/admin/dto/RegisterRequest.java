package com.perfect8.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Registration Request DTO
 * Used for new user registration
 *
 * Updated: 2025-12-19
 * - Changed from password to passwordHash (client-side hashing)
 * - Added passwordSalt field
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    /**
     * BCrypt hash of the password (hashed on client-side)
     * Format: $2a$10$... (60 characters)
     */
    @NotBlank(message = "Password hash is required")
    @Size(min = 60, max = 60, message = "Invalid password hash format")
    private String passwordHash;

    /**
     * BCrypt salt used for hashing (from /api/auth/salt endpoint)
     * Format: $2a$10$... (29 characters)
     */
    @NotBlank(message = "Password salt is required")
    @Size(min = 29, max = 29, message = "Invalid salt format")
    private String passwordSalt;

    private String firstName;

    private String lastName;

    /**
     * Role name (optional)
     * If null or empty, defaults to USER
     * Valid values: USER, CUSTOMER, WRITER, STAFF, ADMIN
     */
    private String role;
}
