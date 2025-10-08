package com.perfect8.shop.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Authentication DTOs - Version 1.0
 * Contains all authentication-related data transfer objects
 *
 * PEDAGOGICAL NOTE for GitHub portfolio:
 * Uses 'passwordHash' instead of 'password' to make it clear that passwords
 * are ALREADY HASHED by the frontend before transmission.
 * This demonstrates security awareness and prevents misconceptions about
 * sending plaintext passwords over the network.
 */
public class AuthDto {

    /**
     * Login Request DTO
     * SECURITY: passwordHash is hashed on client-side before transmission
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {

        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        private String email;

        @NotBlank(message = "Password hash is required")
        private String passwordHash;  // CHANGED: password → passwordHash (pedagogical clarity)

        private boolean rememberMe;
    }

    /**
     * Register Request DTO
     * SECURITY: passwordHash is hashed on client-side before transmission
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterRequest {

        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        private String email;

        @NotBlank(message = "Password hash is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        private String passwordHash;  // CHANGED: password → passwordHash (pedagogical clarity)

        @NotBlank(message = "First name is required")
        @Size(max = 50, message = "First name must not exceed 50 characters")
        private String firstName;

        @NotBlank(message = "Last name is required")
        @Size(max = 50, message = "Last name must not exceed 50 characters")
        private String lastName;

        @Size(max = 20, message = "Phone must not exceed 20 characters")
        private String phone;  // FIXED: phoneNumber → phone (matches Customer.phone)

        private boolean newsletterSubscribed;
        private boolean acceptTerms;
    }

    /**
     * JWT Response DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JwtResponse {

        private String token;
        private String tokenType;
        private Long expiresIn;
        private String refreshToken;
        private Long customerId;
        private String email;
        private String firstName;
        private String lastName;
        private String role;
    }

    /**
     * Change Password Request DTO
     * SECURITY: All password fields are hashed on client-side
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangePasswordRequest {

        @NotBlank(message = "Current password hash is required")
        private String oldPasswordHash;  // CHANGED: oldPassword → oldPasswordHash

        @NotBlank(message = "New password hash is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        private String newPasswordHash;  // CHANGED: newPassword → newPasswordHash

        @NotBlank(message = "Confirm password hash is required")
        private String confirmPasswordHash;  // CHANGED: confirmPassword → confirmPasswordHash
    }

    /**
     * Reset Password Request DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResetPasswordRequest {

        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        private String email;
    }

    /**
     * Reset Password Confirm DTO
     * SECURITY: Password hashes sent from client
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResetPasswordConfirm {

        @NotBlank(message = "Token is required")
        private String token;

        @NotBlank(message = "New password hash is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        private String newPasswordHash;  // CHANGED: newPassword → newPasswordHash

        @NotBlank(message = "Confirm password hash is required")
        private String confirmPasswordHash;  // CHANGED: confirmPassword → confirmPasswordHash
    }

    /**
     * Email Verification Request DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmailVerificationRequest {

        @NotBlank(message = "Token is required")
        private String token;
    }

    /**
     * Resend Verification Email Request DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResendVerificationRequest {

        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        private String email;
    }

    /**
     * Token Validation Response DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenValidationResponse {

        private boolean valid;
        private String message;
        private Long customerId;
        private String email;
        private String role;
    }

    /**
     * Auth Error Response DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthErrorResponse {

        private String error;
        private String message;
        private String path;
        private Long timestamp;
    }

    /**
     * Logout Request DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LogoutRequest {

        private String token;
        private String reason;
    }

    /**
     * Session Info Response DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionInfo {

        private boolean authenticated;
        private Long customerId;
        private String email;
        private String role;
        private Long sessionExpiry;
        private boolean emailVerified;
    }
}