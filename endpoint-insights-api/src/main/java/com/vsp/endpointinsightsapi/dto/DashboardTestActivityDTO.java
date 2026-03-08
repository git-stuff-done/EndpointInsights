package com.vsp.endpointinsightsapi.dto;

import java.time.LocalDate;

public record DashboardTestActivityDTO(
        String id,
        String testName,
        String group,
        LocalDate dateRun,     // frontend sends "2025-07-10"
        long durationMs,
        String startedBy,
        Status status
) {
    public enum Status { PASS, FAIL, SKIP, RUNNING }
}
