package io.life.user_service.dto.auth;

import java.time.Instant;

import io.life.user_service.dto.user.UserDto;

public class LoginResponse {

    private String token;
    private String tokenType;
    private Instant expiresAt;
    private UserDto user;

    public LoginResponse(String token, String tokenType, Instant expiresAt, UserDto user) {
        this.token = token;
        this.tokenType = tokenType;
        this.expiresAt = expiresAt;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public UserDto getUser() {
        return user;
    }
}
