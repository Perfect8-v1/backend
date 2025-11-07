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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Authentication Service - Version 1.0
 * Handles authentication and authorization
 *
 * SECURITY MODEL:
 * - Frontend creates passwordHash (NEVER sends plain password)
 * - Backend stores and compares passwordHash only
 * - NO passwordEncoder in this service - Frontend handles hashing
 *
 * Magnum Opus Principles:
 * - Readable variable names (customerId not customerEmailDTOId)
 * - NO backward compatibility - built right from start
 * - NO alias methods - one method, one name
 * - Use passwordHash ONLY - never password
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final CustomerRepository customerRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;

    /**
     * Register new customer
     * Frontend sends passwordHash - we store it directly
     */
    @Transactional
    public Customer registerCustomer(AuthDto.RegisterRequest registerRequest) {
        log.info("Registering new customer with email: {}", registerRequest.getEmail());

        // Check if email already exists
        if (customerRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BadCredentialsException("Email already registered");
        }

        // Create new customer - FIXED: Use passwordHash directly from frontend
        Customer newCustomer = Customer.builder()
                .email(registerRequest.getEmail())
                .passwordHash(registerRequest.getPasswordHash())  // FIXED: No encoding - frontend sends hash
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .phone(registerRequest.getPhone())
                .role("ROLE_USER")
                .active(true)
                .emailVerified(false)
                .newsletterSubscribed(false)
                .marketingConsent(false)
                .build();

        Customer savedCustomer = customerRepository.save(newCustomer);

        // Send welcome email
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
     * Frontend sends passwordHash - we compare directly
     */
    @Transactional
    public AuthDto.JwtResponse authenticate(AuthDto.LoginRequest loginRequest) {
        log.info("Authenticating customer: {}", loginRequest.getEmail());

        Customer customer = customerRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or passwordHash"));

        // FIXED: Compare passwordHash directly - NO passwordEncoder.matches()
        if (!customer.getPasswordHash().equals(loginRequest.getPasswordHash())) {
            customer.incrementFailedLoginAttempts();
            customerRepository.save(customer);
            log.warn("Failed login attempt for email: {}", loginRequest.getEmail());
            throw new BadCredentialsException("Invalid email or passwordHash");
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

        // Generate JWT token
        Long customerId = customer.getCustomerId();
        String jwtToken = jwtTokenProvider.generateToken(
                customerId,
                customer.getEmail(),
                customer.getRole()
        );

        log.info("Customer {} authenticated successfully", customer.getEmail());

        return AuthDto.JwtResponse.builder()
                .token(jwtToken)
                .tokenType("Bearer")
                .expiresIn(3600L)
                .customerId(customerId)
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

        Long customerId = customer.getCustomerId();
        String newJwtToken = jwtTokenProvider.generateToken(
                customerId,
                customer.getEmail(),
                customer.getRole()
        );

        return AuthDto.JwtResponse.builder()
                .token(newJwtToken)
                .tokenType("Bearer")
                .expiresIn(3600L)
                .customerId(customerId)
                .email(customer.getEmail())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .role(customer.getRole())
                .build();
    }

    /**
     * Change password
     * FIXED: Takes passwordHash parameters - frontend sends hashes
     */
    @Transactional
    public void changePassword(Long customerId, String oldPasswordHash, String newPasswordHash) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

        // FIXED: Compare passwordHash directly - NO passwordEncoder.matches()
        if (!customer.getPasswordHash().equals(oldPasswordHash)) {
            throw new BadCredentialsException("Current passwordHash is incorrect");
        }

        // FIXED: Store new passwordHash directly - NO encoding
        customer.setPasswordHash(newPasswordHash);
        customerRepository.save(customer);

        log.info("Password changed for customer ID: {}", customerId);
    }

    /**
     * Reset password request
     */
    @Transactional
    public void requestPasswordReset(String customerEmail) {
        Customer customer = customerRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

        // Generate reset token
        String resetToken = generateResetToken();
        customer.setResetPasswordToken(resetToken);
        customer.setResetPasswordTokenExpiry(LocalDateTime.now().plusHours(1));
        customerRepository.save(customer);

        // Send reset email
        emailService.sendPasswordResetEmail(
                customer.getEmail(),
                resetToken
        );

        log.info("Password reset requested for email: {}", customerEmail);
    }

    /**
     * Reset password with token
     * FIXED: Takes passwordHash - frontend sends hash
     */
    @Transactional
    public void resetPassword(String resetToken, String newPasswordHash) {
        Customer customer = customerRepository.findByResetPasswordToken(resetToken)
                .orElseThrow(() -> new BadCredentialsException("Invalid reset token"));

        // Check token expiry
        if (customer.getResetPasswordTokenExpiry() != null &&
                customer.getResetPasswordTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new BadCredentialsException("Reset token has expired");
        }

        // FIXED: Store passwordHash directly - NO encoding
        customer.setPasswordHash(newPasswordHash);
        customer.setResetPasswordToken(null);
        customer.setResetPasswordTokenExpiry(null);
        customerRepository.save(customer);

        log.info("Password reset successfully for customer: {}", customer.getEmail());
    }

    /**
     * Verify email
     */
    @Transactional
    public void verifyEmail(String verificationToken) {
        Customer customer = customerRepository.findByEmailVerificationToken(verificationToken)
                .orElseThrow(() -> new BadCredentialsException("Invalid verification token"));

        // Check token expiry (24 hours)
        if (customer.getEmailVerificationSentDate() != null &&
                customer.getEmailVerificationSentDate().isBefore(LocalDateTime.now().minusHours(24))) {
            throw new BadCredentialsException("Verification token has expired");
        }

        // Mark email as verified
        customer.setEmailVerified(true);
        customer.setEmailVerificationToken(null);
        customer.setEmailVerificationSentDate(null);
        customer.setEmailVerifiedDate(LocalDateTime.now());
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
        String verificationToken = generateVerificationToken();
        customer.setEmailVerificationToken(verificationToken);
        customer.setEmailVerificationSentDate(LocalDateTime.now());
        customerRepository.save(customer);

        // Send verification email
        emailService.sendEmailVerification(
                customer.getEmail(),
                verificationToken
        );

        log.info("Verification email resent to: {}", customerEmail);
    }

    /**
     * Validate JWT token
     */
    public boolean validateToken(String jwtToken) {
        return jwtTokenProvider.validateToken(jwtToken);
    }

    /**
     * Get customer ID from token
     */
    public Long getCustomerIdFromToken(String jwtToken) {
        return jwtTokenProvider.getCustomerIdFromToken(jwtToken);
    }

    /**
     * Check if customer has role
     */
    public boolean hasRole(Long customerId, String requiredRole) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

        return customer.getRole().equals(requiredRole) ||
                customer.getRole().equals("ROLE_ADMIN");
    }

    /**
     * Generate reset token
     */
    private String generateResetToken() {
        return UUID.randomUUID().toString();
    }

    /**
     * Generate verification token
     */
    private String generateVerificationToken() {
        return UUID.randomUUID().toString();
    }
}