package com.netiot.service.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.netiot.service.dto.DeviceDTO;
import com.netiot.service.entity.Device;
import com.netiot.service.entity.User;
import com.netiot.service.repository.DeviceRepository;
import com.netiot.service.repository.UserRepository;

import java.util.List;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;

    public DeviceService(DeviceRepository deviceRepository, UserRepository userRepository) {
        this.deviceRepository = deviceRepository;
        this.userRepository = userRepository;
    }

    public List<DeviceDTO> getAll(String email) {
        User user = getUser(email);
        return deviceRepository.findByUserIdOrderByFechaRegistroDesc(user.getId())
                .stream().map(DeviceDTO::from).toList();
    }

    public Page<DeviceDTO> getAll(String email, Pageable pageable) {
        User user = getUser(email);
        return deviceRepository.findByUserId(user.getId(), pageable).map(DeviceDTO::from);
    }

    public DeviceDTO create(String email, DeviceDTO dto) {
        User user = getUser(email);
        Device d = new Device();
        d.setUser(user);
        applyDTO(d, dto);
        return DeviceDTO.from(deviceRepository.save(d));
    }

    public DeviceDTO update(String email, Long id, DeviceDTO dto) {
        User user = getUser(email);
        Device d = deviceRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Dispositivo no encontrado."));
        applyDTO(d, dto);
        return DeviceDTO.from(deviceRepository.save(d));
    }

    public void delete(String email, Long id) {
        User user = getUser(email);
        Device d = deviceRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Dispositivo no encontrado."));
        deviceRepository.delete(d);
    }

    private void applyDTO(Device d, DeviceDTO dto) {
        if (dto.getNombreDispositivo() != null) d.setNombreDispositivo(dto.getNombreDispositivo());
        d.setTipo(dto.getTipo());
        d.setModelo(dto.getModelo());
        d.setDescripcion(dto.getDescripcion());
        d.setFabricanteNombre(dto.getFabricanteNombre());
        d.setFabricantePais(dto.getFabricantePais());
        d.setFabricanteSitioWeb(dto.getFabricanteSitioWeb());
        d.setIpAddress(dto.getIpAddress());
        d.setMacAddress(dto.getMacAddress());
        d.setUbicacion(dto.getUbicacion());
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado."));
    }
}