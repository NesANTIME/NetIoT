package com.netiot.service.controller;


import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.netiot.service.dto.ChangePasswordRequestDTO;
import com.netiot.service.dto.LoginRequestDTO;
import com.netiot.service.dto.LoginResponseDTO;
import com.netiot.service.dto.MessageResponse;
import com.netiot.service.dto.RegisterRequestDTO;
import com.netiot.service.entity.User;
import com.netiot.service.repository.UserRepository;
import com.netiot.service.service.AuthService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String COOKIE_NAME = "jwt_token";

    private final AuthService authService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDTO req) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO req, HttpServletResponse response) {
        try {
            LoginResponseDTO result = authService.login(req);
            ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, result.getToken())
                    .httpOnly(true)
                    .secure(true)        
                    .sameSite("Strict")
                    .path("/")
                    .maxAge(Duration.ofHours(1))
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            return ResponseEntity.ok(Map.of(
                    "nombre", result.getNombre(),
                    "correo", result.getCorreo(),
                    "role", result.getRole()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequestDTO req,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        try {
            authService.changePassword(
                    userDetails.getUsername(),
                    req.getOldPassword(),
                    req.getNewPassword()
            );
            return ResponseEntity.ok(new MessageResponse("Contraseña actualizada."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        ResponseCookie expired = ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, expired.toString());
        return ResponseEntity.ok(new MessageResponse("Sesión cerrada."));
    }

    @DeleteMapping("/account")
    public ResponseEntity<?> deleteAccount(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            authService.deleteAccount(userDetails.getUsername());
            return ResponseEntity.ok(new MessageResponse("Cuenta eliminada."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "No autenticado."));
        }
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Usuario no encontrado."));
        }
        return ResponseEntity.ok(Map.of(
            "correo", user.getEmail(),
            "nombre", user.getName(),
            "createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : ""
        ));
    }
}