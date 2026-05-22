package com.greenhouse.app.controller;

import com.greenhouse.app.dto.ActuatorDto;
import com.greenhouse.app.entity.Actuator.ActuatorState;
import com.greenhouse.app.service.ActuatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for {@link com.greenhouse.app.entity.Actuator} CRUD operations.
 */
@RestController
@RequestMapping("/api/actuators")
@Tag(name = "Actuators", description = "Manage actuators and their states")
public class ActuatorController {

    private final ActuatorService service;

    /**
     * Constructs the controller with its required service.
     *
     * @param service actuator business logic service
     */
    public ActuatorController(ActuatorService service) {
        this.service = service;
    }

    /**
     * Returns all actuators, optionally filtered by zone.
     *
     * @param zoneId optional zone ID to filter by
     * @return HTTP 200 with list of actuator DTOs
     */
    @GetMapping
    @Operation(summary = "List actuators, optionally filtered by zone")
    public ResponseEntity<List<ActuatorDto>> findAll(
            @Parameter(description = "Filter by zone ID")
            @RequestParam(required = false) Long zoneId) {
        if (zoneId != null) {
            return ResponseEntity.ok(service.findByZone(zoneId));
        }
        return ResponseEntity.ok(service.findAll());
    }

    /**
     * Returns a single actuator by ID.
     *
     * @param id the actuator primary key
     * @return HTTP 200 with the actuator DTO
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get actuator by ID")
    public ResponseEntity<ActuatorDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    /**
     * Creates a new actuator.
     *
     * @param dto actuator data with {@code zoneId}
     * @return HTTP 201 with the created actuator DTO
     */
    @PostMapping
    @Operation(summary = "Create a new actuator")
    public ResponseEntity<ActuatorDto> create(@Valid @RequestBody ActuatorDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    /**
     * Updates an existing actuator.
     *
     * @param id  the actuator ID to update
     * @param dto new data
     * @return HTTP 200 with the updated actuator DTO
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update an actuator")
    public ResponseEntity<ActuatorDto> update(@PathVariable Long id, @Valid @RequestBody ActuatorDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    /**
     * Changes the state of an actuator (ENCENDIDO / APAGADO).
     *
     * @param id    the actuator ID
     * @param state the new state value
     * @return HTTP 200 with the updated actuator DTO
     */
    @PatchMapping("/{id}/state")
    @Operation(summary = "Toggle actuator state (ENCENDIDO / APAGADO)")
    public ResponseEntity<ActuatorDto> setState(
            @PathVariable Long id,
            @Parameter(description = "New state: ENCENDIDO or APAGADO")
            @RequestParam ActuatorState state) {
        return ResponseEntity.ok(service.setState(id, state));
    }

    /**
     * Deletes an actuator.
     *
     * @param id the actuator ID to delete
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an actuator")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
