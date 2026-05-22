package com.greenhouse.app.controller;

import com.greenhouse.app.dto.SensorDto;
import com.greenhouse.app.service.SensorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for {@link com.greenhouse.app.entity.Sensor} CRUD operations.
 */
@RestController
@RequestMapping("/api/sensors")
@Tag(name = "Sensors", description = "Manage sensors within zones")
public class SensorController {

    private final SensorService service;

    /**
     * Constructs the controller with its required service.
     *
     * @param service sensor business logic service
     */
    public SensorController(SensorService service) {
        this.service = service;
    }

    /**
     * Returns all sensors, optionally filtered by zone.
     *
     * @param zoneId optional zone ID to filter by
     * @return HTTP 200 with list of sensor DTOs
     */
    @GetMapping
    @Operation(summary = "List sensors, optionally filtered by zone")
    public ResponseEntity<List<SensorDto>> findAll(
            @Parameter(description = "Filter by zone ID")
            @RequestParam(required = false) Long zoneId) {
        if (zoneId != null) {
            return ResponseEntity.ok(service.findByZone(zoneId));
        }
        return ResponseEntity.ok(service.findAll());
    }

    /**
     * Returns a single sensor by ID.
     *
     * @param id the sensor primary key
     * @return HTTP 200 with the sensor DTO
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get sensor by ID")
    public ResponseEntity<SensorDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    /**
     * Creates a new sensor.
     *
     * @param dto sensor data with {@code zoneId}
     * @return HTTP 201 with the created sensor DTO
     */
    @PostMapping
    @Operation(summary = "Create a new sensor")
    public ResponseEntity<SensorDto> create(@Valid @RequestBody SensorDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    /**
     * Updates an existing sensor.
     *
     * @param id  the sensor ID to update
     * @param dto new data
     * @return HTTP 200 with the updated sensor DTO
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update a sensor")
    public ResponseEntity<SensorDto> update(@PathVariable Long id, @Valid @RequestBody SensorDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    /**
     * Deletes a sensor and all its readings and alerts.
     *
     * @param id the sensor ID to delete
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a sensor")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
