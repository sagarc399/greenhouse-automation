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
 * Entity representing a logical zone inside a {@link Greenhouse}.
 *
 * <p>A zone groups related sensors, actuators, and automation rules.
 * For example, a greenhouse might have separate zones for seedlings,
 * mature plants, and propagation areas.</p>
 *
 * @see Greenhouse
 * @see Sensor
 * @see Actuator
 * @see AutomationRule
 */
@Entity
@Table(name = "zones")
@Getter
@Setter
@NoArgsConstructor
public class Zone {

    /**
     * Auto-generated primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Human-readable name of the zone (e.g., "Zone 1 - Seedlings").
     */
    @NotBlank
    @Column(nullable = false, length = 150)
    private String name;

    /**
     * Optional description of the zone (crop type, purpose, etc.).
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * The greenhouse this zone belongs to.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "greenhouse_id", nullable = false)
    private Greenhouse greenhouse;

    /**
     * Sensors installed in this zone.
     */
    @OneToMany(mappedBy = "zone", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Sensor> sensors = new ArrayList<>();

    /**
     * Actuators controlled in this zone.
     */
    @OneToMany(mappedBy = "zone", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Actuator> actuators = new ArrayList<>();

    /**
     * Automation rules configured for this zone.
     */
    @OneToMany(mappedBy = "zone", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AutomationRule> automationRules = new ArrayList<>();

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
