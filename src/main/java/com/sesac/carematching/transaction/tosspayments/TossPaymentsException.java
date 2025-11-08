package com.sesac.carematching.transaction.tosspayments;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

// 토스페이먼츠 API 에러 코드 문서: https://docs.tosspayments.com/reference/error-codes#%EA%B2%B0%EC%A0%9C-%EC%8A%B9%EC%9D%B8
@Getter
public class TossPaymentsException extends RuntimeException {
    private final String code;
    private final String message;

    public TossPaymentsException(@JsonProperty("code") String code,
                                 @JsonProperty("message") String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

}
