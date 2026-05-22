package com.greenhouse.app.dto;

import com.greenhouse.app.entity.Sensor.SensorType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Data transfer object for {@link com.greenhouse.app.entity.Sensor}.
 */
public record SensorDto(
        Long id,
        @NotBlank String name,
        @NotNull SensorType type,
        String model,
        boolean active,
        @NotNull Long zoneId,
        String zoneName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
