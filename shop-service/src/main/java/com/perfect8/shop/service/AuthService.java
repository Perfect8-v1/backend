package com.perfect8.shop.service;

import com.perfect8.shop.dto.AuthDto;
import com.perfect8.shop.dto.JwtResponse;
import com.perfect8.shop.entity.Customer;
import com.perfect8.shop.exception.EmailAlreadyExistsException;
import com.perfect8.shop.exception.UnauthorizedAccessException;
import com.perfect8.shop.repository.CustomerRepository;
import com.perfect8.shop.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Authentication Service - Version 1.0
 * Handles customer authentication and registration
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;

    /**
     * Authenticate customer and generate JWT token
     */
    public JwtResponse login(AuthDto.LoginRequest loginRequest) {
        log.info("Login attempt for email: {}", loginRequest.getEmail());

        Customer customer = customerRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new UnauthorizedAccessException("Invalid email or password"));

        // Check if customer is active - FIX: Use correct method name
        if (!customer.isActiveCustomer()) {
            throw new UnauthorizedAccessException("Account is inactive or deleted");
        }

        // Verify password
        if (!passwordEncoder.matches(loginRequest.getPassword(), customer.getPassword())) {
            log.warn("Failed login attempt for email: {}", loginRequest.getEmail());
            throw new UnauthorizedAccessException("Invalid email or password");
        }

        // Update last login
        customer.updateLastLogin();
        customerRepository.save(customer);

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(
                customer.getCustomerId(),
                customer.getEmail(),
                "CUSTOMER"
        );

        log.info("Successful login for customer ID: {}", customer.getCustomerId());

        return JwtResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpirationTime())
                .customerId(customer.getCustomerId())
                .email(customer.getEmail())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .emailVerified(customer.hasVerifiedEmail())
                .build();
    }

    /**
     * Register new customer
     */
    public JwtResponse register(AuthDto.RegisterRequest registerRequest) {
        log.info("Registration attempt for email: {}", registerRequest.getEmail());

        // Check if email already exists
        if (customerRepository.existsByEmail(registerRequest.getEmail())) {
            throw new EmailAlreadyExistsException("Email is already registered");
        }

        // Create new customer
        Customer customer = Customer.builder()
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .phoneNumber(registerRequest.getPhone())
                .registrationDate(LocalDateTime.now())
                .isActive(true)
                .isEmailVerified(false)
                .preferredCurrency("USD")
                .preferredLanguage("en")
                .build();

        // Generate email verification token
        String verificationToken = generateVerificationToken();
        customer.setEmailVerificationToken(verificationToken);

        Customer savedCustomer = customerRepository.save(customer);

        // Send welcome email (async)
        try {
            emailService.sendWelcomeEmail(savedCustomer);
            // Also send verification email
            emailService.sendEmailVerification(savedCustomer);
        } catch (Exception e) {
            log.error("Failed to send welcome/verification email to {}: {}",
                    savedCustomer.getEmail(), e.getMessage());
            // Don't fail registration if email fails
        }

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(
                savedCustomer.getCustomerId(),
                savedCustomer.getEmail(),
                "CUSTOMER"
        );

        log.info("Successful registration for customer ID: {}", savedCustomer.getCustomerId());

        return JwtResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpirationTime())
                .customerId(savedCustomer.getCustomerId())
                .email(savedCustomer.getEmail())
                .firstName(savedCustomer.getFirstName())
                .lastName(savedCustomer.getLastName())
                .emailVerified(false)
                .build();
    }

    /**
     * Refresh JWT token
     */
    public JwtResponse refreshToken(String token) {
        if (!jwtTokenProvider.validateToken(token)) {
            throw new UnauthorizedAccessException("Invalid or expired token");
        }

        Long customerId = jwtTokenProvider.getUserIdFromToken(token);
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new UnauthorizedAccessException("Customer not found"));

        if (!customer.isActiveCustomer()) {
            throw new UnauthorizedAccessException("Account is inactive");
        }

        // Generate new token
        String newToken = jwtTokenProvider.generateToken(
                customer.getCustomerId(),
                customer.getEmail(),
                "CUSTOMER"
        );

        return JwtResponse.builder()
                .accessToken(newToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpirationTime())
                .customerId(customer.getCustomerId())
                .email(customer.getEmail())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .emailVerified(customer.hasVerifiedEmail())
                .build();
    }

    /**
     * Logout customer (client should discard token)
     */
    public void logout(String token) {
        // In a stateless JWT system, logout is handled client-side
        // Here we just log the event
        if (jwtTokenProvider.validateToken(token)) {
            Long customerId = jwtTokenProvider.getUserIdFromToken(token);
            log.info("Customer {} logged out", customerId);
            // In v2.0, we might want to blacklist the token
        }
    }

    /**
     * Request password reset
     */
    public void requestPasswordReset(String email) {
        log.info("Password reset requested for email: {}", email);

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedAccessException("Email not found"));

        if (!customer.canRequestPasswordReset()) {
            throw new UnauthorizedAccessException("Account is not eligible for password reset");
        }

        // Generate reset token
        String resetToken = generatePasswordResetToken();
        customer.setPasswordResetToken(resetToken);
        customerRepository.save(customer);

        // Send reset email - FIX: Use correct method signature
        try {
            emailService.sendPasswordResetEmail(customer, resetToken);
            log.info("Password reset email sent to {}", email);
        } catch (Exception e) {
            log.error("Failed to send password reset email: {}", e.getMessage());
            throw new RuntimeException("Failed to send password reset email");
        }
    }

    /**
     * Reset password with token
     */
    public void resetPassword(String token, String newPassword) {
        log.info("Attempting password reset with token");

        // Find customer by reset token
        Customer customer = customerRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new UnauthorizedAccessException("Invalid reset token"));

        // Validate token
        if (!customer.isPasswordResetTokenValid(token)) {
            throw new UnauthorizedAccessException("Reset token is invalid or expired");
        }

        // Update password
        customer.changePassword(passwordEncoder.encode(newPassword));
        customerRepository.save(customer);

        // Send confirmation email
        try {
            emailService.sendPasswordChangeConfirmation(customer);
        } catch (Exception e) {
            log.error("Failed to send password change confirmation: {}", e.getMessage());
            // Don't fail the password reset
        }

        log.info("Password reset successful for customer ID: {}", customer.getCustomerId());
    }

    /**
     * Verify email with token
     */
    public void verifyEmail(String token, String email) {
        log.info("Email verification attempt for: {}", email);

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedAccessException("Invalid email"));

        if (!customer.isEmailVerificationTokenValid(token)) {
            throw new UnauthorizedAccessException("Invalid or expired verification token");
        }

        customer.verifyEmail();
        customerRepository.save(customer);

        log.info("Email verified for customer ID: {}", customer.getCustomerId());
    }

    /**
     * Resend verification email
     */
    public void resendVerificationEmail(String email) {
        log.info("Resending verification email to: {}", email);

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedAccessException("Email not found"));

        if (customer.hasVerifiedEmail()) {
            throw new IllegalStateException("Email is already verified");
        }

        if (!customer.needsEmailVerification()) {
            throw new IllegalStateException("Email verification not required");
        }

        // Generate new verification token
        String verificationToken = generateVerificationToken();
        customer.resendEmailVerification(verificationToken);
        customerRepository.save(customer);

        // Send verification email - FIX: Use correct method signature
        try {
            emailService.sendEmailVerification(customer);
            log.info("Verification email resent to {}", email);
        } catch (Exception e) {
            log.error("Failed to send verification email: {}", e.getMessage());
            throw new RuntimeException("Failed to send verification email");
        }
    }

    /**
     * Change password for authenticated customer
     */
    public void changePassword(Long customerId, String currentPassword, String newPassword) {
        log.info("Password change request for customer ID: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new UnauthorizedAccessException("Customer not found"));

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, customer.getPassword())) {
            throw new UnauthorizedAccessException("Current password is incorrect");
        }

        // Update password
        customer.changePassword(passwordEncoder.encode(newPassword));
        customerRepository.save(customer);

        // Send confirmation email
        try {
            emailService.sendPasswordChangeConfirmation(customer);
        } catch (Exception e) {
            log.error("Failed to send password change confirmation: {}", e.getMessage());
        }

        log.info("Password changed successfully for customer ID: {}", customerId);
    }

    /**
     * Get customer profile
     */
    public Customer getCustomerProfile(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new UnauthorizedAccessException("Customer not found"));
    }

    /**
     * Check if email is available
     */
    public boolean isEmailAvailable(String email) {
        return !customerRepository.existsByEmail(email);
    }

    // ========== Helper methods ==========

    private String generateVerificationToken() {
        return UUID.randomUUID().toString();
    }

    private String generatePasswordResetToken() {
        return UUID.randomUUID().toString();
    }

    /* VERSION 2.0 FEATURES (kommenterat bort för v1.0):
     * - Two-factor authentication (2FA)
     * - OAuth2/Social login (Google, Facebook, Apple)
     * - Remember me functionality
     * - Device tracking and management
     * - Login history
     * - Suspicious activity detection
     * - Account lockout after failed attempts
     * - Password strength requirements
     * - Session management
     * - Token blacklisting
     * - IP-based restrictions
     * - Captcha for registration
     * - Email change verification
     * - Security questions
     * - Biometric authentication support
     */
}