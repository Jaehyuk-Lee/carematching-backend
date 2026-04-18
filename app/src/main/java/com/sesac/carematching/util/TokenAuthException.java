package com.sesac.carematching.util;

/**
 * 토큰 인증 실패 시 발생하는 예외
 */
public class TokenAuthException extends RuntimeException {
    public TokenAuthException(String message) {
        super(message);
    }
}
