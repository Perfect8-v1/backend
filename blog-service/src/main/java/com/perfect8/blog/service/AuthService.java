package com.perfect8.blog.service;

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
 * Handles user login and registration
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    /**
     * Authenticate user and generate JWT token
     */
    public AuthDto.JwtResponse login(AuthDto.LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // FIXED: Extract username from Authentication object
        String username = authentication.getName();
        String token = tokenProvider.generateToken(username);

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found after successful authentication"));

        log.info("User {} successfully logged in", username);
        return new AuthDto.JwtResponse(token, user.getUsername(), user.getEmail());
    }

    /**
     * Register new user and generate JWT token
     */
    public AuthDto.JwtResponse register(AuthDto.RegisterRequest request) {
        // Validate username availability
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Registration failed - username already exists: {}", request.getUsername());
            throw new RuntimeException("Username already exists");
        }

        // Validate email availability
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed - email already exists: {}", request.getEmail());
            throw new RuntimeException("Email already exists");
        }

        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Default role is READER for blog users
        Set<Role> roles = new HashSet<>();
        Role readerRole = new Role();
        readerRole.setName("READER");
        roles.add(readerRole);
        user.setRoles(roles);

        userRepository.save(user);
        log.info("New user registered: {}", user.getUsername());

        // Authenticate the newly registered user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // FIXED: Extract username from Authentication object
        String username = authentication.getName();
        String token = tokenProvider.generateToken(username);

        return new AuthDto.JwtResponse(token, user.getUsername(), user.getEmail());
    }

    /**
     * Check if username is available
     */
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    /**
     * Check if email is available
     */
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    /**
     * Get user by username
     */
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    /**
     * Change user password
     */
    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = getUserByUsername(username);

        // Verify old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            log.warn("Password change failed for user {} - incorrect old password", username);
            throw new RuntimeException("Incorrect old password");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password successfully changed for user: {}", username);
    }
}