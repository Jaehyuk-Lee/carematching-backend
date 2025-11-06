package com.sesac.carematching.transaction.tosspayments;

// 토스페이먼츠 API 에러 코드 문서: https://docs.tosspayments.com/reference/error-codes#%EA%B2%B0%EC%A0%9C-%EC%8A%B9%EC%9D%B8
public class TossPaymentsException extends RuntimeException {
    private final String code;
    private final String message;

    public TossPaymentsException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
