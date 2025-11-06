package com.sesac.carematching.config;

import com.sesac.carematching.chat.RoomBuildException;
import com.sesac.carematching.exception.VersionException;
import com.sesac.carematching.transaction.tosspayments.TossPaymentsException;
import com.sesac.carematching.user.AdminAuthException;
import com.sesac.carematching.util.TokenAuthException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * TossPaymentsException 처리
     * HTTP 응답 코드는 TOSS_PAYMENTS의 다양한 에러 응답에 대응하기 전까지는 BAD_REQUEST로 통일
     */
    @ExceptionHandler(TossPaymentsException.class)
    public ResponseEntity<?> handleTossPaymentsException(TossPaymentsException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * 토큰 인증 실패 시 401 Unauthorized 응답 반환
     */
    @ExceptionHandler(TokenAuthException.class)
    public ResponseEntity<Map<String, String>> handleTokenAuthException(TokenAuthException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * 관리자 권한 요구 시 403 Forbidden 응답 반환
     */
    @ExceptionHandler(AdminAuthException.class)
    public ResponseEntity<Map<String, String>> handleAdminAuthException(AdminAuthException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * IllegalArgumentException 처리 (400 Bad Request)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * RoomBuildException 처리  (400 Bad Request)
     */
    @ExceptionHandler(RoomBuildException.class)
    public ResponseEntity<Map<String, String>> handleRoomBuildException(RoomBuildException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * API 버전 관련 예외 처리 (426 Upgrade Required)
     */
    @ExceptionHandler(VersionException.class)
    public ResponseEntity<Map<String, String>> handleVersionException(VersionException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.UPGRADE_REQUIRED).body(errorResponse);
    }
}
