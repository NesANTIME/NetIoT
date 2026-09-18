package com.netiot.service.dto;


import java.time.LocalDateTime;

import com.netiot.service.entity.Device;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class DeviceDTO {
    private Long id;

    @NotBlank(message = "El nombre del dispositivo es obligatorio.")
    @Size(max = 100, message = "El nombre del dispositivo no puede exceder 100 caracteres.")
    private String nombreDispositivo;

    @Size(max = 100, message = "El tipo no puede exceder 100 caracteres.")
    private String tipo;

    @Size(max = 100, message = "El modelo no puede exceder 100 caracteres.")
    private String modelo;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres.")
    private String descripcion;

    @Size(max = 150, message = "El nombre del fabricante no puede exceder 150 caracteres.")
    private String fabricanteNombre;

    @Size(max = 100, message = "El país del fabricante no puede exceder 100 caracteres.")
    private String fabricantePais;

    @Size(max = 300, message = "El sitio web del fabricante no puede exceder 300 caracteres.")
    private String fabricanteSitioWeb;

    @Pattern(regexp = "^(\\d{1,3}\\.){3}\\d{1,3}$", message = "IP inválida.")
    private String ipAddress;

    @Pattern(regexp = "^([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}$", message = "MAC inválida.")
    private String macAddress;

    @Size(max = 150, message = "La ubicación no puede exceder 150 caracteres.")
    private String ubicacion;

    private boolean tieneApi;

    private LocalDateTime fechaRegistro;

    public static DeviceDTO from(Device d) {
        DeviceDTO dto = new DeviceDTO();
        dto.id = d.getId();
        dto.nombreDispositivo = d.getNombreDispositivo();
        dto.tipo = d.getTipo();
        dto.modelo = d.getModelo();
        dto.descripcion = d.getDescripcion();
        dto.fabricanteNombre = d.getFabricanteNombre();
        dto.fabricantePais = d.getFabricantePais();
        dto.fabricanteSitioWeb = d.getFabricanteSitioWeb();
        dto.ipAddress = d.getIpAddress();
        dto.macAddress = d.getMacAddress();
        dto.ubicacion = d.getUbicacion();
        dto.tieneApi = d.isTieneApi();
        dto.fechaRegistro = d.getFechaRegistro();
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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