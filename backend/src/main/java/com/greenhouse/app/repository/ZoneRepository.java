package com.greenhouse.app.repository;

import com.greenhouse.app.entity.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link Zone} entities.
 */
@Repository
public interface ZoneRepository extends JpaRepository<Zone, Long> {

    /**
     * Returns all zones belonging to a specific greenhouse.
     *
     * @param greenhouseId the greenhouse primary key
     * @return list of zones for that greenhouse
     */
    List<Zone> findByGreenhouseId(Long greenhouseId);
}
