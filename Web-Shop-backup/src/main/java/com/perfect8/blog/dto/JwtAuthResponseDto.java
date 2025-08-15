package main.java.com.perfect8.blog.dto;

public class JwtAuthResponseDto {
    private String accessToken;
    private String tokenType = "Bearer";

    // Constructor
    public JwtAuthResponseDto(String accessToken) {
        this.accessToken = accessToken;
    }

    // Generate Getters and Setters for accessToken and tokenType
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
}