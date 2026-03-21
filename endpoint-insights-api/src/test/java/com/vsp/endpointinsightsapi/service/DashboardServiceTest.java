package com.vsp.endpointinsightsapi.service;

import com.vsp.endpointinsightsapi.dto.DashboardSummaryResponseDTO;
import com.vsp.endpointinsightsapi.dto.DashboardTestActivityDTO;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static com.vsp.endpointinsightsapi.dto.DashboardTestActivityDTO.Status.FAIL;
import static com.vsp.endpointinsightsapi.dto.DashboardTestActivityDTO.Status.PASS;
import static org.junit.jupiter.api.Assertions.*;

class DashboardServiceTest {

    private final DashboardService service = new DashboardService();

    @Test
    void calculate_returnsZeros_whenNullOrEmpty() {
        DashboardSummaryResponseDTO res1 = service.calculate(null);
        assertEquals(0, res1.totalRuns());
        assertEquals(0, res1.passedRuns());
        assertEquals(0, res1.failedRuns());
        assertEquals(0.0, res1.passRate());
        assertEquals(0.0, res1.avgDurationMs());

        DashboardSummaryResponseDTO res2 = service.calculate(List.of());
        assertEquals(0, res2.totalRuns());
    }

    @Test
    void calculate_computesTotals_passRate_avgDuration_andByStatus() {
        List<DashboardTestActivityDTO> input = List.of(
                new DashboardTestActivityDTO("a", "Vision API", "Daily",
                        LocalDate.parse("2025-07-10"), 230, "J. Brock", PASS),
                new DashboardTestActivityDTO("b", "Services API", "N/A",
                        LocalDate.parse("2025-07-10"), 20, "F. Zappa", PASS),
                new DashboardTestActivityDTO("c", "Something", "Daily",
                        LocalDate.parse("2025-07-09"), 50, "X", FAIL)
        );

        DashboardSummaryResponseDTO res = service.calculate(input);

        assertEquals(3, res.totalRuns());
        assertEquals(2, res.passedRuns());
        assertEquals(1, res.failedRuns());
        assertEquals(2.0 / 3.0, res.passRate(), 1e-9);
        assertEquals((230 + 20 + 50) / 3.0, res.avgDurationMs(), 1e-9);

        assertEquals(2L, res.byStatus().get(PASS));
        assertEquals(1L, res.byStatus().get(FAIL));

        // recent activity should be sorted by dateRun desc (newest first) if you implemented that
        assertFalse(res.recentActivity().isEmpty());
        assertEquals(LocalDate.parse("2025-07-10"), res.recentActivity().get(0).dateRun());
    }
}
