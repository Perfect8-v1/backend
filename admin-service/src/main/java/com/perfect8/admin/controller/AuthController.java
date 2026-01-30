package com.perfect8.admin.controller;

import com.perfect8.admin.dto.LoginRequest;
import com.perfect8.admin.dto.LoginResponse;
import com.perfect8.admin.model.RefreshToken;
import com.perfect8.admin.model.User;
import com.perfect8.admin.repository.RefreshTokenRepository;
import com.perfect8.admin.repository.UserRepository;
import com.perfect8.admin.util.JwtUtil;
import com.perfect8.common.enums.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshTokenExpiration; // 7 dagar default

    /**
     * LOGIN: Industristandard (plaintext over HTTPS)
     */
    @PostMapping("/login")
    @Transactional
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        log.info("SOP LOGIN: Inloggningsförsök för: {}", request.getEmail());

        try {
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Användaren hittades inte"));

            if (passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                log.info("SOP LOGIN: Match för {}, genererar token.", user.getEmail());

                // Uppdatera metadata
                user.setFailedLoginAttempts(0);
                user.setLastLoginDate(LocalDateTime.now());
                userRepository.save(user);

                // Generera access token
                String accessToken = jwtUtil.generateToken(user.getUserId(), user.getEmail(), user.getRoles());

                // Skapa och spara refresh token i databasen
                RefreshToken refreshToken = createRefreshToken(user, httpRequest);

                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("userId", user.getUserId());
                userInfo.put("email", user.getEmail());
                userInfo.put("roles", user.getRoles());

                return ResponseEntity.ok(new LoginResponse(
                        accessToken,
                        refreshToken.getToken(),
                        "Bearer",
                        3600L,
                        userInfo
                ));
            } else {
                log.warn("SOP LOGIN: Felaktigt lösenord för: {}", user.getEmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
            }
        } catch (Exception e) {
            log.error("SOP LOGIN: Fel vid inloggning: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Login failed"));
        }
    }

    /**
     * REFRESH: Förnya access token med refresh token
     */
    @PostMapping("/refresh")
    @Transactional
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        String refreshTokenString = request.get("refreshToken");
        log.info("SOP REFRESH: Försöker förnya token");

        if (refreshTokenString == null || refreshTokenString.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Refresh token required"));
        }

        try {
            RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenString)
                    .orElseThrow(() -> new RuntimeException("Refresh token not found"));

            if (!refreshToken.isValid()) {
                log.warn("SOP REFRESH: Ogiltig eller utgången refresh token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid or expired refresh token"));
            }

            // Nu fungerar getUser() tack vare @Transactional
            User user = refreshToken.getUser();

            // Generera ny access token
            String newAccessToken = jwtUtil.generateToken(user.getUserId(), user.getEmail(), user.getRoles());

            // Rotera refresh token (skapa ny, revoke gammal)
            refreshToken.revoke();
            RefreshToken newRefreshToken = createRefreshToken(user, httpRequest);
            refreshToken.setReplacedByToken(newRefreshToken.getToken());
            refreshTokenRepository.save(refreshToken);

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", user.getUserId());
            userInfo.put("email", user.getEmail());
            userInfo.put("roles", user.getRoles());

            log.info("SOP REFRESH: Token förnyad för: {}", user.getEmail());

            return ResponseEntity.ok(new LoginResponse(
                    newAccessToken,
                    newRefreshToken.getToken(),
                    "Bearer",
                    3600L,
                    userInfo
            ));
        } catch (Exception e) {
            log.error("SOP REFRESH: Fel vid token-förnyelse: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Token refresh failed"));
        }
    }

    /**
     * LOGOUT: Revoke refresh token
     */
    @PostMapping("/logout")
    @Transactional
    public ResponseEntity<?> logout(@RequestBody Map<String, String> request) {
        String refreshTokenString = request.get("refreshToken");
        log.info("SOP LOGOUT: Loggar ut användare");

        if (refreshTokenString != null && !refreshTokenString.isBlank()) {
            refreshTokenRepository.findByToken(refreshTokenString).ifPresent(token -> {
                token.revoke();
                refreshTokenRepository.save(token);
                log.info("SOP LOGOUT: Refresh token revoked");
            });
        }

        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    /**
     * REGISTER: Haschar lösenordet direkt i backend
     */
    @PostMapping("/register")
    @Transactional
    public ResponseEntity<?> register(@Valid @RequestBody LoginRequest request) {
        log.info("SOP REGISTER: Registrerar ny användare: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Email already registered"));
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setActive(true);
        user.setRoles(Set.of(Role.USER));

        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "User registered successfully"));
    }

    /**
     * Hjälpmetod: Skapar och sparar RefreshToken
     */
    private RefreshToken createRefreshToken(User user, HttpServletRequest httpRequest) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000))
                .ipAddress(getClientIp(httpRequest))
                .deviceInfo(httpRequest.getHeader("User-Agent"))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Hjälpmetod: Hämta klientens IP-adress
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
