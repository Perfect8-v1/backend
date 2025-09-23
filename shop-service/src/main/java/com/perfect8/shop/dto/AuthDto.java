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
 */
public class AuthDto {

    /**
     * Login Request DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {

        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        private String email;

        @NotBlank(message = "Password is required")
        private String password;

        private boolean rememberMe;
    }

    /**
     * Register Request DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterRequest {

        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        private String password;

        @NotBlank(message = "First name is required")
        @Size(max = 50, message = "First name must not exceed 50 characters")
        private String firstName;

        @NotBlank(message = "Last name is required")
        @Size(max = 50, message = "Last name must not exceed 50 characters")
        private String lastName;

        @Size(max = 20, message = "Phone number must not exceed 20 characters")
        private String phoneNumber;

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
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangePasswordRequest {

        @NotBlank(message = "Current password is required")
        private String oldPassword;

        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        private String newPassword;

        @NotBlank(message = "Confirm password is required")
        private String confirmPassword;
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
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResetPasswordConfirm {

        @NotBlank(message = "Token is required")
        private String token;

        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        private String newPassword;

        @NotBlank(message = "Confirm password is required")
        private String confirmPassword;
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