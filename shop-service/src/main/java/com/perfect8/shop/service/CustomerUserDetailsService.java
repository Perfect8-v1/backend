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
 * CustomerUserDetailsService - Spring Security UserDetailsService implementation
 * Version 1.0 - Core authentication functionality
 *
 * Magnum Opus Principles:
 * - Readable variable names (customerEmail not email, customerId not customerEmailDTOId)
 * - Uses passwordHash (NEVER password)
 * - NO backward compatibility - built right from start
 * - NO alias methods - uses exact Lombok-generated method names
 *
 * SECURITY MODEL:
 * - Frontend handles passwordHash creation
 * - Backend only stores and compares passwordHash
 */
@Service("customerUserDetailsService")
@RequiredArgsConstructor
@Slf4j
public class CustomerUserDetailsService implements UserDetailsService {

    private final CustomerRepository customerRepository;

    /**
     * Load user by username (email in our case)
     * NOTE: Method signature must match Spring Security interface
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // username is the customer's email in our system
        String customerEmail = username;
        log.debug("Loading user by customerEmail: {}", customerEmail);

        if (customerEmail == null || customerEmail.trim().isEmpty()) {
            throw new UsernameNotFoundException("Email cannot be empty");
        }

        // Find customer by email
        Customer customer = customerRepository.findByEmail(customerEmail.toLowerCase())
                .orElseThrow(() -> {
                    log.warn("User not found with customerEmail: {}", customerEmail);
                    return new UsernameNotFoundException("User not found with email: " + customerEmail);
                });

        // Check if customer is active
        if (!customer.isActive()) {
            log.warn("Customer account is inactive: {}", customerEmail);
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
        log.debug("Loading user by customerId: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> {
                    log.warn("User not found with customerId: {}", customerId);
                    return new UsernameNotFoundException("User not found with ID: " + customerId);
                });

        // Check if customer is active
        if (!customer.isActive()) {
            log.warn("Customer account is inactive - customerId: {}", customerId);
            throw new UsernameNotFoundException("Account is inactive");
        }

        return createUserDetails(customer);
    }

    /**
     * Create Spring Security UserDetails from Customer entity
     * NOTE: Uses getPasswordHash() - Customer stores hashed passwords
     */
    private UserDetails createUserDetails(Customer customer) {
        List<GrantedAuthority> authorities = getAuthorities(customer);

        // Build user details with correct Lombok method names
        // IMPORTANT: Using getPasswordHash() - matches Customer entity field name
        return User.builder()
                .username(customer.getEmail())
                .password(customer.getPasswordHash())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(!customer.isActive())
                .credentialsExpired(false)
                .disabled(!customer.isActive())
                .build();
    }

    /**
     * Get authorities for customer
     * Handle String role properly, convert to Role enum
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
        if (customer.isEmailVerified()) {
            authorities.add(new SimpleGrantedAuthority("EMAIL_VERIFIED"));
        }

        return authorities;
    }

    /**
     * Create UserDetails with custom attributes
     */
    public UserDetails createUserDetailsWithAttributes(Customer customer) {
        return new CustomerUserPrincipal(customer, getAuthorities(customer));
    }

    /**
     * CustomerUserPrincipal class for extended user information
     */
    public static class CustomerUserPrincipal implements UserDetails {

        private final Customer customer;
        private final Collection<? extends GrantedAuthority> authorities;

        public CustomerUserPrincipal(Customer customer, Collection<? extends GrantedAuthority> authorities) {
            this.customer = customer;
            this.authorities = authorities;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return authorities;
        }

        @Override
        public String getPassword() {
            // Spring Security expects getPassword(), but we store passwordHash
            return customer.getPasswordHash();
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
            return customer.isActive();
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return customer.isActive();
        }

        // Custom getters - using correct Lombok method names
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

        public String getCustomerEmail() {
            return customer.getEmail();
        }

        public Customer getCustomer() {
            return customer;
        }

        public boolean isEmailVerified() {
            return customer.isEmailVerified();
        }

        public boolean hasRole(String role) {
            String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;
            return authorities.stream()
                    .anyMatch(auth -> auth.getAuthority().equals(roleWithPrefix));
        }
    }

    /**
     * Validate passwordHash
     * FIXED: Frontend sends passwordHash - compare directly (NO passwordEncoder)
     */
    public boolean validatePasswordHash(String passwordHash, String storedPasswordHash) {
        return passwordHash != null && storedPasswordHash != null &&
                passwordHash.equals(storedPasswordHash);
    }

    /**
     * Check if customer email exists
     */
    public boolean customerEmailExists(String customerEmail) {
        if (customerEmail == null || customerEmail.trim().isEmpty()) {
            return false;
        }
        return customerRepository.existsByEmail(customerEmail.toLowerCase());
    }

    /**
     * Get customer by email
     */
    public Customer getCustomerByEmail(String customerEmail) {
        if (customerEmail == null || customerEmail.trim().isEmpty()) {
            return null;
        }
        return customerRepository.findByEmail(customerEmail.toLowerCase()).orElse(null);
    }

    // Version 2.0 features - commented out for later
    /*
    // OAuth2 support
    public UserDetails loadUserByOAuth2(String provider, String providerId) {
        // Implementation for OAuth2 login
    }

    // Two-factor authentication
    public boolean validateTwoFactorCode(String customerEmail, String code) {
        // Implementation for 2FA
    }

    // Session management
    public void updateLastLogin(String customerEmail) {
        // Update last login timestamp
    }

    // Permission-based authorization
    public boolean hasPermission(String customerEmail, String permission) {
        // Check specific permissions
    }
    */
}