package com.sesac.carematching.transaction.exception;

public class IllegalTransactionStateException extends RuntimeException {
    public IllegalTransactionStateException(String message) {
        super(message);
    }
}
