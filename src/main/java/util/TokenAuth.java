package util;

import com.sesac.carematching.config.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TokenAuth {
    private final JwtUtil jwtUtil;

    public String extractUsernameFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("인증 토큰이 필요합니다.");
        }

        String token = authHeader.substring(7);
        String username = jwtUtil.extractUsername(token);

        if (username == null) {
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }

        return username;
    }
}
