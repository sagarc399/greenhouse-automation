package com.greenhouse.app.dto;

import com.greenhouse.app.entity.Actuator.ActuatorState;
import com.greenhouse.app.entity.AutomationRule.RuleOperator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Data transfer object for {@link com.greenhouse.app.entity.AutomationRule}.
 */
public record AutomationRuleDto(
        Long id,
        @NotBlank String name,
        @NotNull Long zoneId,
        @NotNull Long sensorId,
        String sensorName,
        @NotNull RuleOperator operator,
        @NotNull Double threshold,
        Long actuatorId,
        String actuatorName,
        ActuatorState actuatorTargetState,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
