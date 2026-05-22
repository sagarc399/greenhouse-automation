package com.greenhouse.app.dto;

import com.greenhouse.app.entity.Alert.AlertSeverity;
import com.greenhouse.app.entity.Alert.AlertStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Data transfer object for {@link com.greenhouse.app.entity.Alert}.
 */
public record AlertDto(
        Long id,
        @NotBlank String message,
        @NotNull AlertSeverity severity,
        AlertStatus status,
        @NotNull Long sensorId,
        String sensorName,
        Long automationRuleId,
        String automationRuleName,
        Double triggerValue,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
