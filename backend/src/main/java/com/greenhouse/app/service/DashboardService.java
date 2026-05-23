package com.greenhouse.app.service;

import com.greenhouse.app.dto.DashboardDto;
import com.greenhouse.app.entity.Alert.AlertSeverity;
import com.greenhouse.app.entity.Alert.AlertStatus;
import com.greenhouse.app.repository.ActuatorRepository;
import com.greenhouse.app.repository.AlertRepository;
import com.greenhouse.app.repository.AutomationRuleRepository;
import com.greenhouse.app.repository.GreenhouseRepository;
import com.greenhouse.app.repository.SensorRepository;
import com.greenhouse.app.repository.ZoneRepository;
import com.greenhouse.app.entity.Actuator.ActuatorState;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service that aggregates system-wide metrics for the dashboard view.
 */
@Service
@Transactional(readOnly = true)
public class DashboardService {

    /** JPA repository providing greenhouse counts. */
    private final GreenhouseRepository greenhouseRepository;

    /** JPA repository providing zone counts. */
    private final ZoneRepository zoneRepository;

    /** JPA repository providing sensor counts and active-sensor queries. */
    private final SensorRepository sensorRepository;

    /** JPA repository providing actuator state queries. */
    private final ActuatorRepository actuatorRepository;

    /** JPA repository providing automation rule counts. */
    private final AutomationRuleRepository ruleRepository;

    /** JPA repository providing alert counts and severity queries. */
    private final AlertRepository alertRepository;

    /**
     * Constructs the service with all required repositories.
     *
     * @param greenhouseRepository  repository for greenhouses
     * @param zoneRepository        repository for zones
     * @param sensorRepository      repository for sensors
     * @param actuatorRepository    repository for actuators
     * @param ruleRepository        repository for automation rules
     * @param alertRepository       repository for alerts
     */
    public DashboardService(GreenhouseRepository greenhouseRepository,
                            ZoneRepository zoneRepository,
                            SensorRepository sensorRepository,
                            ActuatorRepository actuatorRepository,
                            AutomationRuleRepository ruleRepository,
                            AlertRepository alertRepository) {
        this.greenhouseRepository = greenhouseRepository;
        this.zoneRepository = zoneRepository;
        this.sensorRepository = sensorRepository;
        this.actuatorRepository = actuatorRepository;
        this.ruleRepository = ruleRepository;
        this.alertRepository = alertRepository;
    }

    /**
     * Builds and returns a dashboard summary DTO.
     *
     * @return aggregated metrics for the system dashboard
     */
    public DashboardDto getDashboard() {
        return new DashboardDto(
                greenhouseRepository.count(),
                zoneRepository.count(),
                sensorRepository.findAll().stream().filter(s -> s.isActive()).count(),
                actuatorRepository.findByState(ActuatorState.ENCENDIDO).size(),
                ruleRepository.findAll().stream().filter(r -> r.isActive()).count(),
                alertRepository.findByStatus(AlertStatus.PENDIENTE).size(),
                alertRepository.findBySeverity(AlertSeverity.CRITICA).stream()
                        .filter(a -> a.getStatus() == AlertStatus.PENDIENTE).count()
        );
    }
}
