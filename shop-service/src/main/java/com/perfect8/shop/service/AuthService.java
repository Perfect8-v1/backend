package com.perfect8.shop.service;

import com.perfect8.shop.entity.Customer;
import com.perfect8.shop.repository.CustomerRepository;
import com.perfect8.shop.security.JwtTokenProvider;
import com.perfect8.shop.dto.AuthDto;
import com.perfect8.shop.exception.CustomerNotFoundException;
import com.perfect8.shop.exception.UnauthorizedAccessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Authentication Service - Version 1.0
 * Handles authentication and authorization
 * NO BACKWARD COMPATIBILITY - Built right from the start!
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
     * Register new customer
     */
    @Transactional
    public Customer registerCustomer(AuthDto.RegisterRequest registerRequest) {
        log.info("Registering new customer with email: {}", registerRequest.getEmail());

        // Check if email already exists
        if (customerRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BadCredentialsException("Email already registered");
        }

        // Create new customer - using CORRECT field names
        // VERSION 1.0 - Newsletter och marketing consent är inte kritiska
        Customer newCustomer = Customer.builder()
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .phoneNumber(registerRequest.getPhoneNumber())
                .role("ROLE_USER")
                .isActive(true)
                .isEmailVerified(false)
                .newsletterSubscribed(false)  // Default för v1.0
                .marketingConsent(false)      // Default för v1.0
                .build();

        Customer savedCustomer = customerRepository.save(newCustomer);

        // Send welcome email - CORRECT signature (2 parameters)
        String fullName = savedCustomer.getFirstName() + " " + savedCustomer.getLastName();
        emailService.sendWelcomeEmail(
                savedCustomer.getEmail(),
                fullName
        );

        log.info("Customer registered successfully with ID: {}", savedCustomer.getCustomerId());
        return savedCustomer;
    }

    /**
     * Authenticate customer
     */
    @Transactional
    public AuthDto.JwtResponse authenticate(AuthDto.LoginRequest loginRequest) {
        log.info("Authenticating customer: {}", loginRequest.getEmail());

        Customer customer = customerRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        // Verify password
        if (!passwordEncoder.matches(loginRequest.getPassword(), customer.getPassword())) {
            customer.incrementFailedLoginAttempts();
            customerRepository.save(customer);
            log.warn("Failed login attempt for email: {}", loginRequest.getEmail());
            throw new BadCredentialsException("Invalid email or password");
        }

        // Check if account is active
        if (!customer.isActive()) {
            throw new UnauthorizedAccessException("Account is disabled");
        }

        // Check if account is locked
        if (customer.isAccountLocked()) {
            throw new UnauthorizedAccessException("Account is locked");
        }

        // Update last login and reset failed attempts
        customer.updateLastLoginTime();
        customer.resetFailedLoginAttempts();
        customerRepository.save(customer);

        // Generate JWT token with correct signature (3 parameters)
        Long customerIdLong = customer.getCustomerId();
        String jwtToken = jwtTokenProvider.generateToken(
                customerIdLong,
                customer.getEmail(),
                customer.getRole()
        );

        log.info("Customer {} authenticated successfully", customer.getEmail());

        return AuthDto.JwtResponse.builder()
                .token(jwtToken)
                .tokenType("Bearer")
                .expiresIn(3600L) // 1 hour
                .customerId(customerIdLong)
                .email(customer.getEmail())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .role(customer.getRole())
                .build();
    }

    /**
     * Refresh token
     */
    @Transactional(readOnly = true)
    public AuthDto.JwtResponse refreshToken(String customerEmail) {
        Customer customer = customerRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

        Long customerIdLong = customer.getCustomerId();
        String newJwtToken = jwtTokenProvider.generateToken(
                customerIdLong,
                customer.getEmail(),
                customer.getRole()
        );

        return AuthDto.JwtResponse.builder()
                .token(newJwtToken)
                .tokenType("Bearer")
                .expiresIn(3600L)
                .customerId(customerIdLong)
                .email(customer.getEmail())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .role(customer.getRole())
                .build();
    }

    /**
     * Change password
     */
    @Transactional
    public void changePassword(Long customerIdLong, String oldPasswordString, String newPasswordString) {
        Customer customer = customerRepository.findById(customerIdLong)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

        // Verify old password
        if (!passwordEncoder.matches(oldPasswordString, customer.getPassword())) {
            throw new BadCredentialsException("Current password is incorrect");
        }

        // Update password
        customer.setPassword(passwordEncoder.encode(newPasswordString));
        customerRepository.save(customer);

        log.info("Password changed for customer ID: {}", customerIdLong);
    }

    /**
     * Reset password request
     */
    @Transactional
    public void requestPasswordReset(String customerEmail) {
        Customer customer = customerRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

        // Generate reset token
        String resetTokenString = generateResetToken();
        customer.setPasswordResetToken(resetTokenString);
        customer.setPasswordResetExpiresAt(LocalDateTime.now().plusHours(1));
        customerRepository.save(customer);

        // Send reset email - FIXED: Using correct EmailService method signature
        emailService.sendPasswordResetEmail(
                customer.getEmail(),
                resetTokenString  // FIXED: Send the token, not the fullName
        );

        log.info("Password reset requested for email: {}", customerEmail);
    }

    /**
     * Reset password with token
     */
    @Transactional
    public void resetPassword(String resetTokenString, String newPasswordString) {
        Customer customer = customerRepository.findByPasswordResetToken(resetTokenString)
                .orElseThrow(() -> new BadCredentialsException("Invalid reset token"));

        // Check token expiry
        if (customer.getPasswordResetExpiresAt() != null &&
                customer.getPasswordResetExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadCredentialsException("Reset token has expired");
        }

        // Update password
        customer.setPassword(passwordEncoder.encode(newPasswordString));
        customer.setPasswordResetToken(null);
        customer.setPasswordResetExpiresAt(null);
        customerRepository.save(customer);

        log.info("Password reset successfully for customer: {}", customer.getEmail());
    }

    /**
     * Verify email
     */
    @Transactional
    public void verifyEmail(String verificationTokenString) {
        Customer customer = customerRepository.findByEmailVerificationToken(verificationTokenString)
                .orElseThrow(() -> new BadCredentialsException("Invalid verification token"));

        // Check token expiry (24 hours from when it was sent)
        if (customer.getEmailVerificationSentAt() != null &&
                customer.getEmailVerificationSentAt().isBefore(LocalDateTime.now().minusHours(24))) {
            throw new BadCredentialsException("Verification token has expired");
        }

        // Mark email as verified
        customer.setEmailVerified(true);
        customer.setEmailVerificationToken(null);
        customer.setEmailVerificationSentAt(null);
        customerRepository.save(customer);

        log.info("Email verified for customer: {}", customer.getEmail());
    }

    /**
     * Resend verification email
     */
    @Transactional
    public void resendVerificationEmail(String customerEmail) {
        Customer customer = customerRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

        if (customer.isEmailVerified()) {
            throw new BadCredentialsException("Email already verified");
        }

        // Generate new verification token
        String verificationTokenString = generateVerificationToken();
        customer.setEmailVerificationToken(verificationTokenString);
        customer.setEmailVerificationSentAt(LocalDateTime.now());
        customerRepository.save(customer);

        // Send verification email - FIXED: Using correct method
        emailService.sendEmailVerification(
                customer.getEmail(),
                verificationTokenString
        );

        log.info("Verification email resent to: {}", customerEmail);
    }

    /**
     * Validate JWT token
     */
    public boolean validateToken(String jwtTokenString) {
        return jwtTokenProvider.validateToken(jwtTokenString);
    }

    /**
     * Get customer ID from token
     */
    public Long getCustomerIdFromToken(String jwtTokenString) {
        return jwtTokenProvider.getCustomerIdFromToken(jwtTokenString);
    }

    /**
     * Check if customer has role
     */
    public boolean hasRole(Long customerIdLong, String requiredRoleString) {
        Customer customer = customerRepository.findById(customerIdLong)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

        // Simple string comparison for v1.0
        return customer.getRole().equals(requiredRoleString) ||
                customer.getRole().equals("ROLE_ADMIN"); // Admin has all permissions
    }

    /**
     * Generate reset token
     */
    private String generateResetToken() {
        UUID randomUuid = UUID.randomUUID();
        return randomUuid.toString();
    }

    /**
     * Generate verification token
     */
    private String generateVerificationToken() {
        UUID randomUuid = UUID.randomUUID();
        return randomUuid.toString();
    }
}