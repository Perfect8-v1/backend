package com.perfect8.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String accessToken;
    
    private String refreshToken;
    
    private String tokenType;
    
    private long expiresIn;
    
    private Map<String, Object> user;
}
