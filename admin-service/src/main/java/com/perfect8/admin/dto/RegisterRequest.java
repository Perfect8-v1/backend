package com.perfect8.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Registration Request DTO - Version 1.3
 * 
 * ÄNDRINGAR v1.3 (Industristandard):
 * - Borttaget: passwordHash, passwordSalt (v1.2 client-side hashing)
 * - Tillagt: password (plaintext, skickas över HTTPS)
 * - Backend hashar med BCrypt automatiskt
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
     * Plaintext password (v1.3 industristandard)
     * Skickas över HTTPS, hashas i backend med BCrypt
     */
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;

    @Size(max = 100, message = "First name cannot exceed 100 characters")
    private String firstName;

    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    private String lastName;

    /**
     * Phone number (optional, för customers)
     */
    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    private String phone;

    /**
     * Role name (optional)
     * If null or empty, defaults to USER
     * Valid values: USER, CUSTOMER, WRITER, STAFF, ADMIN
     */
    private String role;
}
