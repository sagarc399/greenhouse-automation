package com.greenhouse.app.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

/**
 * Data transfer object for {@link com.greenhouse.app.entity.Greenhouse}.
 *
 * <p>Used for both request (create/update) and response payloads.
 * Fields not relevant for a given operation may be {@code null}.</p>
 */
public record GreenhouseDto(

        /**
         * Primary key — present in responses, ignored in create requests.
         */
        Long id,

        /**
         * Name of the greenhouse. Required for create/update.
         */
        @NotBlank String name,

        /**
         * Physical location of the greenhouse.
         */
        String location,

        /**
         * Detailed description.
         */
        String description,

        /**
         * Number of zones — included in list/detail responses.
         */
        Integer zoneCount,

        /**
         * Record creation timestamp — set by the server.
         */
        LocalDateTime createdAt,

        /**
         * Record last-update timestamp — set by the server.
         */
        LocalDateTime updatedAt
) {}
