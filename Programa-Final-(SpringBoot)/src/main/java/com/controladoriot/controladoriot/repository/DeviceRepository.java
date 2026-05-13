package com.controladoriot.controladoriot.repository;

import com.controladoriot.controladoriot.entity.Device;
import com.controladoriot.controladoriot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long> {
    List<Device> findByUserOrderByFechaRegistroDesc(User user);
    Optional<Device> findByIdAndUser(Long id, User user);
}
