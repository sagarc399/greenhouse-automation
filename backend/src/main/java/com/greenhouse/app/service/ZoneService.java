package com.greenhouse.app.service;

import com.greenhouse.app.dto.ZoneDto;
import com.greenhouse.app.entity.Greenhouse;
import com.greenhouse.app.entity.Zone;
import com.greenhouse.app.exception.ResourceNotFoundException;
import com.greenhouse.app.repository.GreenhouseRepository;
import com.greenhouse.app.repository.ZoneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer for {@link Zone} business logic.
 */
@Service
@Transactional
public class ZoneService {

    private final ZoneRepository zoneRepository;
    private final GreenhouseRepository greenhouseRepository;

    /**
     * Constructs the service with required repositories.
     *
     * @param zoneRepository        JPA repository for zones
     * @param greenhouseRepository  JPA repository for greenhouses
     */
    public ZoneService(ZoneRepository zoneRepository, GreenhouseRepository greenhouseRepository) {
        this.zoneRepository = zoneRepository;
        this.greenhouseRepository = greenhouseRepository;
    }

    /**
     * Returns all zones as DTOs.
     *
     * @return list of all zones
     */
    @Transactional(readOnly = true)
    public List<ZoneDto> findAll() {
        return zoneRepository.findAll().stream().map(this::toDto).toList();
    }

    /**
     * Returns all zones belonging to a greenhouse.
     *
     * @param greenhouseId the greenhouse primary key
     * @return list of zones for that greenhouse
     */
    @Transactional(readOnly = true)
    public List<ZoneDto> findByGreenhouse(Long greenhouseId) {
        return zoneRepository.findByGreenhouseId(greenhouseId).stream().map(this::toDto).toList();
    }

    /**
     * Finds a single zone by primary key.
     *
     * @param id the zone ID
     * @return the zone DTO
     * @throws ResourceNotFoundException if not found
     */
    @Transactional(readOnly = true)
    public ZoneDto findById(Long id) {
        return toDto(getOrThrow(id));
    }

    /**
     * Creates a new zone inside a greenhouse.
     *
     * @param dto zone data with a valid {@code greenhouseId}
     * @return the persisted zone as a DTO
     * @throws ResourceNotFoundException if the greenhouse does not exist
     */
    public ZoneDto create(ZoneDto dto) {
        Greenhouse greenhouse = greenhouseRepository.findById(dto.greenhouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Greenhouse", dto.greenhouseId()));
        Zone entity = new Zone();
        entity.setGreenhouse(greenhouse);
        applyDto(entity, dto);
        return toDto(zoneRepository.save(entity));
    }

    /**
     * Updates an existing zone.
     *
     * @param id  the zone ID to update
     * @param dto new data to apply
     * @return the updated zone as a DTO
     * @throws ResourceNotFoundException if not found
     */
    public ZoneDto update(Long id, ZoneDto dto) {
        Zone entity = getOrThrow(id);
        if (!entity.getGreenhouse().getId().equals(dto.greenhouseId())) {
            Greenhouse greenhouse = greenhouseRepository.findById(dto.greenhouseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Greenhouse", dto.greenhouseId()));
            entity.setGreenhouse(greenhouse);
        }
        applyDto(entity, dto);
        return toDto(zoneRepository.save(entity));
    }

    /**
     * Deletes a zone and all its child entities.
     *
     * @param id the zone ID to delete
     * @throws ResourceNotFoundException if not found
     */
    public void delete(Long id) {
        if (!zoneRepository.existsById(id)) {
            throw new ResourceNotFoundException("Zone", id);
        }
        zoneRepository.deleteById(id);
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private Zone getOrThrow(Long id) {
        return zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone", id));
    }

    private void applyDto(Zone entity, ZoneDto dto) {
        entity.setName(dto.name());
        entity.setDescription(dto.description());
    }

    private ZoneDto toDto(Zone z) {
        return new ZoneDto(
                z.getId(),
                z.getName(),
                z.getDescription(),
                z.getGreenhouse().getId(),
                z.getGreenhouse().getName(),
                z.getCreatedAt(),
                z.getUpdatedAt()
        );
    }
}
