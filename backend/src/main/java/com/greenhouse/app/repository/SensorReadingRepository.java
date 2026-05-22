package com.greenhouse.app.repository;

import com.greenhouse.app.entity.SensorReading;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link SensorReading} entities.
 */
@Repository
public interface SensorReadingRepository extends JpaRepository<SensorReading, Long> {

    /**
     * Returns paginated readings for a specific sensor, ordered by timestamp descending.
     *
     * @param sensorId the sensor primary key
     * @param pageable pagination parameters
     * @return page of readings
     */
    Page<SensorReading> findBySensorIdOrderByRecordedAtDesc(Long sensorId, Pageable pageable);

    /**
     * Returns readings for a sensor within a time range.
     *
     * @param sensorId  the sensor primary key
     * @param from      start of the time range (inclusive)
     * @param to        end of the time range (inclusive)
     * @return list of readings in range
     */
    List<SensorReading> findBySensorIdAndRecordedAtBetween(Long sensorId, LocalDateTime from, LocalDateTime to);

    /**
     * Returns the most recent reading for a sensor.
     *
     * @param sensorId the sensor primary key
     * @return optional containing the latest reading, or empty if none exist
     */
    Optional<SensorReading> findTopBySensorIdOrderByRecordedAtDesc(Long sensorId);
}
