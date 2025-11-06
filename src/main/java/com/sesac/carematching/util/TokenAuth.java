package com.sesac.carematching.util;

import com.sesac.carematching.config.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.ExpiredJwtException;

@RequiredArgsConstructor
@Component
public class TokenAuth {
    private final JwtUtil jwtUtil;

    public String extractUsernameFromToken(HttpServletRequest request) {
        String token = extractAuthHeader(request);
        String username;
        try {
            username = jwtUtil.extractUsername(token);
        } catch (ExpiredJwtException e) {
            throw new TokenAuthException("토큰이 만료되었습니다.");
        }

        if (username == null) {
            throw new TokenAuthException("유효하지 않은 토큰입니다.");
        }

        return username;
    }

    public Integer extractUserIdFromToken(HttpServletRequest request) {
        String token = extractAuthHeader(request);
        Integer userId;
        try {
            userId = jwtUtil.extractUserId(token);
        } catch (ExpiredJwtException e) {
            throw new TokenAuthException("토큰이 만료되었습니다.");
        }

        if (userId == null) {
            throw new TokenAuthException("유효하지 않은 토큰입니다.");
        }

        return userId;
    }

    private String extractAuthHeader(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new TokenAuthException("인증 토큰이 필요합니다.");
        }

        return authHeader.substring(7);
    }
}
