package com.greenhouse.app.service;

import com.greenhouse.app.dto.AutomationRuleDto;
import com.greenhouse.app.entity.Alert;
import com.greenhouse.app.entity.Actuator;
import com.greenhouse.app.entity.AutomationRule;
import com.greenhouse.app.entity.Sensor;
import com.greenhouse.app.entity.Zone;
import com.greenhouse.app.exception.ResourceNotFoundException;
import com.greenhouse.app.repository.ActuatorRepository;
import com.greenhouse.app.repository.AutomationRuleRepository;
import com.greenhouse.app.repository.SensorRepository;
import com.greenhouse.app.repository.ZoneRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer for {@link AutomationRule} business logic.
 *
 * <p>After a {@link com.greenhouse.app.entity.SensorReading} is persisted,
 * {@link #evaluateRulesForSensor(Sensor, double)} is called to check all
 * active rules associated with that sensor. Matching rules may toggle
 * an actuator and/or generate an {@link Alert}.</p>
 */
@Service
@Transactional
public class AutomationRuleService {

    /** JPA repository for {@link AutomationRule} persistence operations. */
    private final AutomationRuleRepository ruleRepository;

    /** JPA repository used to look up {@link Zone} entities when applying DTOs. */
    private final ZoneRepository zoneRepository;

    /** JPA repository used to look up {@link com.greenhouse.app.entity.Sensor} entities when applying DTOs. */
    private final SensorRepository sensorRepository;

    /** JPA repository used to persist actuator state changes triggered by rules. */
    private final ActuatorRepository actuatorRepository;

    /** Service used to create {@link com.greenhouse.app.entity.Alert}s when a rule fires. */
    private final AlertService alertService;

    /**
     * Constructs the service with required dependencies.
     * {@code AlertService} is injected lazily to avoid a circular dependency
     * (AlertService → SensorService → AutomationRuleService → AlertService).
     *
     * @param ruleRepository     JPA repository for automation rules
     * @param zoneRepository     JPA repository for zones
     * @param sensorRepository   JPA repository for sensors
     * @param actuatorRepository JPA repository for actuators
     * @param alertService       service for creating alerts (lazy to break cycle)
     */
    public AutomationRuleService(AutomationRuleRepository ruleRepository,
                                 ZoneRepository zoneRepository,
                                 SensorRepository sensorRepository,
                                 ActuatorRepository actuatorRepository,
                                 @Lazy AlertService alertService) {
        this.ruleRepository = ruleRepository;
        this.zoneRepository = zoneRepository;
        this.sensorRepository = sensorRepository;
        this.actuatorRepository = actuatorRepository;
        this.alertService = alertService;
    }

    /**
     * Returns all automation rules as DTOs.
     *
     * @return list of all rules
     */
    @Transactional(readOnly = true)
    public List<AutomationRuleDto> findAll() {
        return ruleRepository.findAll().stream().map(this::toDto).toList();
    }

    /**
     * Returns all rules for a specific zone.
     *
     * @param zoneId the zone primary key
     * @return list of rules for that zone
     */
    @Transactional(readOnly = true)
    public List<AutomationRuleDto> findByZone(Long zoneId) {
        return ruleRepository.findByZoneId(zoneId).stream().map(this::toDto).toList();
    }

    /**
     * Finds a single rule by primary key.
     *
     * @param id the rule ID
     * @return the rule DTO
     * @throws ResourceNotFoundException if not found
     */
    @Transactional(readOnly = true)
    public AutomationRuleDto findById(Long id) {
        return toDto(getOrThrow(id));
    }

    /**
     * Creates a new automation rule.
     *
     * @param dto rule data
     * @return the persisted rule as a DTO
     * @throws ResourceNotFoundException if zone, sensor, or actuator not found
     */
    public AutomationRuleDto create(AutomationRuleDto dto) {
        AutomationRule entity = new AutomationRule();
        applyDto(entity, dto);
        return toDto(ruleRepository.save(entity));
    }

    /**
     * Updates an existing automation rule.
     *
     * @param id  the rule ID to update
     * @param dto new data to apply
     * @return the updated rule as a DTO
     * @throws ResourceNotFoundException if not found
     */
    public AutomationRuleDto update(Long id, AutomationRuleDto dto) {
        AutomationRule entity = getOrThrow(id);
        applyDto(entity, dto);
        return toDto(ruleRepository.save(entity));
    }

    /**
     * Deletes an automation rule.
     *
     * @param id the rule ID to delete
     * @throws ResourceNotFoundException if not found
     */
    public void delete(Long id) {
        if (!ruleRepository.existsById(id)) {
            throw new ResourceNotFoundException("AutomationRule", id);
        }
        ruleRepository.deleteById(id);
    }

    /**
     * Evaluates all active rules that monitor the given sensor.
     *
     * <p>For each rule whose condition is satisfied:</p>
     * <ol>
     *   <li>If the rule has a linked actuator, its state is updated.</li>
     *   <li>An alert is generated to notify operators.</li>
     * </ol>
     *
     * @param sensor      the sensor that produced a new reading
     * @param sensorValue the latest reading value
     */
    public void evaluateRulesForSensor(Sensor sensor, double sensorValue) {
        List<AutomationRule> activeRules = ruleRepository.findBySensorIdAndActive(sensor.getId(), true);
        for (AutomationRule rule : activeRules) {
            if (rule.evaluate(sensorValue)) {
                if (rule.getActuator() != null && rule.getActuatorTargetState() != null) {
                    Actuator actuator = rule.getActuator();
                    actuator.setState(rule.getActuatorTargetState());
                    actuatorRepository.save(actuator);
                }
                String message = String.format(
                        "Rule '%s' triggered: sensor '%s' value %.2f %s %.2f",
                        rule.getName(), sensor.getName(), sensorValue,
                        rule.getOperator().name(), rule.getThreshold()
                );
                alertService.createFromRule(message, Alert.AlertSeverity.MEDIA, sensorValue, sensor, rule);
            }
        }
    }

    // ── private helpers ──────────────────────────────────────────────────────

    /**
     * Loads an {@link AutomationRule} by primary key or throws if absent.
     *
     * @param id the rule primary key
     * @return the loaded entity
     * @throws ResourceNotFoundException if no rule exists with the given id
     */
    private AutomationRule getOrThrow(Long id) {
        return ruleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AutomationRule", id));
    }

    /**
     * Maps fields from a {@link AutomationRuleDto} onto an existing or new
     * {@link AutomationRule} entity, resolving all referenced entities.
     *
     * @param entity the entity to mutate (new or loaded from the database)
     * @param dto    the source data transfer object
     * @throws ResourceNotFoundException if the referenced zone, sensor, or actuator is not found
     */
    private void applyDto(AutomationRule entity, AutomationRuleDto dto) {
        Zone zone = zoneRepository.findById(dto.zoneId())
                .orElseThrow(() -> new ResourceNotFoundException("Zone", dto.zoneId()));
        Sensor sensor = sensorRepository.findById(dto.sensorId())
                .orElseThrow(() -> new ResourceNotFoundException("Sensor", dto.sensorId()));
        entity.setName(dto.name());
        entity.setZone(zone);
        entity.setSensor(sensor);
        entity.setOperator(dto.operator());
        entity.setThreshold(dto.threshold());
        entity.setActive(dto.active());
        entity.setActuatorTargetState(dto.actuatorTargetState());
        if (dto.actuatorId() != null) {
            Actuator actuator = actuatorRepository.findById(dto.actuatorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Actuator", dto.actuatorId()));
            entity.setActuator(actuator);
        } else {
            entity.setActuator(null);
        }
    }

    /**
     * Converts an {@link AutomationRule} entity to its {@link AutomationRuleDto} representation.
     *
     * @param r the entity to convert
     * @return an immutable DTO populated from the entity and its lazy-loaded associations
     */
    private AutomationRuleDto toDto(AutomationRule r) {
        return new AutomationRuleDto(
                r.getId(),
                r.getName(),
                r.getZone().getId(),
                r.getSensor().getId(),
                r.getSensor().getName(),
                r.getOperator(),
                r.getThreshold(),
                r.getActuator() != null ? r.getActuator().getId() : null,
                r.getActuator() != null ? r.getActuator().getName() : null,
                r.getActuatorTargetState(),
                r.isActive(),
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }
}
