package com.greenhouse.app.controller;

import com.greenhouse.app.dto.SensorReadingDto;
import com.greenhouse.app.service.SensorReadingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for {@link com.greenhouse.app.entity.SensorReading} operations.
 */
@RestController
@RequestMapping("/api/sensor-readings")
@Tag(name = "Sensor Readings", description = "Record and query sensor telemetry data")
public class SensorReadingController {

    private final SensorReadingService service;

    /**
     * Constructs the controller with its required service.
     *
     * @param service sensor reading business logic service
     */
    public SensorReadingController(SensorReadingService service) {
        this.service = service;
    }

    /**
     * Returns paginated readings for a sensor.
     *
     * @param sensorId sensor primary key
     * @param pageable pagination parameters (default: 20 per page)
     * @return HTTP 200 with a page of reading DTOs
     */
    @GetMapping
    @Operation(summary = "List readings for a sensor (paginated, newest first)")
    public ResponseEntity<Page<SensorReadingDto>> findBySensor(
            @Parameter(description = "Sensor ID", required = true)
            @RequestParam Long sensorId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(service.findBySensor(sensorId, pageable));
    }

    /**
     * Returns a single reading by ID.
     *
     * @param id the reading primary key
     * @return HTTP 200 with the reading DTO
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get reading by ID")
    public ResponseEntity<SensorReadingDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    /**
     * Records a new sensor reading and evaluates automation rules.
     *
     * @param dto reading data with {@code sensorId} and {@code value}
     * @return HTTP 201 with the created reading DTO
     */
    @PostMapping
    @Operation(
        summary = "Record a new sensor reading",
        description = "Saves the reading and evaluates any active automation rules for the sensor."
    )
    public ResponseEntity<SensorReadingDto> create(@Valid @RequestBody SensorReadingDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    /**
     * Deletes a sensor reading.
     *
     * @param id the reading ID to delete
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a sensor reading")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
