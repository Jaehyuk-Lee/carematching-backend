package com.sesac.carematching.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sesac.carematching.token.TokenResponse;
import com.sesac.carematching.token.TokenService;
import com.sesac.carematching.user.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final TokenService tokenService;

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) throws IOException {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // 사용자의 role 정보 가져오기
        String role = userDetails.getAuthorities().stream()
            .findFirst()
            .map(GrantedAuthority::getAuthority)
            .orElse("ROLE_USER");

        String accessToken = jwtUtil.generateAccessToken(userDetails.getUsername(), role, userDetails.getUserId());
        String refreshToken = jwtUtil.generateRefreshToken(userDetails.getUsername(), userDetails.getUserId());

        tokenService.saveRefreshToken(userDetails.getUsername(), refreshToken);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // 토큰 응답
        TokenResponse tokenResponse = new TokenResponse(accessToken, refreshToken);
        String jsonResponse = objectMapper.writeValueAsString(tokenResponse);
        response.getWriter().write(jsonResponse);
    }
}
