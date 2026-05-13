package com.controladoriot.controladoriot.service;

import com.controladoriot.controladoriot.dto.*;
import com.controladoriot.controladoriot.entity.User;
import com.controladoriot.controladoriot.repository.UserRepository;
import com.controladoriot.controladoriot.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public MessageResponse register(RegisterRequestDTO req) {
        if (userRepository.existsByEmail(req.getCorreo())) {
            throw new IllegalArgumentException("El correo ya está registrado.");
        }
        User user = new User();
        user.setEmail(req.getCorreo());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setNombre(req.getNombre());
        userRepository.save(user);
        return new MessageResponse("Usuario registrado exitosamente.");
    }

    public LoginResponseDTO login(LoginRequestDTO req) {
        User user = userRepository.findByEmail(req.getCorreo())
                .orElseThrow(() -> new IllegalArgumentException("Correo o contraseña incorrectos."));

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Correo o contraseña incorrectos.");
        }

        String token = jwtTokenProvider.generateToken(user.getEmail());
        return new LoginResponseDTO(token, user.getNombre(), user.getEmail(), user.getRole());
    }

    public void deleteAccount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        userRepository.delete(user);
    }

    public void changePassword(String email, String oldPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta.");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
