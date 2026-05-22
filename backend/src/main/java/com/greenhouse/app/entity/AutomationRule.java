package com.greenhouse.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity representing an automation rule that monitors a sensor and optionally
 * triggers an actuator when a condition is met.
 *
 * <p>When a {@link SensorReading} satisfies the rule's condition
 * ({@code sensorValue OPERATOR threshold}), the system can:
 * <ul>
 *   <li>Change the linked {@link Actuator} state.</li>
 *   <li>Create an {@link Alert} for operator review.</li>
 * </ul>
 * </p>
 *
 * @see Zone
 * @see Sensor
 * @see Actuator
 * @see RuleOperator
 */
@Entity
@Table(name = "automation_rules")
@Getter
@Setter
@NoArgsConstructor
public class AutomationRule {

    /**
     * Auto-generated primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Descriptive name for this rule (e.g., "High Temperature Alert").
     */
    @NotBlank
    @Column(nullable = false, length = 200)
    private String name;

    /**
     * The zone this rule belongs to.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    /**
     * The sensor whose readings are evaluated by this rule.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sensor_id", nullable = false)
    private Sensor sensor;

    /**
     * Comparison operator used to evaluate the condition.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private RuleOperator operator;

    /**
     * Threshold value against which sensor readings are compared.
     */
    @NotNull
    @Column(nullable = false)
    private Double threshold;

    /**
     * Optional actuator to toggle when the rule condition is met.
     * May be {@code null} if the rule only generates alerts.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actuator_id")
    private Actuator actuator;

    /**
     * Target state to set on the actuator when the rule triggers.
     * Ignored when {@code actuator} is {@code null}.
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Actuator.ActuatorState actuatorTargetState;

    /**
     * Whether this rule is currently active and being evaluated.
     */
    @Column(nullable = false)
    private boolean active = true;

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
     * Evaluates whether the given sensor value satisfies this rule's condition.
     *
     * @param sensorValue the latest reading from the monitored sensor
     * @return {@code true} if the condition is satisfied and the rule should trigger
     */
    public boolean evaluate(double sensorValue) {
        return switch (operator) {
            case MAYOR_QUE -> sensorValue > threshold;
            case MENOR_QUE -> sensorValue < threshold;
            case IGUAL_QUE -> Double.compare(sensorValue, threshold) == 0;
        };
    }

    /**
     * Comparison operators available for automation rule conditions.
     */
    public enum RuleOperator {
        /** Triggers when the sensor value is greater than the threshold. */
        MAYOR_QUE,
        /** Triggers when the sensor value is less than the threshold. */
        MENOR_QUE,
        /** Triggers when the sensor value equals the threshold. */
        IGUAL_QUE
    }
}
