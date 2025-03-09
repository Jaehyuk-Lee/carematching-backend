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
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new TokenAuthException("인증 토큰이 필요합니다.");
        }

        String token = authHeader.substring(7);
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
}
