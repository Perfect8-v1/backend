package com.perfect8.admin.service;

import com.perfect8.admin.dto.AuthRequest;
import com.perfect8.admin.dto.AuthResponse;
import com.perfect8.admin.dto.RegisterRequest;
import com.perfect8.admin.model.RefreshToken;
import com.perfect8.admin.model.User;
import com.perfect8.admin.repository.RefreshTokenRepository;
import com.perfect8.admin.repository.UserRepository;
import com.perfect8.admin.util.JwtUtil;
import com.perfect8.common.enums.Role;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Central Authentication Service for Perfect8
 * Handles all user authentication across services
 * 
 * Created: 2025-11-20
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Value("${jwt.refresh-expiration:604800000}") // Default 7 days
    private long refreshTokenExpirationMs;

    @Value("${security.max-failed-attempts:5}")
    private int maxFailedAttempts;

    @Value("${security.lock-duration-minutes:30}")
    private int lockDurationMinutes;

    /**
     * Authenticate user and generate tokens
     */
    public AuthResponse login(AuthRequest request, HttpServletRequest httpRequest) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        // Check if account is locked
        if (user.isLocked()) {
            throw new LockedException("Account is locked until " + user.getAccountLockedUntil());
        }

        // Check if account is active
        if (!user.isActive()) {
            throw new BadCredentialsException("Account is deactivated");
        }

        try {
            // Authenticate
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            // Reset failed attempts on successful login
            user.setFailedLoginAttempts(0);
            user.setAccountLockedUntil(null);
            user.setLastLoginDate(LocalDateTime.now());
            userRepository.save(user);

            // Generate tokens
            String accessToken = jwtUtil.generateToken(user.getUserId(), user.getEmail(), user.getRoles());
            String refreshToken = createRefreshToken(user, httpRequest);

            log.info("User {} successfully logged in", user.getEmail());

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtUtil.getExpirationTime())
                    .userId(user.getUserId())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .roles(user.getRoles().stream().map(Role::name).toList())
                    .build();

        } catch (BadCredentialsException e) {
            // Increment failed attempts
            handleFailedLogin(user);
            throw new BadCredentialsException("Invalid email or password");
        }
    }

    /**
     * Register new user
     */
    public AuthResponse register(RegisterRequest request, HttpServletRequest httpRequest) {
        // Check if email exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // Determine role
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

        // passwordHash is already hashed by frontend
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(request.getPasswordHash())
                .passwordSalt(request.getPasswordSalt())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .roles(roles)
                .isActive(true)
                .isEmailVerified(false)
                .emailVerificationToken(UUID.randomUUID().toString())
                .build();

        user = userRepository.save(user);
        log.info("New user registered: {}", user.getEmail());

        // Generate tokens
        String accessToken = jwtUtil.generateToken(user.getUserId(), user.getEmail(), user.getRoles());
        String refreshToken = createRefreshToken(user, httpRequest);

        // TODO: Send verification email

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirationTime())
                .userId(user.getUserId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(user.getRoles().stream().map(Role::name).toList())
                .build();
    }

    /**
     * Refresh access token using refresh token
     */
    public AuthResponse refreshToken(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (!refreshToken.isValid()) {
            throw new RuntimeException("Refresh token is expired or revoked");
        }

        User user = refreshToken.getUser();

        // Generate new access token
        String newAccessToken = jwtUtil.generateToken(user.getUserId(), user.getEmail(), user.getRoles());

        log.info("Access token refreshed for user: {}", user.getEmail());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshTokenValue) // Return same refresh token
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirationTime())
                .userId(user.getUserId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(user.getRoles().stream().map(Role::name).toList())
                .build();
    }

    /**
     * Logout user - revoke refresh token
     */
    public void logout(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElse(null);

        if (refreshToken != null) {
            refreshToken.revoke();
            refreshTokenRepository.save(refreshToken);
            log.info("User {} logged out", refreshToken.getUser().getEmail());
        }
    }

    /**
     * Logout from all devices - revoke all refresh tokens
     */
    public void logoutAll(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        refreshTokenRepository.revokeAllUserTokens(user, LocalDateTime.now());
        log.info("All sessions revoked for user: {}", user.getEmail());
    }

    /**
     * Verify email with token
     */
    public void verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid verification token"));

        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        userRepository.save(user);

        log.info("Email verified for user: {}", user.getEmail());
    }

    /**
     * Request password reset
     */
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElse(null);

        // Don't reveal if email exists
        if (user == null) {
            log.warn("Password reset requested for non-existent email: {}", email);
            return;
        }

        String resetToken = UUID.randomUUID().toString();
        user.setResetPasswordToken(resetToken);
        user.setResetPasswordTokenExpiry(LocalDateTime.now().plusHours(24));
        userRepository.save(user);

        // TODO: Send password reset email

        log.info("Password reset requested for user: {}", email);
    }

    /**
     * Reset password with token
     */
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetPasswordToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid reset token"));

        if (user.getResetPasswordTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset token has expired");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiry(null);
        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);
        userRepository.save(user);

        // Revoke all existing refresh tokens for security
        refreshTokenRepository.revokeAllUserTokens(user, LocalDateTime.now());

        log.info("Password reset completed for user: {}", user.getEmail());
    }

    /**
     * Change password (when logged in)
     */
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new BadCredentialsException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Password changed for user: {}", user.getEmail());
    }

    // --- Private helper methods ---

    private String createRefreshToken(User user, HttpServletRequest request) {
        String tokenValue = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(tokenValue)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpirationMs / 1000))
                .deviceInfo(request != null ? request.getHeader("User-Agent") : null)
                .ipAddress(request != null ? getClientIp(request) : null)
                .build();

        refreshTokenRepository.save(refreshToken);

        return tokenValue;
    }

    private void handleFailedLogin(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);

        if (attempts >= maxFailedAttempts) {
            user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(lockDurationMinutes));
            log.warn("Account locked for user {} after {} failed attempts", user.getEmail(), attempts);
        }

        userRepository.save(user);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
