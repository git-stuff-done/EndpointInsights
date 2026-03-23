package com.vsp.endpointinsightsapi.controller;

import com.vsp.endpointinsightsapi.dto.DashboardSummaryResponseDTO;
import com.vsp.endpointinsightsapi.dto.DashboardTestActivityDTO;
import com.vsp.endpointinsightsapi.dto.charts.ChartResponseDTO;
import com.vsp.endpointinsightsapi.service.DashboardService;
import com.vsp.endpointinsightsapi.service.PerformanceChartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final PerformanceChartService performanceChartService;

    public DashboardController(DashboardService dashboardService, PerformanceChartService performanceChartService) {
        this.dashboardService = dashboardService;
        this.performanceChartService = performanceChartService;
    }

    @PostMapping("/summary")
    public ResponseEntity<DashboardSummaryResponseDTO> summary(@RequestBody List<DashboardTestActivityDTO> tests) {
        return ResponseEntity.ok(dashboardService.calculate(tests));
    }


    @GetMapping("/charts/performance")
    public ResponseEntity<ChartResponseDTO> getApiPerformanceChart(
            @RequestParam(required = false) UUID jobId,
            @RequestParam(required = false) UUID batchId,
            @RequestParam(defaultValue = "10") int limit) {

        if (jobId != null && batchId != null) {
            throw new IllegalArgumentException("Provide only one of jobId or batchId");
        }

        return ResponseEntity.ok(
                performanceChartService.getApiPerformanceChart(jobId, batchId, limit)
        );
    }
}