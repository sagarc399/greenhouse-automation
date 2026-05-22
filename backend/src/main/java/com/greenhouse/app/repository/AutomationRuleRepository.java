package com.greenhouse.app.repository;

import com.greenhouse.app.entity.AutomationRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link AutomationRule} entities.
 */
@Repository
public interface AutomationRuleRepository extends JpaRepository<AutomationRule, Long> {

    /**
     * Returns all automation rules configured for a specific zone.
     *
     * @param zoneId the zone primary key
     * @return list of rules for that zone
     */
    List<AutomationRule> findByZoneId(Long zoneId);

    /**
     * Returns active rules that monitor a specific sensor.
     *
     * @param sensorId the sensor primary key
     * @param active   {@code true} to retrieve only active rules
     * @return list of matching rules
     */
    List<AutomationRule> findBySensorIdAndActive(Long sensorId, boolean active);
}
