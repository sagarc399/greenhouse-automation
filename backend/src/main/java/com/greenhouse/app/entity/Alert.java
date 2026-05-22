package com.greenhouse.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity representing an alert generated when a sensor reading breaches a threshold
 * defined by an {@link AutomationRule}.
 *
 * <p>Alerts have a {@link AlertSeverity} indicating urgency and an
 * {@link AlertStatus} tracking whether an operator has acknowledged them.</p>
 *
 * @see Sensor
 * @see AutomationRule
 * @see AlertSeverity
 * @see AlertStatus
 */
@Entity
@Table(name = "alerts", indexes = {
    @Index(name = "idx_alerts_sensor_status", columnList = "sensor_id, status")
})
@Getter
@Setter
@NoArgsConstructor
public class Alert {

    /**
     * Auto-generated primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Short message describing what triggered the alert.
     */
    @NotBlank
    @Column(nullable = false, length = 300)
    private String message;

    /**
     * Urgency level of this alert.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AlertSeverity severity;

    /**
     * Current handling status of this alert.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private AlertStatus status = AlertStatus.PENDIENTE;

    /**
     * The sensor whose anomalous reading triggered this alert.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sensor_id", nullable = false)
    private Sensor sensor;

    /**
     * The automation rule that generated this alert, if applicable.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "automation_rule_id")
    private AutomationRule automationRule;

    /**
     * The sensor reading value that triggered this alert.
     */
    @Column
    private Double triggerValue;

    /**
     * Timestamp when this alert was generated.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when an operator last updated this alert.
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
     * Severity levels for alerts.
     */
    public enum AlertSeverity {
        /** Low priority — informational only. */
        BAJA,
        /** Medium priority — should be reviewed soon. */
        MEDIA,
        /** High priority — requires prompt attention. */
        ALTA,
        /** Critical — immediate action required. */
        CRITICA
    }

    /**
     * Lifecycle statuses for alerts.
     */
    public enum AlertStatus {
        /** Alert has been generated but not yet reviewed. */
        PENDIENTE,
        /** Alert has been reviewed and handled by an operator. */
        ATENDIDA
    }
}
