package com.sesac.carematching.transaction.tosspayments;

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
