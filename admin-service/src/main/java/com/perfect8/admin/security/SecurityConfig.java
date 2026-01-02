package com.perfect8.admin.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security Configuration for Admin Service
 * 
 * IMPORTANT: This service now trusts Spring Cloud Gateway for authentication.
 * Gateway validates JWT tokens and adds user info to request headers:
 * - X-Auth-User: username
 * - X-Auth-Role: ROLE_ADMIN / ROLE_USER
 * 
 * This service does NOT validate JWT tokens - Gateway handles that.
 * All requests reaching this service have already been authenticated by Gateway.
 * 
 * @author Perfect8 Team
 * @version 2.0.0 (FAS 3 - Gateway Integration)
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Security Filter Chain - Trust Gateway
     * 
     * Gateway handles:
     * - JWT validation
     * - Public/protected endpoint rules
     * - Authentication
     * 
     * This service:
     * - Trusts Gateway headers (X-Auth-User, X-Auth-Role)
     * - Allows all requests (Gateway has already authenticated)
     * - Maintains stateless session policy
     * 
     * @param http HttpSecurity configuration
     * @return SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            // Disable CSRF - stateless API with JWT (validated by Gateway)
            .csrf(csrf -> csrf.disable())
            
            // Authorization rules - trust Gateway
            .authorizeHttpRequests(auth -> auth
                // Allow all requests - Gateway has already authenticated
                .anyRequest().permitAll()
            )
            
            // Session management - stateless
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            .build();
    }

    /**
     * Password encoder for user authentication
     * 
     * BCrypt with strength 10 (default)
     * 
     * CRITICAL: This bean is required by:
     * - DataInitializer (creates default admin user)
     * - AuthService.login() (verifies passwords)
     * - AuthService.register() (hashes new user passwords)
     * 
     * CURRENT IMPLEMENTATION (v1.0):
     * Frontend hashes passwords with bcrypt before transmission (defense in depth).
     * This encoder hashes the frontend hash again, storing hash-of-hash in database.
     * 
     * TODO (v2.0): Remove frontend password hashing
     * - Change frontend to send plaintext passwords over HTTPS
     * - Remove bcrypt from Dart/Flutter code
     * - This encoder will then hash plaintext directly (industry standard)
     * - Simpler, more maintainable, equally secure with HTTPS
     * - See: Password_Security_Discussion_Perfect8.md for full analysis
     * 
     * @return BCryptPasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Authentication Manager
     * 
     * CRITICAL: This bean is required by:
     * - AuthController (for login authentication)
     * 
     * Spring Security's AuthenticationManager handles the actual authentication process.
     * It uses the configured UserDetailsService and PasswordEncoder to verify credentials.
     * 
     * @param authenticationConfiguration Spring's authentication configuration
     * @return AuthenticationManager
     * @throws Exception if authentication manager cannot be created
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
