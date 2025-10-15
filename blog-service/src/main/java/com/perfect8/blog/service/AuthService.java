package com.perfect8.blog.service;

/* VERSION 2.0 - COMMENTED OUT FOR CLEAN V1.0 RELEASE
 * 
 * This authentication service will be uncommented and enhanced in version 2.0
 * 
 * Version 1.0 Strategy:
 * - blog-service focuses on READING posts (public access)
 * - admin-service handles ALL user authentication
 * - No separate user registration in blog-service
 * 
 * Version 2.0 Will Add:
 * - Blog-specific user authentication
 * - Comment system with user accounts
 * - Author profiles
 * - User preferences
 * 
 * Reason for commenting out:
 * - Eliminates AuthenticationManager dependency (causing crash)
 * - Simplifies v1.0 deployment
 * - Follows Magnum Opus principle: Version 1.0 fokus - kärnfunktionalitet först
 */

import com.perfect8.blog.dto.AuthDto;
import com.perfect8.blog.model.Role;
import com.perfect8.blog.model.User;
import com.perfect8.blog.repository.UserRepository;
import com.perfect8.blog.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.Set;

/**
 * Authentication Service for blog-service
 * VERSION 2.0 - Currently commented out
 * Handles user login and registration
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    // VERSION 2.0: Uncomment when adding blog user authentication
    // private final PasswordEncoder passwordEncoder;
    // private final AuthenticationManager authenticationManager;
    // private final JwtTokenProvider tokenProvider;

    /**
     * VERSION 1.0 - Placeholder method
     * Returns true to indicate service is operational
     */
    public boolean isServiceHealthy() {
        return true;
    }

    /* VERSION 2.0 - Authenticate user and generate JWT token
    public AuthDto.JwtResponse login(AuthDto.LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        String username = authentication.getName();
        String token = tokenProvider.generateToken(username);

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found after successful authentication"));

        log.info("User {} successfully logged in", username);
        return new AuthDto.JwtResponse(token, user.getUsername(), user.getEmail());
    }
    */

    /* VERSION 2.0 - Register new user and generate JWT token
    public AuthDto.JwtResponse register(AuthDto.RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Registration failed - username already exists: {}", request.getUsername());
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed - email already exists: {}", request.getEmail());
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        Set<Role> roles = new HashSet<>();
        Role readerRole = new Role();
        readerRole.setName("READER");
        roles.add(readerRole);
        user.setRoles(roles);

        userRepository.save(user);
        log.info("New user registered: {}", user.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        String username = authentication.getName();
        String token = tokenProvider.generateToken(username);

        return new AuthDto.JwtResponse(token, user.getUsername(), user.getEmail());
    }
    */

    /**
     * Check if username is available
     * VERSION 1.0 - Simplified version
     */
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    /**
     * Check if email is available
     * VERSION 1.0 - Simplified version
     */
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    /**
     * Get user by username
     * VERSION 1.0 - Simplified version
     */
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    /* VERSION 2.0 - Change user password
    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = getUserByUsername(username);

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            log.warn("Password change failed for user {} - incorrect old password", username);
            throw new RuntimeException("Incorrect old password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password successfully changed for user: {}", username);
    }
    */
}
