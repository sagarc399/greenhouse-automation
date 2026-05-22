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

    private final AutomationRuleRepository ruleRepository;
    private final ZoneRepository zoneRepository;
    private final SensorRepository sensorRepository;
    private final ActuatorRepository actuatorRepository;
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
     * <p>For each rule whose condition is satisfied:
     * <ol>
     *   <li>If the rule has a linked actuator, its state is updated.</li>
     *   <li>An alert is generated to notify operators.</li>
     * </ol>
     * </p>
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

    private AutomationRule getOrThrow(Long id) {
        return ruleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AutomationRule", id));
    }

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
