package com.controller_iot.controlador_iot_y_domotica.config;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter_Authentication extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    public JwtFilter_Authentication(JwtUtil jwtUtil) { this.jwtUtil = jwtUtil; }



    private String extractJwtFromRequest(HttpServletRequest peticion) {
        String token_temporal = peticion.getHeader("Authorization");
        if (token_temporal != null && token_temporal.startsWith("Bearer ")) {
            return token_temporal.substring(7);
        }
        return null;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
    throws ServletException, IOException {

        final String jwt_variable = extractJwtFromRequest(request);

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }
        if (jwt_variable != null && jwtUtil.validateToken(jwt_variable)) { 
            String username = jwtUtil.getUsernameFromToken(jwt_variable); //
            String userType = jwtUtil.getUserTypeFromToken(jwt_variable);

            List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                    new SimpleGrantedAuthority(userType)
            );
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    username, // Principal (el nombre o ID del usuario)
                    null,     // Credenciales (nulo para tokens, ya están validadas)
                    authorities // Autoridades
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
    
}
