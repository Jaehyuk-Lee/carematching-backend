package com.sesac.carematching.exception;

public class VersionException extends RuntimeException {
    private static final String MESSAGE = "클라이언트 버전이 낮습니다. 페이지를 새로고침 해주세요.";
    public VersionException() {
        super(MESSAGE);
    }
}
