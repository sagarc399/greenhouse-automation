package com.greenhouse.app.repository;

import com.greenhouse.app.entity.Actuator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link Actuator} entities.
 */
@Repository
public interface ActuatorRepository extends JpaRepository<Actuator, Long> {

    /**
     * Returns all actuators installed in a specific zone.
     *
     * @param zoneId the zone primary key
     * @return list of actuators in that zone
     */
    List<Actuator> findByZoneId(Long zoneId);

    /**
     * Returns all actuators currently in a given state.
     *
     * @param state the actuator state to filter by
     * @return list of actuators with that state
     */
    List<Actuator> findByState(Actuator.ActuatorState state);
}
