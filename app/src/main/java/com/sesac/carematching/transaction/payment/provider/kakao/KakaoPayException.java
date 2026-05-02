package com.sesac.carematching.transaction.payment.provider.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

// 카카오페이 API 오류코드: https://developers.kakaopay.com/docs/getting-started/api-common-guide/error-code
@Getter
public class KakaoPayException extends RuntimeException {
    private final String error_code;
    private final String error_message;
    private final String extras;

    public KakaoPayException(@JsonProperty("error_code") String error_code,
                             @JsonProperty("error_message") String error_message,
                             @JsonProperty("extras") String extras) {
        super(error_message);
        this.error_code = error_code;
        this.error_message = error_message;
        this.extras = extras;
    }

}
