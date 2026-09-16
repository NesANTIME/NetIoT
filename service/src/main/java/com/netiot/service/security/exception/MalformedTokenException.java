package com.netiot.service.security.exception;

public class MalformedTokenException extends JwtAuthException {
    public MalformedTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}