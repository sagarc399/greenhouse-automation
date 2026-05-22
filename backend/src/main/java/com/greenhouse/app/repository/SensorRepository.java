package com.greenhouse.app.repository;

import com.greenhouse.app.entity.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link Sensor} entities.
 */
@Repository
public interface SensorRepository extends JpaRepository<Sensor, Long> {

    /**
     * Returns all sensors installed in a specific zone.
     *
     * @param zoneId the zone primary key
     * @return list of sensors in that zone
     */
    List<Sensor> findByZoneId(Long zoneId);

    /**
     * Returns all active sensors installed in a specific zone.
     *
     * @param zoneId the zone primary key
     * @param active {@code true} for active sensors, {@code false} for inactive
     * @return filtered list of sensors
     */
    List<Sensor> findByZoneIdAndActive(Long zoneId, boolean active);

    /**
     * Counts sensors by type across all zones.
     *
     * @param type the sensor type to count
     * @return number of sensors with the given type
     */
    long countByType(Sensor.SensorType type);
}
