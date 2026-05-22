package com.greenhouse.app.controller;

import com.greenhouse.app.dto.AlertDto;
import com.greenhouse.app.entity.Alert.AlertStatus;
import com.greenhouse.app.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for {@link com.greenhouse.app.entity.Alert} operations.
 */
@RestController
@RequestMapping("/api/alerts")
@Tag(name = "Alerts", description = "Manage and acknowledge system alerts")
public class AlertController {

    private final AlertService service;

    /**
     * Constructs the controller with its required service.
     *
     * @param service alert business logic service
     */
    public AlertController(AlertService service) {
        this.service = service;
    }

    /**
     * Returns all alerts, optionally filtered by status.
     *
     * @param pending if {@code true}, returns only pending alerts
     * @return HTTP 200 with list of alert DTOs
     */
    @GetMapping
    @Operation(summary = "List all alerts, optionally filtered to pending only")
    public ResponseEntity<List<AlertDto>> findAll(
            @Parameter(description = "If true, return only PENDIENTE alerts")
            @RequestParam(defaultValue = "false") boolean pending) {
        if (pending) {
            return ResponseEntity.ok(service.findPending());
        }
        return ResponseEntity.ok(service.findAll());
    }

    /**
     * Returns a single alert by ID.
     *
     * @param id the alert primary key
     * @return HTTP 200 with the alert DTO
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get alert by ID")
    public ResponseEntity<AlertDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    /**
     * Creates a new alert manually.
     *
     * @param dto alert data with {@code sensorId} and {@code severity}
     * @return HTTP 201 with the created alert DTO
     */
    @PostMapping
    @Operation(summary = "Create a manual alert")
    public ResponseEntity<AlertDto> create(@Valid @RequestBody AlertDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    /**
     * Updates the status of an alert (e.g., mark as ATENDIDA).
     *
     * @param id     the alert ID
     * @param status the new status
     * @return HTTP 200 with the updated alert DTO
     */
    @PutMapping("/{id}/status")
    @Operation(
        summary = "Update alert status",
        description = "Operators use this to mark an alert as ATENDIDA once handled."
    )
    public ResponseEntity<AlertDto> updateStatus(
            @PathVariable Long id,
            @Parameter(description = "New status: PENDIENTE or ATENDIDA")
            @RequestParam AlertStatus status) {
        return ResponseEntity.ok(service.updateStatus(id, status));
    }

    /**
     * Deletes an alert.
     *
     * @param id the alert ID to delete
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an alert")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
