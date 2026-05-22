package com.greenhouse.app.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Data transfer object for {@link com.greenhouse.app.entity.SensorReading}.
 */
public record SensorReadingDto(
        Long id,
        @NotNull Double value,
        String unit,
        LocalDateTime recordedAt,
        @NotNull Long sensorId,
        String sensorName
) {}
