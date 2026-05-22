package com.greenhouse.app.dto;

/**
 * Aggregated data transfer object for the dashboard endpoint.
 *
 * <p>Provides a single-call summary of system state: counts and recent activity.</p>
 */
public record DashboardDto(
        long totalGreenhouses,
        long totalZones,
        long activeSensors,
        long activeActuators,
        long activeRules,
        long pendingAlerts,
        long criticalAlerts
) {}
