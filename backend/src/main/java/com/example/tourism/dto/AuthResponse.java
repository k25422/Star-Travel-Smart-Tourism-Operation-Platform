package com.example.tourism.dto;

public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String username;
    private String nickname;
    private String role;

    public AuthResponse(String accessToken, String refreshToken, String username, String nickname, String role) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.username = username;
        this.nickname = nickname;
        this.role = role;
    }

    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public String getUsername() { return username; }
    public String getNickname() { return nickname; }
    public String getRole() { return role; }
}