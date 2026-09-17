package com.netiot.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.netiot.service.entity.Device;

import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long> {
    List<Device> findByUserIdOrderByFechaRegistroDesc(String userId);
    Optional<Device> findByIdAndUserId(Long id, String userId);
}

