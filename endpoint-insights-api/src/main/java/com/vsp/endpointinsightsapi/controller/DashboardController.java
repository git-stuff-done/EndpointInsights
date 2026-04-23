package com.vsp.endpointinsightsapi.controller;

import com.vsp.endpointinsightsapi.authentication.RequiredRoles;
import com.vsp.endpointinsightsapi.dto.DashboardSummaryResponseDTO;
import com.vsp.endpointinsightsapi.dto.DashboardTestActivityDTO;
import com.vsp.endpointinsightsapi.dto.charts.ChartResponseDTO;
import com.vsp.endpointinsightsapi.model.enums.UserRole;
import com.vsp.endpointinsightsapi.service.DashboardService;
import com.vsp.endpointinsightsapi.service.PerformanceChartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Dashboard data, summaries, and analytics endpoints")
public class DashboardController {

    private final DashboardService dashboardService;
    private final PerformanceChartService performanceChartService;

    public DashboardController(DashboardService dashboardService, PerformanceChartService performanceChartService) {
        this.dashboardService = dashboardService;
        this.performanceChartService = performanceChartService;
    }

    @PostMapping("/summary")
    @RequiredRoles(roles = {UserRole.WRITE})
    @Operation(summary = "Calculate dashboard summary", description = "Calculates aggregated summary statistics for test activities")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Summary calculated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<DashboardSummaryResponseDTO> summary(@RequestBody List<DashboardTestActivityDTO> tests) {
        return ResponseEntity.ok(dashboardService.calculate(tests));
    }


    @GetMapping("/charts/performance")
    @Operation(summary = "Get API performance chart data", description = "Retrieves performance metrics and chart data for APIs, optionally filtered by job or batch")
    @RequiredRoles(roles = {UserRole.READ})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chart data retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters - cannot provide both jobId and batchId"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ChartResponseDTO> getApiPerformanceChart(
            @Parameter(description = "Filter by job ID")
            @RequestParam(required = false) UUID jobId,
            @Parameter(description = "Filter by batch ID")
            @RequestParam(required = false) UUID batchId,
            @Parameter(description = "Number of data points to retrieve", example = "10")
            @RequestParam(defaultValue = "10") int limit) {

        if (jobId != null && batchId != null) {
            throw new IllegalArgumentException("Provide only one of jobId or batchId");
        }

        return ResponseEntity.ok(
                performanceChartService.getApiPerformanceChart(jobId, batchId, limit)
        );
    }
}