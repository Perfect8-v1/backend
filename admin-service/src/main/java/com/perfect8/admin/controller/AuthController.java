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
        log.info("MAGNUM OPUS DEBUG: Inloggningsförsök för: {}", request.getEmail());

        try {
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Tvätta strängarna från eventuella dolda mellanslag
            String providedHash = request.getPasswordHash() != null ? request.getPasswordHash().trim() : "";
            String storedHash = user.getPasswordHash() != null ? user.getPasswordHash().trim() : "";

            // DENNA LOGG GER OSS SVARET
            log.info("MAGNUM OPUS DEBUG: Jämför strängar...");
            log.info("MAGNUM OPUS DEBUG: MOTTAGEN: [{}] (Längd: {})", providedHash, providedHash.length());
            log.info("MAGNUM OPUS DEBUG: SPARAD:   [{}] (Längd: {})", storedHash, storedHash.length());

            if (storedHash.equals(providedHash)) {
                log.info("MAGNUM OPUS DEBUG: MATCH! Genererar token...");

                user.setFailedLoginAttempts(0);
                user.setLastLoginDate(LocalDateTime.now());
                userRepository.save(user);

                String token = jwtUtil.generateToken(user.getUserId(), user.getEmail(), user.getRoles());

                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("userId", user.getUserId());
                userInfo.put("email", user.getEmail());
                userInfo.put("roles", user.getRoles());

                return ResponseEntity.ok(new LoginResponse(token, "Bearer", user.getEmail(), 3600L, userInfo));
            } else {
                log.error("MAGNUM OPUS DEBUG: MISS! Hasharna är inte identiska.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
            }
        } catch (Exception e) {
            log.error("MAGNUM OPUS DEBUG: Fel: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Login failed"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody LoginRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(request.getPasswordHash().trim());
        user.setActive(true);
        user.setCreatedDate(LocalDateTime.now());
        user.setRoles(Set.of(Role.valueOf("USER"))); // Justerat för att matcha din Role.java (USER)
        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "User registered"));
    }
}