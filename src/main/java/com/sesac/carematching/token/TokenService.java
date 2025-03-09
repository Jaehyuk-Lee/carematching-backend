package com.sesac.carematching.token;

import com.sesac.carematching.config.JwtUtil;
import com.sesac.carematching.user.User;
import com.sesac.carematching.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Transactional
    public TokenResponse reissue(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new RuntimeException("Refresh Token이 유효하지 않습니다.");
        }

        String username = jwtUtil.extractUsername(refreshToken);
        if (username == null) {
            throw new RuntimeException("토큰에서 사용자 정보를 찾을 수 없습니다.");
        }

        RefreshToken savedRefreshToken = refreshTokenRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("로그아웃된 사용자입니다."));

        if (!savedRefreshToken.getToken().equals(refreshToken)) {
            throw new RuntimeException("토큰의 유저 정보가 일치하지 않습니다.");
        }

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        String newAccessToken = jwtUtil.generateAccessToken(username, user.getRole().getRname());
        String newRefreshToken = jwtUtil.generateRefreshToken(username);

        savedRefreshToken.setToken(newRefreshToken);
        refreshTokenRepository.save(savedRefreshToken);

        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void saveRefreshToken(String username, String refreshToken) {
        RefreshToken token = new RefreshToken(username, refreshToken);
        refreshTokenRepository.save(token);
    }

    @Transactional
    public void removeRefreshToken(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken).ifPresent(refreshTokenRepository::delete);
    }
}
