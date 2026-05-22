package com.greenhouse.app.repository;

import com.greenhouse.app.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link Alert} entities.
 */
@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    /**
     * Returns all alerts linked to a specific sensor.
     *
     * @param sensorId the sensor primary key
     * @return list of alerts for that sensor
     */
    List<Alert> findBySensorId(Long sensorId);

    /**
     * Returns all alerts with a given status.
     *
     * @param status the alert status to filter by
     * @return list of alerts with that status
     */
    List<Alert> findByStatus(Alert.AlertStatus status);

    /**
     * Returns all alerts with a given severity.
     *
     * @param severity the alert severity to filter by
     * @return list of alerts with that severity
     */
    List<Alert> findBySeverity(Alert.AlertSeverity severity);

    /**
     * Counts pending alerts for a specific sensor.
     *
     * @param sensorId the sensor primary key
     * @param status   the status to count
     * @return number of alerts matching the criteria
     */
    long countBySensorIdAndStatus(Long sensorId, Alert.AlertStatus status);
}
