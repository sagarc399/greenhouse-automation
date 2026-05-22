package com.greenhouse.app.dto;

import com.greenhouse.app.entity.Actuator.ActuatorState;
import com.greenhouse.app.entity.Actuator.ActuatorType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Data transfer object for {@link com.greenhouse.app.entity.Actuator}.
 */
public record ActuatorDto(
        Long id,
        @NotBlank String name,
        @NotNull ActuatorType type,
        ActuatorState state,
        @NotNull Long zoneId,
        String zoneName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
