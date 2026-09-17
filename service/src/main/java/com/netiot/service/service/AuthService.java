package com.netiot.service.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.netiot.service.dto.LoginRequestDTO;
import com.netiot.service.dto.LoginResponseDTO;
import com.netiot.service.dto.MessageResponse;
import com.netiot.service.dto.RegisterRequestDTO;
import com.netiot.service.entity.User;
import com.netiot.service.repository.UserRepository;
import com.netiot.service.security.JwtTokenProvider;

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
        user.setName(req.getNombre());
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
        return new LoginResponseDTO(token, user.getName(), user.getEmail(), user.getRole());
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
