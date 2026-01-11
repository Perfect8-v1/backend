package com.perfect8.admin.service;

import com.perfect8.admin.model.User;
import com.perfect8.admin.repository.UserRepository;
import com.perfect8.admin.dto.LoginRequest;
import com.perfect8.admin.dto.LoginResponse;
import com.perfect8.admin.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class AuthService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Login - SOP-branchen (Plaintext -> BCrypt match)
     * Industristandard där backend sköter hashning.
     */
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Användaren hittades inte"));

        // passwordEncoder.matches() jämför inkommande klartext mot hashen i DB
        if (passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.info("SOP LOGIN: Framgångsrik inloggning för {}", user.getEmail());

            // Uppdatera inloggningsstatistik och nollställ felaktiga försök
            user.setLastLoginDate(LocalDateTime.now());
            user.setFailedLoginAttempts(0);
            userRepository.save(user);

            // Generera JWT-token
            String token = jwtUtil.generateToken(user.getUserId(), user.getEmail(), user.getRoles());

            // Bygg userInfo-mappen
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", user.getUserId());
            userInfo.put("email", user.getEmail());
            userInfo.put("roles", user.getRoles());

            return new LoginResponse(
                    token,
                    UUID.randomUUID().toString(),
                    "Bearer",
                    3600L,
                    userInfo
            );
        }

        log.warn("SOP LOGIN: Felaktigt lösenord för {}", request.getEmail());
        throw new RuntimeException("Felaktiga inloggningsuppgifter");
    }
}
