package io.life.user_service.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.life.user_service.entity.User;
import io.life.user_service.entity.UserRole;

import javax.crypto.SecretKey;

@Component
public class JwtTokenProvider {

    private static final String ROLE_CLAIM = "role";
    private static final String WORKSTATION_ID_CLAIM = "workstationId";

    private final JwtProperties properties;
    private final SecretKey signingKey;

    public JwtTokenProvider(JwtProperties properties) {
        this.properties = properties;
        this.signingKey = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public JwtToken createToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(properties.getExpiration());

        String tokenValue = Jwts.builder()
            .setSubject(user.getUsername())
            .claim(ROLE_CLAIM, user.getRole().name())
            .claim(WORKSTATION_ID_CLAIM, user.getWorkstationId())
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(expiresAt))
            .signWith(signingKey, SignatureAlgorithm.HS256)
            .compact();

        return new JwtToken(tokenValue, expiresAt);
    }

    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return parseClaims(token).getBody().getSubject();
    }

    public UserRole extractRole(String token) {
        String role = parseClaims(token).getBody().get(ROLE_CLAIM, String.class);
        return role != null ? UserRole.valueOf(role) : null;
    }

    public Long extractWorkstationId(String token) {
        Object workstationId = parseClaims(token).getBody().get(WORKSTATION_ID_CLAIM);
        if (workstationId instanceof Number) {
            return ((Number) workstationId).longValue();
        }
        return null;
    }

    private Jws<Claims> parseClaims(String token) {
        return Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token);
    }

    public record JwtToken(String token, Instant expiresAt) {
    }
}
