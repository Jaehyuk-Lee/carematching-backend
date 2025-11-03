package com.sesac.carematching.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    private final Key key;
    private final long accessExpirationTime;
    private final long refreshExpirationTime;

    public JwtUtil(
        @Value("${jwt.secret}") String secretKey,
        @Value("${jwt.access-expiration-time}") long accessExpirationTime,
        @Value("${jwt.refresh-expiration-time}") long  refreshExpirationTime
    ) {
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessExpirationTime = accessExpirationTime;
        this.refreshExpirationTime = refreshExpirationTime;
    }

    private String createToken(Map<String, Object> claims, String subject, long expiration) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

    public String generateAccessToken(String username, String role, Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null.");
        }
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("username", username);
        claims.put("userId", userId);
        return createToken(claims, userId.toString(), accessExpirationTime);
    }

    public String generateRefreshToken(String username, Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null.");
        }
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        claims.put("userId", userId);
        return createToken(claims, userId.toString(), refreshExpirationTime);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String extractUsername(String token) {
        Object val = extractClaims(token).get("username");
        if (val == null) return null;
        return val.toString();
    }

    public Integer extractUserId(String token) {
        String val = extractClaims(token).getSubject();
        if (val == null) return null;
        try {
            return Integer.valueOf(val);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
}
