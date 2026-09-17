package com.netiot.service.dto;

import com.netiot.service.entity.User;

public class LoginResponseDTO {
    private String token;
    private String nombre;
    private String correo;
    private User.Role role;

    public LoginResponseDTO(String token, String nombre, String correo, User.Role role) {
        this.token = token;
        this.nombre = nombre;
        this.correo = correo;
        this.role = role;
    }

    public String getToken() { return token; }
    public String getNombre() { return nombre; }
    public String getCorreo() { return correo; }
    public User.Role getRole() { return role; }
}
