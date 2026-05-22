package com.greenhouse.app.controller;

import com.greenhouse.app.dto.AutomationRuleDto;
import com.greenhouse.app.service.AutomationRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for {@link com.greenhouse.app.entity.AutomationRule} CRUD operations.
 */
@RestController
@RequestMapping("/api/automation-rules")
@Tag(name = "Automation Rules", description = "Manage sensor-triggered automation rules")
public class AutomationRuleController {

    private final AutomationRuleService service;

    /**
     * Constructs the controller with its required service.
     *
     * @param service automation rule business logic service
     */
    public AutomationRuleController(AutomationRuleService service) {
        this.service = service;
    }

    /**
     * Returns all automation rules, optionally filtered by zone.
     *
     * @param zoneId optional zone ID to filter by
     * @return HTTP 200 with list of rule DTOs
     */
    @GetMapping
    @Operation(summary = "List automation rules, optionally filtered by zone")
    public ResponseEntity<List<AutomationRuleDto>> findAll(
            @Parameter(description = "Filter by zone ID")
            @RequestParam(required = false) Long zoneId) {
        if (zoneId != null) {
            return ResponseEntity.ok(service.findByZone(zoneId));
        }
        return ResponseEntity.ok(service.findAll());
    }

    /**
     * Returns a single automation rule by ID.
     *
     * @param id the rule primary key
     * @return HTTP 200 with the rule DTO
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get automation rule by ID")
    public ResponseEntity<AutomationRuleDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    /**
     * Creates a new automation rule.
     *
     * @param dto rule data
     * @return HTTP 201 with the created rule DTO
     */
    @PostMapping
    @Operation(summary = "Create a new automation rule")
    public ResponseEntity<AutomationRuleDto> create(@Valid @RequestBody AutomationRuleDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    /**
     * Updates an existing automation rule.
     *
     * @param id  the rule ID to update
     * @param dto new data
     * @return HTTP 200 with the updated rule DTO
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update an automation rule")
    public ResponseEntity<AutomationRuleDto> update(
            @PathVariable Long id, @Valid @RequestBody AutomationRuleDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    /**
     * Deletes an automation rule.
     *
     * @param id the rule ID to delete
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an automation rule")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
