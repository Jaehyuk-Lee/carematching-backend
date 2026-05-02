package com.sesac.carematching.util.fallback;

import lombok.Getter;

@Getter
public class ApiFallbackException extends RuntimeException {
    // code는 HTTP 상태 코드에 대응합니다.
    // HTTP 결합도를 낮추기 위해 최종 응답시에만 HttpStatus 열거형을 직접 사용.
    int code;
    String message;
    public ApiFallbackException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}
