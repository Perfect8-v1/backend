package com.perfect8.shop.service;

import com.perfect8.shop.entity.Customer;
import com.perfect8.shop.repository.CustomerRepository;
import com.perfect8.shop.dto.AuthDto;
import com.perfect8.shop.dto.JwtResponse;
import com.perfect8.shop.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Authentication Service - Version 1.0 STUB
 *
 * Basic authentication for v1.0.
 * This is a simplified implementation - admin-service handles full auth in production.
 *
 * IMPORTANT: This is a stub for shop-service. Real authentication is handled by admin-service!
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    /**
     * Authenticate customer and generate JWT token
     */
    @Transactional
    public JwtResponse authenticate(String email, String password) {
        log.info("Authenticating user: {}", email);

        try {
            // Authenticate with Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Get customer
            Customer customer = customerRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));

            // Update login info
            customer.setLastLoginAt(LocalDateTime.now());
            customerRepository.save(customer);

            // Generate JWT token - FIXED: Using correct method signature
            String token = jwtTokenProvider.generateCustomerToken(
                    customer.getId(),
                    email,
                    email,  // Using email as username
                    "CUSTOMER"
            );

            // FIXED: Using correct builder field names
            return JwtResponse.builder()
                    .token(token)  // FIXED: This field now exists in JwtResponse
                    .accessToken(token)  // Also set accessToken for compatibility
                    .tokenType("Bearer")
                    .userId(customer.getId())
                    .customerId(customer.getId())
                    .email(customer.getEmail())
                    .username(email)
                    .customerName(customer.getFullName())
                    .customerEmail(customer.getEmail())
                    .role("CUSTOMER")
                    .success(true)
                    .message("Authentication successful")
                    .issuedAt(LocalDateTime.now())
                    .expiresIn(86400L)  // 24 hours in seconds
                    .build();

        } catch (Exception e) {
            log.error("Authentication failed for user {}: {}", email, e.getMessage());
            throw new RuntimeException("Invalid email or password");
        }
    }

    /**
     * Register new customer
     */
    @Transactional
    public Customer register(AuthDto.RegisterRequest request) {
        log.info("Registering new customer: {}", request.getEmail());

        // Check if email already exists
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // Create new customer
        Customer customer = Customer.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .isActive(true)
                .isEmailVerified(false)
                .acceptsMarketing(false)
                .registrationDate(LocalDateTime.now())
                .build();

        Customer savedCustomer = customerRepository.save(customer);
        log.info("Customer registered successfully with ID: {}", savedCustomer.getId());

        return savedCustomer;
    }

    /**
     * Validate JWT token
     */
    public boolean validateToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }

    /**
     * Get customer ID from token
     */
    public Long getCustomerIdFromToken(String token) {
        return jwtTokenProvider.getUserIdFromToken(token);
    }

    /**
     * Get customer email from token
     */
    public String getEmailFromToken(String token) {
        return jwtTokenProvider.getEmailFromToken(token);  // FIXED: This method now exists
    }

    /**
     * Change password
     */
    @Transactional
    public void changePassword(Long customerId, String oldPassword, String newPassword) {
        log.info("Changing password for customer: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // Verify old password
        if (!passwordEncoder.matches(oldPassword, customer.getPassword())) {
            throw new RuntimeException("Invalid current password");
        }

        // Update password
        customer.setPassword(passwordEncoder.encode(newPassword));
        customerRepository.save(customer);

        log.info("Password changed successfully for customer: {}", customerId);
    }

    /**
     * Request password reset
     */
    @Transactional
    public void requestPasswordReset(String email) {
        log.info("Password reset requested for: {}", email);

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email not found"));

        // In v1.0, just log the request
        // In v2.0, generate reset token and send email
        log.info("Password reset would be sent to: {} (not implemented in v1.0)", email);
    }

    /**
     * Verify email
     */
    @Transactional
    public void verifyEmail(Long customerId, String verificationCode) {
        log.info("Verifying email for customer: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // In v1.0, just mark as verified
        // In v2.0, check verification code
        customer.setIsEmailVerified(true);
        customerRepository.save(customer);

        log.info("Email verified for customer: {}", customerId);
    }

    /**
     * Logout (invalidate token)
     */
    public void logout(String token) {
        log.info("Logging out user with token");

        // In v1.0, just clear security context
        // In v2.0, add token to blacklist
        SecurityContextHolder.clearContext();

        log.info("User logged out successfully");
    }

    /**
     * Check if customer is admin
     */
    public boolean isAdmin(Long customerId) {
        // In shop-service, no customers are admins
        // Admin users are in admin-service
        return false;
    }

    /**
     * Get current authenticated customer
     */
    public Customer getCurrentCustomer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No authenticated user");
        }

        String email = authentication.getName(); // This is Spring Security's getName() - returns the principal/username
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
    }

    /* VERSION 2.0 - ADVANCED AUTH FEATURES
     *
     * In v2.0, this service will include:
     * - OAuth2 integration (Google, Facebook)
     * - Two-factor authentication
     * - Session management
     * - Token refresh mechanism
     * - Password reset with tokens
     * - Email verification with codes
     * - Account lockout after failed attempts
     * - IP-based security
     * - Device fingerprinting
     * - Security audit logging
     * - Role-based permissions
     * - API key management
     *
     * Note: Most of these features should be in admin-service,
     * not shop-service, for proper microservice separation.
     */
}