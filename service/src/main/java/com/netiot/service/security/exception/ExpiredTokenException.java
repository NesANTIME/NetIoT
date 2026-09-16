package com.netiot.service.security.exception;

public class ExpiredTokenException extends JwtAuthException {
    public ExpiredTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}