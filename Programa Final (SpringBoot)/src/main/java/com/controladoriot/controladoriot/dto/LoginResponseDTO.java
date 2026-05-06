package com.controladoriot.controladoriot.dto;

public class LoginResponseDTO {
    private String token;
    private String nombre;
    private String correo;
    private String role;

    public LoginResponseDTO(String token, String nombre, String correo, String role) {
        this.token = token;
        this.nombre = nombre;
        this.correo = correo;
        this.role = role;
    }

    public String getToken() { return token; }
    public String getNombre() { return nombre; }
    public String getCorreo() { return correo; }
    public String getRole() { return role; }
}
