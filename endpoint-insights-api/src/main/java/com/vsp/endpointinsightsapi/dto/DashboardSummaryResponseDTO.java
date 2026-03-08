package com.vsp.endpointinsightsapi.dto;

import java.util.List;
import java.util.Map;

public record DashboardSummaryResponseDTO(
        long totalRuns,
        long passedRuns,
        long failedRuns,
        double passRate,
        double avgDurationMs,
        Map<DashboardTestActivityDTO.Status, Long> byStatus,
        List<DashboardTestActivityDTO> recentActivity
) {}
