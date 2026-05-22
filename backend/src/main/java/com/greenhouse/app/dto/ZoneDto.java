package com.greenhouse.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Data transfer object for {@link com.greenhouse.app.entity.Zone}.
 */
public record ZoneDto(
        Long id,
        @NotBlank String name,
        String description,
        /** Parent greenhouse ID. Required for create requests. */
        @NotNull Long greenhouseId,
        String greenhouseName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
