package com.sesac.carematching.util.fallback;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class FallbackExceptionHandler {
    /**
     * ApiFallbackException 처리
     * 메서드에 붙인 @FallbackMessage(code, message)에서 정의한 code로 응답 (기본값 503)
     */
    @ExceptionHandler(ApiFallbackException.class)
    public ResponseEntity<?> handleApiFallbackException(ApiFallbackException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.valueOf(ex.getCode())).body(errorResponse);
    }

}
