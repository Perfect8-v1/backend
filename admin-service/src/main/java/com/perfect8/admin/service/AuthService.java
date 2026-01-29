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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * AuthService v1.3 Unified
 * - Industristandard: password i plaintext över HTTPS, hashas i backend
 * - Stödjer både ADMIN och CUSTOMER roller
 * - Ingen passwordSalt (v1.2 legacy borttaget)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse login(AuthRequest request) {
        log.info("SOP LOGIN: Försöker logga in användare: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.error("SOP LOGIN: Användare ej hittad: {}", request.getEmail());
                    return new BadCredentialsException("Invalid email or password");
                });

        // v1.3: BCrypt jämför plaintext password mot lagrad hash
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.error("SOP LOGIN: Fel lösenord för: {}", request.getEmail());
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            userRepository.save(user);
            throw new BadCredentialsException("Invalid email or password");
        }

        if (!user.isActive()) {
            log.error("SOP LOGIN: Inaktiv användare: {}", request.getEmail());
            throw new BadCredentialsException("Account is inactive");
        }

        if (user.isLocked()) {
            log.error("SOP LOGIN: Låst användare: {}", request.getEmail());
            throw new BadCredentialsException("Account is locked");
        }

        // Uppdatera användarmetadata
        user.setLastLoginDate(LocalDateTime.now());
        user.setFailedLoginAttempts(0);
        userRepository.save(user);

        // Generera JWT access token
        String accessToken = jwtUtil.generateToken(user.getUserId(), user.getEmail(), user.getRoles());
        
        // Skapa refresh token
        String refreshTokenValue = UUID.randomUUID().toString();
        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        refreshTokenRepository.save(refreshToken);

        log.info("SOP LOGIN: Lyckad inloggning för: {} med roller: {}", user.getEmail(), user.getRoles());

        // Bygg flat AuthResponse
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .tokenType("Bearer")
                .expiresIn(3600)
                .userId(user.getUserId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(user.getRoles().stream()
                        .map(Role::name)
                        .collect(Collectors.toList()))
                .build();
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("SOP REGISTER: Försöker registrera användare: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            log.error("SOP REGISTER: Email redan registrerad: {}", request.getEmail());
            throw new IllegalArgumentException("Email already registered");
        }

        // v1.3: BCrypt-hasha plaintext password
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // Bestäm roll (default: USER om inget anges)
        Role userRole = Role.USER;
        if (request.getRole() != null && !request.getRole().isBlank()) {
            try {
                userRole = Role.valueOf(request.getRole().toUpperCase());
                log.info("SOP REGISTER: Använder angiven roll: {}", userRole);
            } catch (IllegalArgumentException e) {
                log.warn("SOP REGISTER: Ogiltig roll '{}', använder default USER", request.getRole());
            }
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(hashedPassword)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .roles(Set.of(userRole))
                .isActive(true)
                .isEmailVerified(false)
                .failedLoginAttempts(0)
                .build();

        userRepository.save(user);
        log.info("SOP REGISTER: Användare skapad: {} med roll: {}", user.getEmail(), userRole);

        // Skapa tokens direkt (auto-login efter registrering)
        String accessToken = jwtUtil.generateToken(user.getUserId(), user.getEmail(), user.getRoles());
        String refreshTokenValue = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .tokenType("Bearer")
                .expiresIn(3600)
                .userId(user.getUserId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(user.getRoles().stream()
                        .map(Role::name)
                        .collect(Collectors.toList()))
                .build();
    }

    @Transactional
    public AuthResponse refreshAccessToken(String refreshTokenValue) {
        log.info("SOP REFRESH: Försöker förnya token");

        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> {
                    log.error("SOP REFRESH: Token ej hittad");
                    return new BadCredentialsException("Invalid refresh token");
                });

        if (!refreshToken.isValid()) {
            log.error("SOP REFRESH: Token har gått ut eller är revoked");
            refreshTokenRepository.delete(refreshToken);
            throw new BadCredentialsException("Refresh token expired or revoked");
        }

        User user = refreshToken.getUser();
        
        // Generera ny access token
        String newAccessToken = jwtUtil.generateToken(user.getUserId(), user.getEmail(), user.getRoles());
        String newRefreshTokenValue = UUID.randomUUID().toString();

        // Revoke gammal token och skapa ny
        refreshToken.revoke();
        refreshToken.setReplacedByToken(newRefreshTokenValue);
        refreshTokenRepository.save(refreshToken);

        RefreshToken newRefreshToken = RefreshToken.builder()
                .token(newRefreshTokenValue)
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        refreshTokenRepository.save(newRefreshToken);

        log.info("SOP REFRESH: Token förnyad för användare: {}", user.getEmail());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshTokenValue)
                .tokenType("Bearer")
                .expiresIn(3600)
                .userId(user.getUserId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(user.getRoles().stream()
                        .map(Role::name)
                        .collect(Collectors.toList()))
                .build();
    }

    @Transactional
    public void logout(String refreshTokenValue) {
        log.info("SOP LOGOUT: Försöker logga ut");
        refreshTokenRepository.findByToken(refreshTokenValue)
                .ifPresent(token -> {
                    token.revoke();
                    refreshTokenRepository.save(token);
                    log.info("SOP LOGOUT: Token revoked för användare: {}", token.getUser().getEmail());
                });
    }
}
