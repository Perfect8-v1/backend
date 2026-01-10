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
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder; // Nu använder vi Spring Securitys standard

    /**
     * LOGIN: Industristandard (klartext över HTTPS)
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        log.info("SOP LOGIN: Inloggningsförsök för: {}", request.getEmail());

        try {
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Användaren hittades inte"));

            // passwordEncoder.matches() jämför det inkommande lösenordet mot hashen i DB
            // Det spelar ingen roll om lösenordet är "magnus123" eller "jonathan123"
            if (passwordEncoder.matches(request.getPasswordHash(), user.getPasswordHash())) {
                log.info("SOP LOGIN: Match för {}, genererar token.", user.getEmail());

                // Uppdatera metadata
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
                        UUID.randomUUID().toString(),
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
     * REGISTER: Haschar lösenordet direkt i backend
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody LoginRequest request) {
        log.info("SOP REGISTER: Registrerar ny användare: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Email already registered"));
        }

        User user = new User();
        user.setEmail(request.getEmail());

        // Här haschar vi lösenordet innan det sparas i databasen
        user.setPasswordHash(passwordEncoder.encode(request.getPasswordHash()));

        user.setActive(true);
        // Datum sköts nu automatiskt av MySQL (via ditt nya script),
        // men vi kan sätta det här också om vi vill vara extra tydliga.
        user.setRoles(Set.of(Role.USER));

        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "User registered successfully"));
    }
}