package com.sesac.carematching.transaction.payment.provider.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

// 카카오페이 API 오류코드: https://developers.kakaopay.com/docs/getting-started/api-common-guide/error-code
@Getter
public class KakaoPayException extends RuntimeException {
    private final String code;
    private final String message;

    public KakaoPayException(@JsonProperty("error_code")  String code,
                             @JsonProperty("error_message") String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

}
