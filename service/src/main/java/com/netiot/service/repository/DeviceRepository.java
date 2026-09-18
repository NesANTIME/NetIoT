package com.netiot.service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.netiot.service.entity.Device;

import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long> {
    List<Device> findByUserIdOrderByFechaRegistroDesc(String userId);

    Page<Device> findByUserId(String userId, Pageable pageable);

    Optional<Device> findByIdAndUserId(Long id, String userId);
}