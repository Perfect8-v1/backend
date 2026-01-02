package com.perfect8.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * JWT Response DTO - Version 1.0
 * Response object for authentication containing JWT token and user details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {

    // Primary token field
    private String token;

    // Alternative token field name (kept for compatibility)
    private String accessToken;

    // Token metadata
    private String tokenType;
    private Long expiresIn;  // Expiration time in seconds
    private LocalDateTime expirationDate;

    // Refresh token (for future implementation)
    private String refreshToken;
    private Long refreshExpiresIn;

    // User information
    private Long userId;
    private String username;
    private String email;
    private String role;
    private List<String> authorities;

    // Customer name fields - ADDED for AuthService compatibility
    private String firstName;
    private String lastName;

    // Customer information (if applicable)
    private Long customerId;
    private String customerName;
    private String customerEmail;

    // Additional metadata
    private LocalDateTime issuedDate;
    private String issuer;

    // Session information
    private String sessionId;

    // Status and messages
    private boolean success;
    private String message;

    // Helper constructors for common use cases

    /**
     * Create a simple success response with just a token
     */
    public static JwtResponse success(String token) {
        return JwtResponse.builder()
                .token(token)
                .accessToken(token)  // Set both for compatibility
                .tokenType("Bearer")
                .success(true)
                .message("Authentication successful")
                .issuedDate(LocalDateTime.now())
                .build();
    }

    /**
     * Create a success response with token and user details
     */
    public static JwtResponse successWithUser(String token, Long userId, String username, String role) {
        return JwtResponse.builder()
                .token(token)
                .accessToken(token)  // Set both for compatibility
                .tokenType("Bearer")
                .userId(userId)
                .username(username)
                .role(role)
                .success(true)
                .message("Authentication successful")
                .issuedDate(LocalDateTime.now())
                .build();
    }

    /**
     * Create a success response with customer details
     */
    public static JwtResponse successWithCustomer(String token, Long customerId, String firstName,
                                                  String lastName, String email, String role) {
        return JwtResponse.builder()
                .token(token)
                .accessToken(token)  // Set both for compatibility
                .tokenType("Bearer")
                .customerId(customerId)
                .userId(customerId)  // Set userId same as customerId
                .firstName(firstName)
                .lastName(lastName)
                .customerName(firstName + " " + lastName)
                .email(email)
                .customerEmail(email)
                .username(email)  // Use email as username
                .role(role)
                .success(true)
                .message("Authentication successful")
                .issuedDate(LocalDateTime.now())
                .build();
    }

    /**
     * Create a success response with full details
     */
    public static JwtResponse successFull(String token, Long userId, String username,
                                          String email, String role, Long expiresIn) {
        LocalDateTime now = LocalDateTime.now();
        return JwtResponse.builder()
                .token(token)
                .accessToken(token)  // Set both for compatibility
                .tokenType("Bearer")
                .userId(userId)
                .username(username)
                .email(email)
                .role(role)
                .expiresIn(expiresIn)
                .expirationDate(now.plusSeconds(expiresIn))
                .success(true)
                .message("Authentication successful")
                .issuedDate(now)
                .build();
    }

    /**
     * Create a success response with full customer details
     */
    public static JwtResponse successFullCustomer(String token, Long customerId, String firstName,
                                                  String lastName, String email, String role,
                                                  Long expiresIn) {
        LocalDateTime now = LocalDateTime.now();
        return JwtResponse.builder()
                .token(token)
                .accessToken(token)  // Set both for compatibility
                .tokenType("Bearer")
                .customerId(customerId)
                .userId(customerId)
                .firstName(firstName)
                .lastName(lastName)
                .customerName(firstName + " " + lastName)
                .username(email)
                .email(email)
                .customerEmail(email)
                .role(role)
                .expiresIn(expiresIn)
                .expirationDate(now.plusSeconds(expiresIn))
                .success(true)
                .message("Authentication successful")
                .issuedDate(now)
                .build();
    }

    /**
     * Create a failure response
     */
    public static JwtResponse failure(String message) {
        return JwtResponse.builder()
                .success(false)
                .message(message)
                .issuedDate(LocalDateTime.now())
                .build();
    }

    // Helper methods

    /**
     * Get full name from firstName and lastName
     */
    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        }
        return customerName;
    }

    /**
     * Check if the token is expired
     */
    public boolean isExpired() {
        if (expirationDate == null) {
            return false; // If no expiration date is set, assume not expired
        }
        return LocalDateTime.now().isAfter(expirationDate);
    }

    /**
     * Get the token with Bearer prefix for Authorization header
     */
    public String getAuthorizationHeader() {
        if (token != null && !token.isEmpty()) {
            return "Bearer " + token;
        }
        if (accessToken != null && !accessToken.isEmpty()) {
            return "Bearer " + accessToken;
        }
        return null;
    }

    /**
     * Check if this is a successful authentication response
     */
    public boolean isAuthenticated() {
        return success && (token != null || accessToken != null);
    }

    /**
     * Get the actual token (tries both fields)
     */
    public String getActualToken() {
        return token != null ? token : accessToken;
    }

    /**
     * Ensure both token fields are synchronized
     */
    public void synchronizeTokenFields() {
        if (token != null && accessToken == null) {
            accessToken = token;
        } else if (accessToken != null && token == null) {
            token = accessToken;
        }
    }

    /**
     * Synchronize name fields
     */
    public void synchronizeNameFields() {
        if (firstName != null && lastName != null && customerName == null) {
            customerName = firstName + " " + lastName;
        } else if (customerName != null && firstName == null && lastName == null) {
            // Try to split customerName
            String[] parts = customerName.split(" ", 2);
            if (parts.length >= 1) {
                firstName = parts[0];
            }
            if (parts.length >= 2) {
                lastName = parts[1];
            }
        }
    }

    /**
     * Check if user has a specific role
     */
    public boolean hasRole(String requiredRole) {
        if (role != null && role.equalsIgnoreCase(requiredRole)) {
            return true;
        }
        if (authorities != null) {
            return authorities.stream()
                    .anyMatch(auth -> auth.equalsIgnoreCase(requiredRole) ||
                            auth.equalsIgnoreCase("ROLE_" + requiredRole));
        }
        return false;
    }

    /**
     * Check if this is an admin user
     */
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * Check if this is a customer user
     */
    public boolean isCustomer() {
        return hasRole("CUSTOMER") || hasRole("USER");
    }

    /**
     * Set customer information
     */
    public void setCustomerInfo(Long customerId, String firstName, String lastName, String email) {
        this.customerId = customerId;
        this.userId = customerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.customerName = firstName + " " + lastName;
        this.email = email;
        this.customerEmail = email;
        this.username = email;
    }

    // Version 2.0 fields - commented out for future
    /*
    // Multi-factor authentication
    private boolean mfaRequired;
    private String mfaToken;

    // Device information
    private String deviceId;
    private String deviceType;
    private String ipAddress;

    // Additional claims
    private Map<String, Object> additionalClaims;

    // Permissions (more granular than roles)
    private List<String> permissions;

    // Session management
    private Integer maxSessions;
    private List<String> activeSessions;
    */
}