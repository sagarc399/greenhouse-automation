package com.greenhouse.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity representing a single data point captured by a {@link Sensor}.
 *
 * <p>Readings are time-series records. Each record stores the numeric value
 * measured by the sensor at a specific instant in time.</p>
 *
 * @see Sensor
 */
@Entity
@Table(name = "sensor_readings", indexes = {
    @Index(name = "idx_readings_sensor_timestamp", columnList = "sensor_id, recorded_at DESC")
})
@Getter
@Setter
@NoArgsConstructor
public class SensorReading {

    /**
     * Auto-generated primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The numeric value captured by the sensor (e.g., 24.5 for 24.5°C).
     */
    @NotNull
    @Column(nullable = false)
    private Double value;

    /**
     * Optional unit of measurement (e.g., "°C", "%", "lux", "pH").
     */
    @Column(length = 20)
    private String unit;

    /**
     * Timestamp when the reading was captured.
     */
    @NotNull
    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    /**
     * The sensor that produced this reading.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sensor_id", nullable = false)
    private Sensor sensor;

    /**
     * Sets the recording timestamp to now before persisting if not already set.
     */
    @PrePersist
    protected void onCreate() {
        if (recordedAt == null) {
            recordedAt = LocalDateTime.now();
        }
    }
}
