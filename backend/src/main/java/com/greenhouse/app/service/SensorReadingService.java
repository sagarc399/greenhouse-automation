package com.greenhouse.app.service;

import com.greenhouse.app.dto.SensorReadingDto;
import com.greenhouse.app.entity.Sensor;
import com.greenhouse.app.entity.SensorReading;
import com.greenhouse.app.exception.ResourceNotFoundException;
import com.greenhouse.app.repository.SensorReadingRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer for {@link SensorReading} business logic.
 *
 * <p>After a reading is saved, active automation rules for the sensor's zone
 * are evaluated. This is delegated to {@link AutomationRuleService}.</p>
 */
@Service
@Transactional
public class SensorReadingService {

    private final SensorReadingRepository readingRepository;
    private final SensorService sensorService;
    private final AutomationRuleService ruleService;

    /**
     * Constructs the service with required dependencies.
     *
     * @param readingRepository JPA repository for sensor readings
     * @param sensorService     service to load sensor entities
     * @param ruleService       service to evaluate automation rules
     */
    public SensorReadingService(SensorReadingRepository readingRepository,
                                SensorService sensorService,
                                AutomationRuleService ruleService) {
        this.readingRepository = readingRepository;
        this.sensorService = sensorService;
        this.ruleService = ruleService;
    }

    /**
     * Returns a page of readings for a sensor, ordered newest-first.
     *
     * @param sensorId sensor primary key
     * @param pageable pagination parameters
     * @return page of reading DTOs
     */
    @Transactional(readOnly = true)
    public Page<SensorReadingDto> findBySensor(Long sensorId, Pageable pageable) {
        return readingRepository.findBySensorIdOrderByRecordedAtDesc(sensorId, pageable)
                .map(this::toDto);
    }

    /**
     * Finds a single reading by primary key.
     *
     * @param id the reading ID
     * @return the reading DTO
     * @throws ResourceNotFoundException if not found
     */
    @Transactional(readOnly = true)
    public SensorReadingDto findById(Long id) {
        return toDto(readingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SensorReading", id)));
    }

    /**
     * Records a new sensor reading and triggers rule evaluation.
     *
     * @param dto reading data with a valid {@code sensorId}
     * @return the persisted reading as a DTO
     * @throws ResourceNotFoundException if the sensor does not exist
     */
    public SensorReadingDto create(SensorReadingDto dto) {
        Sensor sensor = sensorService.getOrThrow(dto.sensorId());
        SensorReading entity = new SensorReading();
        entity.setSensor(sensor);
        entity.setValue(dto.value());
        entity.setUnit(dto.unit());
        entity.setRecordedAt(dto.recordedAt());
        SensorReading saved = readingRepository.save(entity);
        ruleService.evaluateRulesForSensor(sensor, dto.value());
        return toDto(saved);
    }

    /**
     * Deletes a sensor reading.
     *
     * @param id the reading ID to delete
     * @throws ResourceNotFoundException if not found
     */
    public void delete(Long id) {
        if (!readingRepository.existsById(id)) {
            throw new ResourceNotFoundException("SensorReading", id);
        }
        readingRepository.deleteById(id);
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private SensorReadingDto toDto(SensorReading r) {
        return new SensorReadingDto(
                r.getId(),
                r.getValue(),
                r.getUnit(),
                r.getRecordedAt(),
                r.getSensor().getId(),
                r.getSensor().getName()
        );
    }
}
