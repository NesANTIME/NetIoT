package com.netiot.service.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.netiot.service.security.exception.ExpiredTokenException;
import com.netiot.service.security.exception.InvalidSignatureException;
import com.netiot.service.security.exception.InvalidTokenException;
import com.netiot.service.security.exception.MalformedTokenException;
import com.netiot.service.security.exception.UnsupportedTokenException;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.security.WeakKeyException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String tokenSecret;

    @Value("${jwt.expiration}")
    private long tokenExpiration;

    private SecretKey signingKey;


    @PostConstruct
    private void init() {
        if (!StringUtils.hasText(tokenSecret)) {
            throw new IllegalStateException("jwt.secret no está configurado");
        }
        try {
            this.signingKey = Keys.hmacShaKeyFor(tokenSecret.getBytes(StandardCharsets.UTF_8));
        } catch (WeakKeyException e) {
            throw new IllegalStateException(
                    "jwt.secret es demasiado corto para el algoritmo HMAC utilizado", e);
        }
        if (tokenExpiration <= 0) {
            throw new IllegalStateException("jwt.expiration debe ser un valor positivo (ms)");
        }
    }
    
    private SecretKey key() {
        return signingKey;
    }


    public String generateToken(String email) {
        Date now = new Date();
        return Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + tokenExpiration))
                .signWith(key())
                .compact();
    }

    public String getEmail(String token) {
        if (!StringUtils.hasText(token)) {
            throw new InvalidTokenException("El token provisto es nulo o está vacío");
        }
        try {
            return Jwts.parser()
                    .verifyWith(key())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (ExpiredJwtException e) {
            log.warn("Intento de acceso con JWT expirado (sub={})", safeSubject(e));
            throw new ExpiredTokenException("El token ha expirado", e);
        } catch (MalformedJwtException e) {
            log.warn("Estructura de JWT inválida: {}", e.getMessage());
            throw new MalformedTokenException("El token está malformado", e);
        } catch (SignatureException e) {
            // Posible intento de manipulación - vale la pena vigilar frecuencia/origen
            log.warn("Firma de JWT no coincide, posible token manipulado");
            throw new InvalidSignatureException("La firma del token no es válida", e);
        } catch (UnsupportedJwtException e) {
            log.warn("Formato JWT no soportado: {}", e.getMessage());
            throw new UnsupportedTokenException("El formato del token no es soportado", e);
        } catch (IllegalArgumentException e) {
            log.warn("Argumento inválido al procesar JWT: {}", e.getMessage());
            throw new InvalidTokenException("Parámetro de token no válido", e);
        } catch (JwtException e) {
            log.error("Error no especificado procesando JWT", e);
            throw new InvalidTokenException("Error al procesar la autenticación", e);
        }
    }

    public boolean validate(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }
        try {
            Jwts.parser().verifyWith(key()).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Token inválido durante validación: {}", e.getMessage());
            return false;
        }
    }

    private String safeSubject(ExpiredJwtException e) {
        try {
            return e.getClaims().getSubject();
        } catch (Exception ignored) {
            return "desconocido";
        }
    }
}