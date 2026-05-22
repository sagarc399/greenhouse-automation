package com.greenhouse.app.controller;

import com.greenhouse.app.dto.DashboardDto;
import com.greenhouse.app.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for the system dashboard.
 *
 * <p>Returns aggregated metrics (greenhouse count, active sensors, pending alerts, etc.)
 * for the dashboard view.</p>
 */
@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "System-wide summary metrics")
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Constructs the controller with its required service.
     *
     * @param dashboardService service that computes dashboard metrics
     */
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Returns a summary of system state for the dashboard.
     *
     * @return HTTP 200 with a {@link DashboardDto}
     */
    @GetMapping
    @Operation(
        summary = "Get dashboard summary",
        description = "Returns aggregated metrics: greenhouse count, active sensors, running actuators, pending alerts."
    )
    public ResponseEntity<DashboardDto> getDashboard() {
        return ResponseEntity.ok(dashboardService.getDashboard());
    }
}
