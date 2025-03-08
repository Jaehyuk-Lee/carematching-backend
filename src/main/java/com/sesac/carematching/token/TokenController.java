package com.sesac.carematching.token;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/token")
@RequiredArgsConstructor
public class TokenController {
    private final TokenService tokenService;

    @PostMapping("/reissue")
    public ResponseEntity<TokenResponse> reissue(@RequestHeader("Refresh-Token") String refreshToken) {
        return ResponseEntity.ok(tokenService.reissue(refreshToken));
    }

    @PostMapping("/remove")
    public ResponseEntity<?> remove(@RequestHeader("Refresh-Token") String refreshToken) {
        tokenService.removeRefreshToken(refreshToken);
        return ResponseEntity.ok().build();
    }
}
