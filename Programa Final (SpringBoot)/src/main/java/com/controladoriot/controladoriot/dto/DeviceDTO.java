package com.controladoriot.controladoriot.dto;

import com.controladoriot.controladoriot.entity.Device;

import java.time.LocalDateTime;

public class DeviceDTO {
    private Long id;
    private String nombreDispositivo;
    private String tipo;
    private String modelo;
    private String descripcion;
    private String fabricanteNombre;
    private String fabricantePais;
    private String fabricanteSitioWeb;
    private String ipAddress;
    private String macAddress;
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
