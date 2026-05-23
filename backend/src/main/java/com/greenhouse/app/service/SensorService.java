package com.greenhouse.app.service;

import com.greenhouse.app.dto.SensorDto;
import com.greenhouse.app.entity.Sensor;
import com.greenhouse.app.entity.Zone;
import com.greenhouse.app.exception.ResourceNotFoundException;
import com.greenhouse.app.repository.SensorRepository;
import com.greenhouse.app.repository.ZoneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer for {@link Sensor} business logic.
 */
@Service
@Transactional
public class SensorService {

    /** JPA repository for sensor persistence operations. */
    private final SensorRepository sensorRepository;

    /** JPA repository used to resolve parent zone references. */
    private final ZoneRepository zoneRepository;

    /**
     * Constructs the service with required repositories.
     *
     * @param sensorRepository JPA repository for sensors
     * @param zoneRepository   JPA repository for zones
     */
    public SensorService(SensorRepository sensorRepository, ZoneRepository zoneRepository) {
        this.sensorRepository = sensorRepository;
        this.zoneRepository = zoneRepository;
    }

    /**
     * Returns all sensors as DTOs.
     *
     * @return list of all sensors
     */
    @Transactional(readOnly = true)
    public List<SensorDto> findAll() {
        return sensorRepository.findAll().stream().map(this::toDto).toList();
    }

    /**
     * Returns all sensors in a given zone.
     *
     * @param zoneId the zone primary key
     * @return list of sensors in that zone
     */
    @Transactional(readOnly = true)
    public List<SensorDto> findByZone(Long zoneId) {
        return sensorRepository.findByZoneId(zoneId).stream().map(this::toDto).toList();
    }

    /**
     * Finds a single sensor by primary key.
     *
     * @param id the sensor ID
     * @return the sensor DTO
     * @throws ResourceNotFoundException if not found
     */
    @Transactional(readOnly = true)
    public SensorDto findById(Long id) {
        return toDto(getOrThrow(id));
    }

    /**
     * Creates a new sensor in a zone.
     *
     * @param dto sensor data with a valid {@code zoneId}
     * @return the persisted sensor as a DTO
     * @throws ResourceNotFoundException if the zone does not exist
     */
    public SensorDto create(SensorDto dto) {
        Zone zone = zoneRepository.findById(dto.zoneId())
                .orElseThrow(() -> new ResourceNotFoundException("Zone", dto.zoneId()));
        Sensor entity = new Sensor();
        entity.setZone(zone);
        applyDto(entity, dto);
        return toDto(sensorRepository.save(entity));
    }

    /**
     * Updates an existing sensor.
     *
     * @param id  the sensor ID to update
     * @param dto new data to apply
     * @return the updated sensor as a DTO
     * @throws ResourceNotFoundException if not found
     */
    public SensorDto update(Long id, SensorDto dto) {
        Sensor entity = getOrThrow(id);
        if (!entity.getZone().getId().equals(dto.zoneId())) {
            Zone zone = zoneRepository.findById(dto.zoneId())
                    .orElseThrow(() -> new ResourceNotFoundException("Zone", dto.zoneId()));
            entity.setZone(zone);
        }
        applyDto(entity, dto);
        return toDto(sensorRepository.save(entity));
    }

    /**
     * Deletes a sensor and all its readings and alerts.
     *
     * @param id the sensor ID to delete
     * @throws ResourceNotFoundException if not found
     */
    public void delete(Long id) {
        if (!sensorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Sensor", id);
        }
        sensorRepository.deleteById(id);
    }

    // ── private helpers ──────────────────────────────────────────────────────

    /**
     * Loads a sensor or throws {@link ResourceNotFoundException}.
     *
     * @param id sensor primary key
     * @return the loaded entity
     */
    public Sensor getOrThrow(Long id) {
        return sensorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sensor", id));
    }

    private void applyDto(Sensor entity, SensorDto dto) {
        entity.setName(dto.name());
        entity.setType(dto.type());
        entity.setModel(dto.model());
        entity.setActive(dto.active());
    }

    private SensorDto toDto(Sensor s) {
        return new SensorDto(
                s.getId(),
                s.getName(),
                s.getType(),
                s.getModel(),
                s.isActive(),
                s.getZone().getId(),
                s.getZone().getName(),
                s.getCreatedAt(),
                s.getUpdatedAt()
        );
    }
}
