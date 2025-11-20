package com.perfect8.admin.controller;

import com.perfect8.admin.dto.*;
import com.perfect8.admin.model.User;
import com.perfect8.admin.model.RefreshToken;
import com.perfect8.admin.repository.UserRepository;
import com.perfect8.admin.repository.RefreshTokenRepository;
import com.perfect8.admin.util.JwtUtil;
import com.perfect8.common.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // Refresh token validity in days
    private static final int REFRESH_TOKEN_VALIDITY_DAYS = 30;

    /**
     * Login endpoint - returns access token and refresh token
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            log.info("Login attempt for email: {}", request.getEmail());

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new BadCredentialsException("User not found"));

            // Check if account is locked
            if (user.isLocked()) {
                log.warn("Account locked for user: {}", request.getEmail());
                return ResponseEntity.status(HttpStatus.LOCKED)
                        .body(Map.of("error", "Account is locked. Try again later."));
            }

            // Check if account is active
            if (!user.isActive()) {
                log.warn("Inactive account login attempt: {}", request.getEmail());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Account is not active"));
            }

            // Reset failed login attempts on successful login
            user.setFailedLoginAttempts(0);
            user.setAccountLockedUntil(null);
            user.setLastLoginDate(LocalDateTime.now());
            userRepository.save(user);

            // Generate tokens
            String accessToken = jwtUtil.generateToken(user.getEmail(), user.getRoles());
            String refreshToken = generateRefreshToken(user);

            // Build response
            LoginResponse response = LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtUtil.getExpirationTime())
                    .user(buildUserDto(user))
                    .build();

            log.info("Login successful for user: {}", user.getEmail());
            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            log.warn("Failed login attempt for email: {}", request.getEmail());
            
            // Increment failed login attempts
            userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
                user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
                
                // Lock account after 5 failed attempts
                if (user.getFailedLoginAttempts() >= 5) {
                    user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(30));
                    log.warn("Account locked due to too many failed attempts: {}", request.getEmail());
                }
                userRepository.save(user);
            });

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid email or password"));
        }
    }

    /**
     * Register new user
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration attempt for email: {}", request.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed - email already exists: {}", request.getEmail());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Email already registered"));
        }

        // Determine role (default to USER/CUSTOMER)
        Set<Role> roles = new HashSet<>();
        if (request.getRole() != null && !request.getRole().isEmpty()) {
            Role role = Role.fromName(request.getRole());
            if (role != null) {
                roles.add(role);
            } else {
                roles.add(Role.USER);
            }
        } else {
            roles.add(Role.USER);
        }

        // Create new user
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .roles(roles)
                .isActive(true)
                .isEmailVerified(false)
                .emailVerificationToken(UUID.randomUUID().toString())
                .build();

        userRepository.save(user);

        log.info("User registered successfully: {}", user.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "userId", user.getUserId(),
                        "email", user.getEmail(),
                        "message", "Registration successful"
                ));
    }

    /**
     * Refresh access token using refresh token
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        String refreshTokenStr = request.get("refreshToken");

        if (refreshTokenStr == null || refreshTokenStr.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Refresh token is required"));
        }

        // Find refresh token
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByToken(refreshTokenStr);

        if (tokenOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid refresh token"));
        }

        RefreshToken refreshToken = tokenOpt.get();

        // Validate token
        if (!refreshToken.isValid()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Refresh token is expired or revoked"));
        }

        User user = refreshToken.getUser();

        // Generate new tokens
        String newAccessToken = jwtUtil.generateToken(user.getEmail(), user.getRoles());
        String newRefreshToken = generateRefreshToken(user);

        // Revoke old refresh token
        refreshToken.revoke();
        refreshToken.setReplacedByToken(newRefreshToken);
        refreshTokenRepository.save(refreshToken);

        log.info("Token refreshed for user: {}", user.getEmail());

        return ResponseEntity.ok(Map.of(
                "accessToken", newAccessToken,
                "refreshToken", newRefreshToken,
                "expiresIn", jwtUtil.getExpirationTime()
        ));
    }

    /**
     * Logout - revoke refresh token
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, String> request) {
        String refreshTokenStr = request.get("refreshToken");

        if (refreshTokenStr != null && !refreshTokenStr.isEmpty()) {
            refreshTokenRepository.findByToken(refreshTokenStr).ifPresent(token -> {
                token.revoke();
                refreshTokenRepository.save(token);
                log.info("Refresh token revoked for user: {}", token.getUser().getEmail());
            });
        }

        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    /**
     * Get current user info
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Not authenticated"));
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(buildUserDto(user));
    }

    /**
     * Change password
     */
    @PutMapping("/password")
    public ResponseEntity<?> changePassword(
            Authentication authentication,
            @RequestBody Map<String, String> request) {

        String email = authentication.getName();
        String currentPassword = request.get("currentPassword");
        String newPassword = request.get("newPassword");

        if (currentPassword == null || newPassword == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Current and new password are required"));
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Current password is incorrect"));
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Revoke all refresh tokens for security
        refreshTokenRepository.revokeAllUserTokens(user, LocalDateTime.now());

        log.info("Password changed for user: {}", email);

        return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
    }

    // ==================== Helper Methods ====================

    /**
     * Generate refresh token and save to database
     */
    private String generateRefreshToken(User user) {
        String tokenValue = UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(tokenValue)
                .expiresAt(LocalDateTime.now().plusDays(REFRESH_TOKEN_VALIDITY_DAYS))
                .build();

        refreshTokenRepository.save(refreshToken);

        return tokenValue;
    }

    /**
     * Build user DTO from entity
     */
    private Map<String, Object> buildUserDto(User user) {
        Map<String, Object> userDto = new HashMap<>();
        userDto.put("userId", user.getUserId());
        userDto.put("email", user.getEmail());
        userDto.put("firstName", user.getFirstName());
        userDto.put("lastName", user.getLastName());
        userDto.put("roles", user.getRoles().stream()
                .map(Role::getAuthority)
                .collect(Collectors.toList()));
        userDto.put("isEmailVerified", user.isEmailVerified());
        userDto.put("createdDate", user.getCreatedDate());
        return userDto;
    }
}
