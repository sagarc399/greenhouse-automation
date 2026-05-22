package com.greenhouse.app.controller;

import com.greenhouse.app.dto.ZoneDto;
import com.greenhouse.app.service.ZoneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for {@link com.greenhouse.app.entity.Zone} CRUD operations.
 */
@RestController
@RequestMapping("/api/zones")
@Tag(name = "Zones", description = "Manage zones within greenhouses")
public class ZoneController {

    private final ZoneService service;

    /**
     * Constructs the controller with its required service.
     *
     * @param service zone business logic service
     */
    public ZoneController(ZoneService service) {
        this.service = service;
    }

    /**
     * Returns all zones, optionally filtered by greenhouse.
     *
     * @param greenhouseId optional greenhouse ID to filter by
     * @return HTTP 200 with list of zone DTOs
     */
    @GetMapping
    @Operation(summary = "List zones, optionally filtered by greenhouse")
    public ResponseEntity<List<ZoneDto>> findAll(
            @Parameter(description = "Filter by greenhouse ID")
            @RequestParam(required = false) Long greenhouseId) {
        if (greenhouseId != null) {
            return ResponseEntity.ok(service.findByGreenhouse(greenhouseId));
        }
        return ResponseEntity.ok(service.findAll());
    }

    /**
     * Returns a single zone by ID.
     *
     * @param id the zone primary key
     * @return HTTP 200 with the zone DTO
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get zone by ID")
    public ResponseEntity<ZoneDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    /**
     * Creates a new zone inside a greenhouse.
     *
     * @param dto zone data with {@code greenhouseId}
     * @return HTTP 201 with the created zone DTO
     */
    @PostMapping
    @Operation(summary = "Create a new zone")
    public ResponseEntity<ZoneDto> create(@Valid @RequestBody ZoneDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    /**
     * Updates an existing zone.
     *
     * @param id  the zone ID to update
     * @param dto new data
     * @return HTTP 200 with the updated zone DTO
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update a zone")
    public ResponseEntity<ZoneDto> update(@PathVariable Long id, @Valid @RequestBody ZoneDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    /**
     * Deletes a zone and all its child entities.
     *
     * @param id the zone ID to delete
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a zone")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
