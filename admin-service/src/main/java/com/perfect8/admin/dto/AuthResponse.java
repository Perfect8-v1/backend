package com.perfect8.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Authentication Response DTO
 * Returned after successful login/register/refresh
 * 
 * Created: 2025-11-20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    
    private String refreshToken;
    
    @Builder.Default
    private String tokenType = "Bearer";
    
    private long expiresIn;
    
    private Long userId;
    
    private String email;
    
    private String firstName;
    
    private String lastName;
    
    private List<String> roles;
}
