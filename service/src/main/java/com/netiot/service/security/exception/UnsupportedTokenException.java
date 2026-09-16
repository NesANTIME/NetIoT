package com.netiot.service.security.exception;

public class UnsupportedTokenException extends JwtAuthException {
    public UnsupportedTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}