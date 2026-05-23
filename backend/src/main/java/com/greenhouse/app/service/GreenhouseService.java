package com.greenhouse.app.service;

import com.greenhouse.app.dto.GreenhouseDto;
import com.greenhouse.app.entity.Greenhouse;
import com.greenhouse.app.exception.ResourceNotFoundException;
import com.greenhouse.app.repository.GreenhouseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer for {@link Greenhouse} business logic.
 *
 * <p>All persistence operations are wrapped in transactions.
 * Read-only queries use {@code readOnly = true} for query optimization.</p>
 */
@Service
@Transactional
public class GreenhouseService {

    /** JPA repository for greenhouse persistence operations. */
    private final GreenhouseRepository repository;

    /**
     * Constructs the service with its required repository.
     *
     * @param repository JPA repository for greenhouses
     */
    public GreenhouseService(GreenhouseRepository repository) {
        this.repository = repository;
    }

    /**
     * Returns all greenhouses as DTOs.
     *
     * @return list of all greenhouses
     */
    @Transactional(readOnly = true)
    public List<GreenhouseDto> findAll() {
        return repository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Finds a single greenhouse by primary key.
     *
     * @param id the greenhouse ID
     * @return the greenhouse DTO
     * @throws ResourceNotFoundException if no greenhouse exists with that ID
     */
    @Transactional(readOnly = true)
    public GreenhouseDto findById(Long id) {
        return toDto(getOrThrow(id));
    }

    /**
     * Creates a new greenhouse from the given DTO.
     *
     * @param dto greenhouse data (name required, id ignored)
     * @return the persisted greenhouse as a DTO
     */
    public GreenhouseDto create(GreenhouseDto dto) {
        Greenhouse entity = new Greenhouse();
        applyDto(entity, dto);
        return toDto(repository.save(entity));
    }

    /**
     * Updates an existing greenhouse.
     *
     * @param id  the greenhouse ID to update
     * @param dto new data to apply
     * @return the updated greenhouse as a DTO
     * @throws ResourceNotFoundException if no greenhouse exists with that ID
     */
    public GreenhouseDto update(Long id, GreenhouseDto dto) {
        Greenhouse entity = getOrThrow(id);
        applyDto(entity, dto);
        return toDto(repository.save(entity));
    }

    /**
     * Deletes a greenhouse and all its child zones (cascade).
     *
     * @param id the greenhouse ID to delete
     * @throws ResourceNotFoundException if no greenhouse exists with that ID
     */
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Greenhouse", id);
        }
        repository.deleteById(id);
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private Greenhouse getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Greenhouse", id));
    }

    private void applyDto(Greenhouse entity, GreenhouseDto dto) {
        entity.setName(dto.name());
        entity.setLocation(dto.location());
        entity.setDescription(dto.description());
    }

    private GreenhouseDto toDto(Greenhouse g) {
        return new GreenhouseDto(
                g.getId(),
                g.getName(),
                g.getLocation(),
                g.getDescription(),
                g.getZones().size(),
                g.getCreatedAt(),
                g.getUpdatedAt()
        );
    }
}
