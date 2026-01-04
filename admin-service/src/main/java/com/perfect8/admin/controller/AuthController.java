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
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    /**
     * MAGNUM OPUS: Hämta salt för klient-side hashing.
     * Detta är endpointen som saknades och orsakade 403/404.
     */
    @GetMapping("/salt")
    public ResponseEntity<?> getSalt(@RequestParam String email, @RequestParam(required = false) boolean forRegistration) {
        log.info("SALT: Begäran om salt för {}", email);

        if (forRegistration) {
            // Vid registrering genererar vi ett nytt salt (BCrypt format)
            // Här returnerar vi en hårdkodad "genSalt"-liknande sträng eller genererar en
            // För enkelhetens skull i MVP kan vi låta klienten generera saltet vid reg,
            // men för login MÅSTE vi hämta det från DB.
            return ResponseEntity.ok(Map.of("salt", org.springframework.security.crypto.bcrypt.BCrypt.gensalt()));
        }

        return userRepository.findByEmail(email)
                .map(user -> {
                    String hash = user.getPasswordHash();
                    if (hash != null && hash.length() >= 29) {
                        // BCrypt-salt är de första 29 tecknen av hashen ($2a$10$...)
                        String salt = hash.substring(0, 29);
                        return ResponseEntity.ok(Map.of("salt", salt));
                    }
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of("error", "Ogiltigt hash-format i databasen"));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Användaren hittades inte")));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        log.info("LOGIN: Inloggningsförsök för: {}", request.getEmail());

        try {
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Tvätta strängarna
            String providedHash = request.getPasswordHash() != null ?
                    request.getPasswordHash().trim().replace("\"", "").replace("'", "") : "";
            String storedHash = user.getPasswordHash() != null ?
                    user.getPasswordHash().trim().replace("\"", "").replace("'", "") : "";

            if (storedHash.equals(providedHash)) {
                log.info("LOGIN: Match för {}, genererar token.", user.getEmail());

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
                log.error("LOGIN: Felaktigt lösenord för: {}", user.getEmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
            }
        } catch (Exception e) {
            log.error("LOGIN: Fel vid inloggning: {}", e.getMessage());
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
        user.setPasswordHash(request.getPasswordHash().trim().replace("\"", "").replace("'", ""));
        user.setActive(true);
        user.setCreatedDate(LocalDateTime.now());
        user.setRoles(Set.of(Role.valueOf("USER")));

        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "User registered successfully"));
    }
}