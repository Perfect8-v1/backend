package com.perfect8.admin.controller;

import com.perfect8.admin.dto.LoginRequest;
import com.perfect8.admin.dto.LoginResponse;
import com.perfect8.admin.model.User;
import com.perfect8.admin.repository.UserRepository;
import com.perfect8.admin.util.JwtUtil;
import com.perfect8.common.enums.Role;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        log.info("DEBUG LOGIN: Inloggningsförsök för: {}", request.getEmail());

        try {
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String providedHash = request.getPasswordHash() != null ? request.getPasswordHash().trim() : "";
            String storedHash = user.getPasswordHash() != null ? user.getPasswordHash().trim() : "";

            // KRITISK LOGGNING FÖR ATT HITTA FELET
            log.info("DEBUG LOGIN: Jämför hashar...");
            log.info("DEBUG LOGIN: MOTTAGEN: [{}] (Längd: {})", providedHash, providedHash.length());
            log.info("DEBUG LOGIN: SPARAD:   [{}] (Längd: {})", storedHash, storedHash.length());

            if (storedHash.equals(providedHash)) {
                log.info("DEBUG LOGIN: Hash matchade för: {}", user.getEmail());

                user.setFailedLoginAttempts(0);
                user.setLastLoginDate(LocalDateTime.now());
                userRepository.save(user);

                String token = jwtUtil.generateToken(user.getUserId(), user.getEmail(), user.getRoles());

                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("userId", user.getUserId());
                userInfo.put("email", user.getEmail());
                userInfo.put("roles", user.getRoles());

                return ResponseEntity.ok(new LoginResponse(
                        token,
                        "Bearer",
                        user.getEmail(),
                        3600L,
                        userInfo
                ));
            } else {
                log.error("DEBUG LOGIN: Felaktigt lösenord för: {}", user.getEmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
            }
        } catch (Exception e) {
            log.error("DEBUG LOGIN: Fel: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Login failed"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody LoginRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Email already registered"));
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(request.getPasswordHash().trim());
        user.setActive(true);
        user.setCreatedDate(LocalDateTime.now());

        // Använder valueOf för att undvika kompileringsfel
        user.setRoles(Set.of(Role.valueOf("ROLE_USER")));

        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "User registered successfully"));
    }
}