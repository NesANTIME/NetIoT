package com.netiot.service.security.exception;

public class InvalidTokenException extends JwtAuthException {
    public InvalidTokenException(String message) {
        super(message);
    }
    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}