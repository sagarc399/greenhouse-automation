package com.greenhouse.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a physical or virtual sensor installed in a {@link Zone}.
 *
 * <p>Sensors continuously produce {@link SensorReading}s and can trigger
 * {@link Alert}s when readings breach configured thresholds defined by
 * {@link AutomationRule}s.</p>
 *
 * @see Zone
 * @see SensorReading
 * @see Alert
 * @see SensorType
 */
@Entity
@Table(name = "sensors")
@Getter
@Setter
@NoArgsConstructor
public class Sensor {

    /**
     * Auto-generated primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Human-readable label for this sensor (e.g., "Temp Sensor 1").
     */
    @NotBlank
    @Column(nullable = false, length = 150)
    private String name;

    /**
     * The measurement type this sensor captures.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SensorType type;

    /**
     * Optional model or serial number for hardware identification.
     */
    @Column(length = 100)
    private String model;

    /**
     * Whether the sensor is currently active and producing readings.
     */
    @Column(nullable = false)
    private boolean active = true;

    /**
     * The zone where this sensor is installed.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    /**
     * Historical readings captured by this sensor.
     */
    @OneToMany(mappedBy = "sensor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SensorReading> readings = new ArrayList<>();

    /**
     * Alerts generated from anomalies detected on this sensor.
     */
    @OneToMany(mappedBy = "sensor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Alert> alerts = new ArrayList<>();

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

    /**
     * Supported sensor measurement types.
     */
    public enum SensorType {
        /** Ambient temperature sensor. */
        TEMPERATURA,
        /** Ambient air humidity sensor. */
        HUMEDAD_AMBIENTAL,
        /** Soil moisture sensor. */
        HUMEDAD_SUELO,
        /** Light intensity sensor. */
        LUZ,
        /** Soil pH sensor. */
        PH
    }
}
