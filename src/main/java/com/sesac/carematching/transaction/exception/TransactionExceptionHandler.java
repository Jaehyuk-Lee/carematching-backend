package com.sesac.carematching.transaction.exception;

import com.sesac.carematching.transaction.payment.provider.toss.TossPaymentsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class TransactionExceptionHandler {
    /**
     * ApiFallbackException 처리
     * HTTP 응답 코드는 ACCEPTED(202) - 추후 PG사 정상화되면 백엔드에서 알아서 재시도 해줄 예정
     */
    @ExceptionHandler(ApiFallbackException.class)
    public ResponseEntity<?> handleApiFallbackException(ApiFallbackException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(errorResponse);
    }

    /**
     * TossPaymentsException 처리
     * HTTP 응답 코드는 TOSS_PAYMENTS의 다양한 에러 응답에 대응하기 전까지는 BAD_REQUEST로 통일
     */
    @ExceptionHandler(TossPaymentsException.class)
    public ResponseEntity<?> handleTossPaymentsException(TossPaymentsException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("status", ex.getCode());
        errorResponse.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

}
