package com.netiot.service.controller;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.netiot.service.dto.DeviceDTO;
import com.netiot.service.service.DeviceService;

import jakarta.validation.Valid;

import java.util.Map;

@RestController
@RequestMapping("/api/dispositivos")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping
    public ResponseEntity<?> list(@AuthenticationPrincipal UserDetails userDetails,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "20") int size) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        Page<DeviceDTO> result = deviceService.getAll(
                userDetails.getUsername(),
                PageRequest.of(Math.max(page, 0), safeSize, Sort.by(Sort.Direction.DESC, "fechaRegistro"))
        );
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody DeviceDTO dto,
                                     @AuthenticationPrincipal UserDetails userDetails) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(deviceService.create(userDetails.getUsername(), dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                     @Valid @RequestBody DeviceDTO dto,
                                     @AuthenticationPrincipal UserDetails userDetails) {
        try {
            return ResponseEntity.ok(deviceService.update(userDetails.getUsername(), id, dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id,
                                     @AuthenticationPrincipal UserDetails userDetails) {
        try {
            deviceService.delete(userDetails.getUsername(), id);
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }
}