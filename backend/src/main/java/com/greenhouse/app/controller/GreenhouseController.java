package com.greenhouse.app.controller;

import com.greenhouse.app.dto.GreenhouseDto;
import com.greenhouse.app.service.GreenhouseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for {@link com.greenhouse.app.entity.Greenhouse} CRUD operations.
 */
@RestController
@RequestMapping("/api/greenhouses")
@Tag(name = "Greenhouses", description = "Manage greenhouse facilities")
public class GreenhouseController {

    private final GreenhouseService service;

    /**
     * Constructs the controller with its required service.
     *
     * @param service greenhouse business logic service
     */
    public GreenhouseController(GreenhouseService service) {
        this.service = service;
    }

    /**
     * Returns a list of all greenhouses.
     *
     * @return HTTP 200 with list of greenhouse DTOs
     */
    @GetMapping
    @Operation(summary = "List all greenhouses")
    public ResponseEntity<List<GreenhouseDto>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    /**
     * Returns a single greenhouse by ID.
     *
     * @param id the greenhouse primary key
     * @return HTTP 200 with the greenhouse DTO, or 404 if not found
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get greenhouse by ID")
    @ApiResponse(responseCode = "404", description = "Greenhouse not found")
    public ResponseEntity<GreenhouseDto> findById(
            @Parameter(description = "Greenhouse ID") @PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    /**
     * Creates a new greenhouse.
     *
     * @param dto greenhouse data (name required)
     * @return HTTP 201 with the created greenhouse DTO
     */
    @PostMapping
    @Operation(summary = "Create a new greenhouse")
    @ApiResponse(responseCode = "201", description = "Greenhouse created")
    public ResponseEntity<GreenhouseDto> create(@Valid @RequestBody GreenhouseDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    /**
     * Updates an existing greenhouse.
     *
     * @param id  the greenhouse ID to update
     * @param dto new data
     * @return HTTP 200 with the updated greenhouse DTO
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update a greenhouse")
    public ResponseEntity<GreenhouseDto> update(
            @PathVariable Long id, @Valid @RequestBody GreenhouseDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    /**
     * Deletes a greenhouse and all its child entities.
     *
     * @param id the greenhouse ID to delete
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a greenhouse")
    @ApiResponse(responseCode = "204", description = "Greenhouse deleted")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
