package com.controladoriot.controladoriot.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "devices")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String nombreDispositivo;

    private String tipo;
    private String modelo;

    @Column(length = 500)
    private String descripcion;

    private String fabricanteNombre;
    private String fabricantePais;
    private String fabricanteSitioWeb;
    private String ipAddress;
    private String macAddress;
    private String ubicacion;

    @Column(columnDefinition = "boolean default false")
    private boolean tieneApi = false;

    @CreationTimestamp
    private LocalDateTime fechaRegistro;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getNombreDispositivo() { return nombreDispositivo; }
    public void setNombreDispositivo(String v) { this.nombreDispositivo = v; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getFabricanteNombre() { return fabricanteNombre; }
    public void setFabricanteNombre(String v) { this.fabricanteNombre = v; }
    public String getFabricantePais() { return fabricantePais; }
    public void setFabricantePais(String v) { this.fabricantePais = v; }
    public String getFabricanteSitioWeb() { return fabricanteSitioWeb; }
    public void setFabricanteSitioWeb(String v) { this.fabricanteSitioWeb = v; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getMacAddress() { return macAddress; }
    public void setMacAddress(String macAddress) { this.macAddress = macAddress; }
    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    public boolean isTieneApi() { return tieneApi; }
    public void setTieneApi(boolean tieneApi) { this.tieneApi = tieneApi; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}
