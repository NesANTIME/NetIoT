package com.netiot.service.security.exception;

public class InvalidSignatureException extends JwtAuthException {
    public InvalidSignatureException(String message, Throwable cause) {
        super(message, cause);
    }
}