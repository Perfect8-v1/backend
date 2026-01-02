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

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        log.info("AUTH: Inloggningsförsök för e-post: {}", request.getEmail());

        try {
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Användaren hittades inte"));

            // Tvätta strängarna från dolda tecken/citattecken som kan komma från testverktyg eller JSON-fel
            String providedHash = request.getPasswordHash() != null ?
                    request.getPasswordHash().trim().replace("\"", "").replace("'", "") : "";
            String storedHash = user.getPasswordHash() != null ?
                    user.getPasswordHash().trim().replace("\"", "").replace("'", "") : "";

            if (storedHash.equals(providedHash)) {
                log.info("AUTH: Hash-matchning lyckades för {}", user.getEmail());

                // Uppdatera användarstatus
                user.setFailedLoginAttempts(0);
                user.setLastLoginDate(LocalDateTime.now());
                userRepository.save(user);

                // Generera JWT-token
                String token = jwtUtil.generateToken(user.getUserId(), user.getEmail(), user.getRoles());

                // Förbered användarinformation för respons
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("userId", user.getUserId());
                userInfo.put("email", user.getEmail());
                userInfo.put("roles", user.getRoles());

                // Returnera enligt LoginResponse DTO-struktur:
                // 1. accessToken, 2. refreshToken, 3. tokenType, 4. expiresIn, 5. user
                return ResponseEntity.ok(new LoginResponse(
                        token,
                        UUID.randomUUID().toString(), // Generera ett faktiskt unikt refresh-token
                        "Bearer",
                        3600L,
                        userInfo
                ));
            } else {
                log.warn("AUTH: Ogiltiga inloggningsuppgifter för {}", user.getEmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Ogiltiga inloggningsuppgifter"));
            }
        } catch (Exception e) {
            log.error("AUTH: Systemfel vid inloggning: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Inloggningen misslyckades på grund av ett serverfel"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody LoginRequest request) {
        log.info("AUTH: Registrerar ny användare: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "E-postadressen är redan registrerad"));
        }

        User user = new User();
        user.setEmail(request.getEmail());
        // Tvätta hashen även vid registrering
        user.setPasswordHash(request.getPasswordHash().trim().replace("\"", "").replace("'", ""));
        user.setActive(true);
        user.setCreatedDate(LocalDateTime.now());

        // Sätt standardroll (Använder valueOf för att undvika kompileringsstrul mellan moduler)
        user.setRoles(Set.of(Role.valueOf("USER")));

        userRepository.save(user);
        log.info("AUTH: Användare {} skapad.", user.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Användaren har registrerats framgångsrikt"));
    }
}