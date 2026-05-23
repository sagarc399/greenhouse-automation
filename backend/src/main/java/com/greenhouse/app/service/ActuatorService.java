package com.greenhouse.app.service;

import com.greenhouse.app.dto.ActuatorDto;
import com.greenhouse.app.entity.Actuator;
import com.greenhouse.app.entity.Zone;
import com.greenhouse.app.exception.ResourceNotFoundException;
import com.greenhouse.app.repository.ActuatorRepository;
import com.greenhouse.app.repository.ZoneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer for {@link Actuator} business logic.
 */
@Service
@Transactional
public class ActuatorService {

    /** JPA repository for actuator persistence operations. */
    private final ActuatorRepository actuatorRepository;

    /** JPA repository used to resolve parent zone references. */
    private final ZoneRepository zoneRepository;

    /**
     * Constructs the service with required repositories.
     *
     * @param actuatorRepository JPA repository for actuators
     * @param zoneRepository     JPA repository for zones
     */
    public ActuatorService(ActuatorRepository actuatorRepository, ZoneRepository zoneRepository) {
        this.actuatorRepository = actuatorRepository;
        this.zoneRepository = zoneRepository;
    }

    /**
     * Returns all actuators as DTOs.
     *
     * @return list of all actuators
     */
    @Transactional(readOnly = true)
    public List<ActuatorDto> findAll() {
        return actuatorRepository.findAll().stream().map(this::toDto).toList();
    }

    /**
     * Returns all actuators in a given zone.
     *
     * @param zoneId the zone primary key
     * @return list of actuators in that zone
     */
    @Transactional(readOnly = true)
    public List<ActuatorDto> findByZone(Long zoneId) {
        return actuatorRepository.findByZoneId(zoneId).stream().map(this::toDto).toList();
    }

    /**
     * Finds a single actuator by primary key.
     *
     * @param id the actuator ID
     * @return the actuator DTO
     * @throws ResourceNotFoundException if not found
     */
    @Transactional(readOnly = true)
    public ActuatorDto findById(Long id) {
        return toDto(getOrThrow(id));
    }

    /**
     * Creates a new actuator in a zone.
     *
     * @param dto actuator data with a valid {@code zoneId}
     * @return the persisted actuator as a DTO
     * @throws ResourceNotFoundException if the zone does not exist
     */
    public ActuatorDto create(ActuatorDto dto) {
        Zone zone = zoneRepository.findById(dto.zoneId())
                .orElseThrow(() -> new ResourceNotFoundException("Zone", dto.zoneId()));
        Actuator entity = new Actuator();
        entity.setZone(zone);
        applyDto(entity, dto);
        return toDto(actuatorRepository.save(entity));
    }

    /**
     * Updates an existing actuator (name, type, state).
     *
     * @param id  the actuator ID to update
     * @param dto new data to apply
     * @return the updated actuator as a DTO
     * @throws ResourceNotFoundException if not found
     */
    public ActuatorDto update(Long id, ActuatorDto dto) {
        Actuator entity = getOrThrow(id);
        applyDto(entity, dto);
        return toDto(actuatorRepository.save(entity));
    }

    /**
     * Changes only the state of an actuator (ENCENDIDO / APAGADO).
     *
     * @param id    the actuator ID
     * @param state the new state
     * @return the updated actuator as a DTO
     * @throws ResourceNotFoundException if not found
     */
    public ActuatorDto setState(Long id, Actuator.ActuatorState state) {
        Actuator entity = getOrThrow(id);
        entity.setState(state);
        return toDto(actuatorRepository.save(entity));
    }

    /**
     * Deletes an actuator.
     *
     * @param id the actuator ID to delete
     * @throws ResourceNotFoundException if not found
     */
    public void delete(Long id) {
        if (!actuatorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Actuator", id);
        }
        actuatorRepository.deleteById(id);
    }

    // ── package-private for AutomationRuleService ─────────────────────────

    /**
     * Loads an actuator entity by ID for internal use by other services.
     *
     * @param id actuator primary key
     * @return the loaded entity
     * @throws ResourceNotFoundException if not found
     */
    Actuator getOrThrow(Long id) {
        return actuatorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Actuator", id));
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private void applyDto(Actuator entity, ActuatorDto dto) {
        entity.setName(dto.name());
        entity.setType(dto.type());
        if (dto.state() != null) {
            entity.setState(dto.state());
        }
    }

    private ActuatorDto toDto(Actuator a) {
        return new ActuatorDto(
                a.getId(),
                a.getName(),
                a.getType(),
                a.getState(),
                a.getZone().getId(),
                a.getZone().getName(),
                a.getCreatedAt(),
                a.getUpdatedAt()
        );
    }
}
