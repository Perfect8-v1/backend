package com.perfect8.shop.service;

import com.perfect8.shop.entity.Customer;
import com.perfect8.shop.entity.Role;
import com.perfect8.shop.repository.CustomerRepository;
import com.perfect8.shop.security.JwtTokenProvider;
import com.perfect8.shop.dto.AuthDto;
import com.perfect8.shop.dto.JwtResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Registrera ny kund
     */
    public JwtResponse register(AuthDto.RegisterRequest request) {
        log.info("Registrerar ny kund med email: {}", request.getEmail());

        // Validera att email inte redan finns
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email redan registrerad: " + request.getEmail());
        }

        // Skapa ny kund med Role enum
        Customer customer = Customer.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .role(Role.CUSTOMER) // Använder Role enum
                .emailVerified(false)
                .accountLocked(false)
                .loginAttempts(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        customer = customerRepository.save(customer);
        log.info("Kund skapad med id: {}", customer.getCustomerId());

        // Generera JWT token - JwtTokenProvider hanterar Role enum
        String token = jwtTokenProvider.generateToken(
                customer.getEmail(),
                customer.getCustomerId(),
                customer.getRole()
        );

        return JwtResponse.builder()
                .token(token)
                .email(customer.getEmail())
                .customerId(customer.getCustomerId())
                .role(customer.getRole().name()) // Konverterar till String för response
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .expiresIn(jwtTokenProvider.getJwtExpiration())
                .build();
    }

    /**
     * Logga in kund
     */
    public JwtResponse login(AuthDto.LoginRequest request) {
        log.info("Inloggningsförsök för email: {}", request.getEmail());

        Customer customer = customerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Felaktiga inloggningsuppgifter"));

        // Kontrollera om kontot är låst
        if (customer.getAccountLocked() != null && customer.getAccountLocked()) {
            log.warn("Inloggningsförsök på låst konto: {}", request.getEmail());
            throw new BadCredentialsException("Kontot är låst. Kontakta support.");
        }

        // Verifiera lösenord
        if (!passwordEncoder.matches(request.getPassword(), customer.getPassword())) {
            handleFailedLogin(customer);
            throw new BadCredentialsException("Felaktiga inloggningsuppgifter");
        }

        // Återställ misslyckade inloggningsförsök
        handleSuccessfulLogin(customer);

        // Generera JWT token med Role enum
        String token = jwtTokenProvider.generateToken(
                customer.getEmail(),
                customer.getCustomerId(),
                customer.getRole()
        );

        return JwtResponse.builder()
                .token(token)
                .email(customer.getEmail())
                .customerId(customer.getCustomerId())
                .role(customer.getRole().name()) // Konverterar till String för response
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .expiresIn(jwtTokenProvider.getJwtExpiration())
                .build();
    }

    /**
     * Validera JWT token
     */
    public boolean validateToken(String token) {
        try {
            return jwtTokenProvider.validateToken(token);
        } catch (Exception e) {
            log.error("Token validering misslyckades: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Hämta email från token
     */
    public String getEmailFromToken(String token) {
        return jwtTokenProvider.getEmailFromToken(token);
    }

    /**
     * Hämta customerId från token
     */
    public Long getCustomerIdFromToken(String token) {
        return jwtTokenProvider.getCustomerIdFromToken(token);
    }

    /**
     * Hämta role från token som enum
     */
    public Role getRoleFromToken(String token) {
        return jwtTokenProvider.getRoleFromToken(token);
    }

    /**
     * Kontrollera om användare har admin-roll
     */
    public boolean isAdmin(Long customerId) {
        return customerRepository.findById(customerId)
                .map(customer -> customer.getRole() == Role.ADMIN)
                .orElse(false);
    }

    /**
     * Kontrollera om användare har en specifik roll
     */
    public boolean hasRole(Long customerId, Role role) {
        return customerRepository.findById(customerId)
                .map(customer -> customer.getRole() == role)
                .orElse(false);
    }

    /**
     * Byt lösenord
     */
    public void changePassword(Long customerId, String oldPassword, String newPassword) {
        log.info("Lösenordsbyte för customerId: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Kund finns inte"));

        // Verifiera gammalt lösenord
        if (!passwordEncoder.matches(oldPassword, customer.getPassword())) {
            throw new BadCredentialsException("Felaktigt nuvarande lösenord");
        }

        // Uppdatera lösenord
        customer.setPassword(passwordEncoder.encode(newPassword));
        customer.setPasswordChangedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());

        customerRepository.save(customer);
        log.info("Lösenord uppdaterat för customerId: {}", customerId);
    }

    /**
     * Återställ lösenord (för glömt lösenord)
     */
    public void resetPassword(String email, String newPassword) {
        log.info("Lösenordsåterställning för email: {}", email);

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email finns inte"));

        customer.setPassword(passwordEncoder.encode(newPassword));
        customer.setPasswordChangedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());

        customerRepository.save(customer);
        log.info("Lösenord återställt för email: {}", email);
    }

    /**
     * Verifiera email
     */
    public void verifyEmail(Long customerId) {
        log.info("Email verifiering för customerId: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Kund finns inte"));

        customer.setEmailVerified(true);
        customer.setEmailVerifiedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());

        customerRepository.save(customer);
        log.info("Email verifierad för customerId: {}", customerId);
    }

    /**
     * Hantera misslyckad inloggning
     */
    private void handleFailedLogin(Customer customer) {
        int attempts = customer.getLoginAttempts() != null ? customer.getLoginAttempts() + 1 : 1;
        customer.setLoginAttempts(attempts);

        // Lås konto efter 5 misslyckade försök
        if (attempts >= 5) {
            customer.setAccountLocked(true);
            customer.setAccountLockedAt(LocalDateTime.now());
            log.warn("Konto låst efter {} misslyckade inloggningsförsök: {}",
                    attempts, customer.getEmail());
        }

        customer.setUpdatedAt(LocalDateTime.now());
        customerRepository.save(customer);
    }

    /**
     * Hantera lyckad inloggning
     */
    private void handleSuccessfulLogin(Customer customer) {
        customer.setLoginAttempts(0);
        customer.setLastLoginAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());
        customerRepository.save(customer);
    }

    /**
     * Lås upp konto (admin funktion)
     */
    public void unlockAccount(Long customerId) {
        log.info("Låser upp konto för customerId: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Kund finns inte"));

        customer.setAccountLocked(false);
        customer.setAccountLockedAt(null);
        customer.setLoginAttempts(0);
        customer.setUpdatedAt(LocalDateTime.now());

        customerRepository.save(customer);
        log.info("Konto upplåst för customerId: {}", customerId);
    }

    /**
     * Uppdatera användarens roll (endast admin kan göra detta)
     */
    public void updateUserRole(Long customerId, Role newRole, Long adminId) {
        log.info("Uppdaterar roll för customerId: {} till {}", customerId, newRole);

        // Verifiera att den som gör ändringen är admin
        if (!isAdmin(adminId)) {
            throw new SecurityException("Endast admin kan ändra roller");
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Kund finns inte"));

        Role oldRole = customer.getRole();
        customer.setRole(newRole);
        customer.setUpdatedAt(LocalDateTime.now());

        customerRepository.save(customer);
        log.info("Roll uppdaterad från {} till {} för customerId: {}",
                oldRole, newRole, customerId);
    }

    // ===== VERSION 2.0 FUNKTIONER - KOMMENTERADE =====

    /*
    // Two-factor authentication
    public void enableTwoFactor(Long customerId) {
        // Version 2.0
    }

    // OAuth integration
    public JwtResponse loginWithGoogle(String googleToken) {
        // Version 2.0
    }

    // Session management
    public void logoutAllDevices(Long customerId) {
        // Version 2.0
    }

    // Login analytics
    public LoginMetrics getLoginMetrics(LocalDateTime from, LocalDateTime to) {
        // Version 2.0
    }

    // Security audit log
    public List<SecurityEvent> getSecurityEvents(Long customerId) {
        // Version 2.0
    }

    // IP-based security
    public void addTrustedIp(Long customerId, String ipAddress) {
        // Version 2.0
    }

    // Device fingerprinting
    public void registerDevice(Long customerId, DeviceInfo device) {
        // Version 2.0
    }

    // Biometric authentication
    public void enableBiometric(Long customerId, BiometricData data) {
        // Version 2.0
    }
    */
}