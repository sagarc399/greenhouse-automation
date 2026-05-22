package com.greenhouse.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity representing a physical actuator device installed in a {@link Zone}.
 *
 * <p>Actuators are controllable devices (valves, fans, heaters, lights) that
 * can be toggled by {@link AutomationRule}s or manually via the API.</p>
 *
 * @see Zone
 * @see AutomationRule
 * @see ActuatorType
 * @see ActuatorState
 */
@Entity
@Table(name = "actuators")
@Getter
@Setter
@NoArgsConstructor
public class Actuator {

    /**
     * Auto-generated primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Human-readable label for this actuator (e.g., "Main Irrigation Valve").
     */
    @NotBlank
    @Column(nullable = false, length = 150)
    private String name;

    /**
     * The functional type of this actuator.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ActuatorType type;

    /**
     * Current operational state of the actuator.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ActuatorState state = ActuatorState.APAGADO;

    /**
     * The zone where this actuator is installed.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

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
     * Supported actuator device types.
     */
    public enum ActuatorType {
        /** Irrigation valve or drip system. */
        RIEGO,
        /** Ventilation fan or louver. */
        VENTILACION,
        /** Artificial lighting system. */
        ILUMINACION,
        /** Heating unit or heat mat. */
        CALEFACCION
    }

    /**
     * Possible operational states for an actuator.
     */
    public enum ActuatorState {
        /** Actuator is currently running / on. */
        ENCENDIDO,
        /** Actuator is currently stopped / off. */
        APAGADO
    }
}
