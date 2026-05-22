package com.greenhouse.app.repository;

import com.greenhouse.app.entity.Greenhouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link Greenhouse} entities.
 *
 * <p>Provides standard CRUD operations inherited from {@link JpaRepository}
 * plus greenhouse-specific query methods.</p>
 */
@Repository
public interface GreenhouseRepository extends JpaRepository<Greenhouse, Long> {

    /**
     * Finds all greenhouses whose name contains the given string (case-insensitive).
     *
     * @param name partial or full greenhouse name to search
     * @return list of matching greenhouses
     */
    List<Greenhouse> findByNameContainingIgnoreCase(String name);
}
