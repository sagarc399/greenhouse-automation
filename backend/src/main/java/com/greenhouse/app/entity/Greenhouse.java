package com.greenhouse.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a physical greenhouse facility.
 *
 * <p>A greenhouse is the top-level container that holds one or more {@link Zone}s.
 * Each zone groups sensors, actuators, and automation rules.</p>
 *
 * @see Zone
 */
@Entity
@Table(name = "greenhouses")
@Getter
@Setter
@NoArgsConstructor
public class Greenhouse {

    /**
     * Auto-generated primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Human-readable name of the greenhouse (e.g., "Greenhouse A").
     */
    @NotBlank
    @Column(nullable = false, length = 150)
    private String name;

    /**
     * Optional physical location description (city, address, GPS coordinates, etc.).
     */
    @Column(length = 255)
    private String location;

    /**
     * Detailed description of the greenhouse purpose or crop type.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Timestamp when this record was created.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp of the last update to this record.
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Zones contained within this greenhouse.
     */
    @OneToMany(mappedBy = "greenhouse", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Zone> zones = new ArrayList<>();

    /**
     * Sets audit timestamps before persisting.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * Updates the audit timestamp before merging.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
