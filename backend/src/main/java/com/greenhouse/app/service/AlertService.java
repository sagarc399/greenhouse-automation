package com.greenhouse.app.service;

import com.greenhouse.app.dto.AlertDto;
import com.greenhouse.app.entity.Alert;
import com.greenhouse.app.entity.Alert.AlertStatus;
import com.greenhouse.app.entity.AutomationRule;
import com.greenhouse.app.entity.Sensor;
import com.greenhouse.app.exception.ResourceNotFoundException;
import com.greenhouse.app.repository.AlertRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer for {@link Alert} business logic.
 *
 * <p>Alerts are created automatically by {@link AutomationRuleService} or
 * manually via the REST API. Operators can update the status to
 * {@link AlertStatus#ATENDIDA}.</p>
 */
@Service
@Transactional
public class AlertService {

    /** JPA repository for alert persistence operations. */
    private final AlertRepository alertRepository;

    /** Service used to load {@link com.greenhouse.app.entity.Sensor} entities by ID. */
    private final SensorService sensorService;

    /**
     * Constructs the service with required dependencies.
     *
     * @param alertRepository JPA repository for alerts
     * @param sensorService   service to load sensor entities
     */
    public AlertService(AlertRepository alertRepository, SensorService sensorService) {
        this.alertRepository = alertRepository;
        this.sensorService = sensorService;
    }

    /**
     * Returns all alerts as DTOs.
     *
     * @return list of all alerts
     */
    @Transactional(readOnly = true)
    public List<AlertDto> findAll() {
        return alertRepository.findAll().stream().map(this::toDto).toList();
    }

    /**
     * Returns all pending alerts.
     *
     * @return list of alerts with status {@link AlertStatus#PENDIENTE}
     */
    @Transactional(readOnly = true)
    public List<AlertDto> findPending() {
        return alertRepository.findByStatus(AlertStatus.PENDIENTE).stream().map(this::toDto).toList();
    }

    /**
     * Returns all alerts for a specific sensor.
     *
     * @param sensorId the sensor primary key
     * @return list of alerts
     */
    @Transactional(readOnly = true)
    public List<AlertDto> findBySensor(Long sensorId) {
        return alertRepository.findBySensorId(sensorId).stream().map(this::toDto).toList();
    }

    /**
     * Finds a single alert by primary key.
     *
     * @param id the alert ID
     * @return the alert DTO
     * @throws ResourceNotFoundException if not found
     */
    @Transactional(readOnly = true)
    public AlertDto findById(Long id) {
        return toDto(getOrThrow(id));
    }

    /**
     * Creates a new alert manually.
     *
     * @param dto alert data with a valid {@code sensorId}
     * @return the persisted alert as a DTO
     * @throws ResourceNotFoundException if the sensor does not exist
     */
    public AlertDto create(AlertDto dto) {
        Sensor sensor = sensorService.getOrThrow(dto.sensorId());
        Alert entity = buildAlert(dto.message(), dto.severity(), dto.triggerValue(), sensor, null);
        return toDto(alertRepository.save(entity));
    }

    /**
     * Creates an alert automatically from an automation rule trigger.
     *
     * @param message  descriptive alert message
     * @param severity alert urgency level
     * @param value    the sensor reading value that triggered the rule
     * @param sensor   the sensor that produced the anomalous reading
     * @param rule     the automation rule that fired
     * @return the persisted alert
     */
    public Alert createFromRule(String message, Alert.AlertSeverity severity, double value,
                                Sensor sensor, AutomationRule rule) {
        Alert entity = buildAlert(message, severity, value, sensor, rule);
        return alertRepository.save(entity);
    }

    /**
     * Updates the status of an alert (e.g., mark as attended).
     *
     * @param id     the alert ID
     * @param status the new status
     * @return the updated alert as a DTO
     * @throws ResourceNotFoundException if not found
     */
    public AlertDto updateStatus(Long id, AlertStatus status) {
        Alert entity = getOrThrow(id);
        entity.setStatus(status);
        return toDto(alertRepository.save(entity));
    }

    /**
     * Deletes an alert.
     *
     * @param id the alert ID to delete
     * @throws ResourceNotFoundException if not found
     */
    public void delete(Long id) {
        if (!alertRepository.existsById(id)) {
            throw new ResourceNotFoundException("Alert", id);
        }
        alertRepository.deleteById(id);
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private Alert getOrThrow(Long id) {
        return alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert", id));
    }

    private Alert buildAlert(String message, Alert.AlertSeverity severity, Double triggerValue,
                             Sensor sensor, AutomationRule rule) {
        Alert a = new Alert();
        a.setMessage(message);
        a.setSeverity(severity);
        a.setTriggerValue(triggerValue);
        a.setSensor(sensor);
        a.setAutomationRule(rule);
        a.setStatus(AlertStatus.PENDIENTE);
        return a;
    }

    private AlertDto toDto(Alert a) {
        return new AlertDto(
                a.getId(),
                a.getMessage(),
                a.getSeverity(),
                a.getStatus(),
                a.getSensor().getId(),
                a.getSensor().getName(),
                a.getAutomationRule() != null ? a.getAutomationRule().getId() : null,
                a.getAutomationRule() != null ? a.getAutomationRule().getName() : null,
                a.getTriggerValue(),
                a.getCreatedAt(),
                a.getUpdatedAt()
        );
    }
}
