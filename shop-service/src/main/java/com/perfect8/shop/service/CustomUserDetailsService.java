package com.perfect8.shop.service;

import com.perfect8.shop.entity.Customer;
import com.perfect8.shop.repository.CustomerRepository;
import com.perfect8.common.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Custom UserDetailsService implementation for Spring Security
 * Version 1.0 - Core authentication functionality
 * FIXED: Using CORRECT Lombok-generated method names for boolean fields!
 */
@Service("customUserDetailsService")
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final CustomerRepository customerRepository;

    /**
     * Load user by username (email in our case)
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user by email: {}", email);

        if (email == null || email.trim().isEmpty()) {
            throw new UsernameNotFoundException("Email cannot be empty");
        }

        // Find customer by email
        Customer customer = customerRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });

        // Check if customer is active - FIXED: Using isActive() not getIsActive()
        if (!customer.isActive()) {
            log.warn("Customer account is inactive: {}", email);
            throw new UsernameNotFoundException("Account is inactive");
        }

        // Build and return UserDetails
        return createUserDetails(customer);
    }

    /**
     * Load user by customer ID
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long customerId) throws UsernameNotFoundException {
        log.debug("Loading user by ID: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", customerId);
                    return new UsernameNotFoundException("User not found with ID: " + customerId);
                });

        // Check if customer is active - FIXED: Using isActive()
        if (!customer.isActive()) {
            log.warn("Customer account is inactive - ID: {}", customerId);
            throw new UsernameNotFoundException("Account is inactive");
        }

        return createUserDetails(customer);
    }

    /**
     * Create Spring Security UserDetails from Customer entity
     */
    private UserDetails createUserDetails(Customer customer) {
        List<GrantedAuthority> authorities = getAuthorities(customer);

        // Build user details - FIXED: Using correct method names
        return User.builder()
                .username(customer.getEmail())
                .password(customer.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(!customer.isActive())
                .credentialsExpired(false)
                .disabled(!customer.isActive())
                .build();
    }

    /**
     * Get authorities for customer
     * FIXED: Handle String role properly, convert to Role enum
     */
    private List<GrantedAuthority> getAuthorities(Customer customer) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // Get role as String and add directly
        String roleString = customer.getRole();
        if (roleString != null && !roleString.isEmpty()) {
            // Add the role directly as authority
            authorities.add(new SimpleGrantedAuthority(roleString));

            // Convert to Role enum for additional authorities
            try {
                Role roleEnum = Role.valueOf(roleString.replace("ROLE_", ""));

                // Add additional authorities based on role hierarchy
                if (roleEnum == Role.ADMIN) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                    authorities.add(new SimpleGrantedAuthority("ROLE_STAFF"));
                    authorities.add(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
                } else if (roleEnum == Role.STAFF) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_STAFF"));
                    authorities.add(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
                } else {
                    authorities.add(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
                }
            } catch (IllegalArgumentException e) {
                // If conversion fails, default to CUSTOMER
                log.warn("Invalid role string: {}, defaulting to CUSTOMER", roleString);
                authorities.add(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
            }
        } else {
            // Default to CUSTOMER role if none set
            authorities.add(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
        }

        // Add feature-based authorities
        // FIXED: Using isEmailVerified() not getIsEmailVerified()
        if (customer.isEmailVerified()) {
            authorities.add(new SimpleGrantedAuthority("EMAIL_VERIFIED"));
        }

        return authorities;
    }

    /**
     * Create UserDetails with custom attributes
     */
    public UserDetails createUserDetailsWithAttributes(Customer customer) {
        return new CustomUserPrincipal(customer, getAuthorities(customer));
    }

    /**
     * Custom UserPrincipal class for extended user information
     */
    public static class CustomUserPrincipal implements UserDetails {

        private final Customer customer;
        private final Collection<? extends GrantedAuthority> authorities;

        public CustomUserPrincipal(Customer customer, Collection<? extends GrantedAuthority> authorities) {
            this.customer = customer;
            this.authorities = authorities;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return authorities;
        }

        @Override
        public String getPassword() {
            return customer.getPassword();
        }

        @Override
        public String getUsername() {
            return customer.getEmail();
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            // FIXED: Using isActive() for boolean field
            return customer.isActive();
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            // FIXED: Using isActive() for boolean field
            return customer.isActive();
        }

        // Custom getters - using CORRECT method names!
        public Long getCustomerId() {
            return customer.getCustomerId();
        }

        public String getFirstName() {
            return customer.getFirstName();
        }

        public String getLastName() {
            return customer.getLastName();
        }

        public String getFullName() {
            return customer.getFullName();
        }

        public String getEmail() {
            return customer.getEmail();
        }

        public Customer getCustomer() {
            return customer;
        }

        public boolean isEmailVerified() {
            // FIXED: Using isEmailVerified() for boolean field
            return customer.isEmailVerified();
        }

        public boolean hasRole(String role) {
            String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;
            return authorities.stream()
                    .anyMatch(auth -> auth.getAuthority().equals(roleWithPrefix));
        }
    }

    /**
     * Validate password
     */
    public boolean validatePassword(String rawPassword, String encodedPassword) {
        // This should use PasswordEncoder in production
        // For now, just compare (assuming passwords are already encoded)
        return rawPassword != null && encodedPassword != null &&
                rawPassword.equals(encodedPassword);
    }

    /**
     * Check if email exists
     */
    public boolean emailExists(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return customerRepository.existsByEmail(email.toLowerCase());
    }

    /**
     * Get customer by email
     */
    public Customer getCustomerByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        return customerRepository.findByEmail(email.toLowerCase()).orElse(null);
    }

    // Version 2.0 features - commented out for later
    /*
    // OAuth2 support
    public UserDetails loadUserByOAuth2(String provider, String providerId) {
        // Implementation for OAuth2 login
    }

    // Two-factor authentication
    public boolean validateTwoFactorCode(String email, String code) {
        // Implementation for 2FA
    }

    // Session management
    public void updateLastLogin(String email) {
        // Update last login timestamp
    }

    // Permission-based authorization
    public boolean hasPermission(String email, String permission) {
        // Check specific permissions
    }
    */
}