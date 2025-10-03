package com.perfect8.admin.controller;

import com.perfect8.admin.dto.AuthRequest;
import com.perfect8.admin.dto.AuthResponse;
import com.perfect8.admin.model.AdminUser;
import com.perfect8.admin.service.AdminUserService;
import com.perfect8.admin.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest authRequest) {
        try {
            // Authenticate user
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getUsername(),
                            authRequest.getPassword()
                    )
            );

            // Load user details
            UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getUsername());

            // Generate JWT token
            String token = jwtUtil.generateToken(userDetails);

            // Update last login time
            AdminUser adminUser = adminUserService.findByUsername(authRequest.getUsername());
            adminUser.setLastLogin(LocalDateTime.now());
            adminUserService.save(adminUser);

            // Create response using your existing AuthResponse DTO
            AuthResponse authResponse = new AuthResponse();
            authResponse.setToken(token);
            authResponse.setUsername(adminUser.getUsername());
            authResponse.setRole(adminUser.getRole().name());
            // Set other fields if they exist in your AuthResponse

            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("success", true);
            responseMap.put("data", authResponse);
            responseMap.put("message", "Login successful");

            return ResponseEntity.ok(responseMap);

        } catch (BadCredentialsException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", Map.of(
                    "code", "INVALID_CREDENTIALS",
                    "message", "Invalid username or password"
            ));
            return ResponseEntity.status(401).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", Map.of(
                    "code", "LOGIN_ERROR",
                    "message", "An error occurred during login"
            ));
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // In a stateless JWT setup, logout is typically handled client-side
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Logout successful");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        // This endpoint requires authentication
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "User info retrieved successfully");
        return ResponseEntity.ok(response);
    }
}