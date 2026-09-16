package com.netiot.service.security.exception;

public abstract class JwtAuthException extends RuntimeException {
    protected JwtAuthException(String message) {
        super(message);
    }
    protected JwtAuthException(String message, Throwable cause) {
        super(message, cause);
    }
}
