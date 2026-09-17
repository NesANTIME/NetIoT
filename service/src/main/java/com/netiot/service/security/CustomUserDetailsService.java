package com.netiot.service.security;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.netiot.service.entity.User;
import com.netiot.service.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);
    private static final User.Role DEFAULT_ROLE = User.Role.USER;

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String normalizedEmail = normalizeEmail(email);

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> {
                    log.warn("Intento de login fallido: usuario no encontrado (email hash: {})",
                            normalizedEmail.hashCode());
                    return new UsernameNotFoundException("Credenciales inválidas");
                });

        User.Role role = resolveRole(user);

        log.info("Usuario autenticado cargado correctamente: id={}", user.getId());

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + role.name())))
                .disabled(!user.isEnabled())
                .accountLocked(user.isLocked())
                .accountExpired(false)
                .credentialsExpired(false)
                .build();
    }

    private String normalizeEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new UsernameNotFoundException("Credenciales inválidas");
        }
        return email.trim().toLowerCase();
    }

    private User.Role resolveRole(User user) {
        User.Role role = user.getRole();
        if (role == null) {
            log.warn("Usuario id={} no tiene rol asignado, se usa rol por defecto '{}'",
                    user.getId(), DEFAULT_ROLE);
            return DEFAULT_ROLE;
        }
        return role;
    }
}