package com.vsp.endpointinsightsapi.service;

import com.vsp.endpointinsightsapi.dto.RecentActivityDTO;
import com.vsp.endpointinsightsapi.dto.charts.ChartPointDTO;
import com.vsp.endpointinsightsapi.dto.charts.ChartResponseDTO;
import com.vsp.endpointinsightsapi.dto.charts.ChartSeriesDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PerformanceChartServiceTest {

    @Mock
    private TestRunService testRunService;

    @InjectMocks
    private PerformanceChartService performanceChartService;

    private RecentActivityDTO buildActivity(
            String testName,
            String status,
            long durationMs,
            Instant dateRun
    ) {
        return RecentActivityDTO.builder()
                .runId(UUID.randomUUID().toString())
                .jobId(UUID.randomUUID().toString())
                .testName(testName)
                .group("Daily")
                .dateRun(dateRun)
                .durationMs(durationMs)
                .startedBy("tester")
                .status(status)
                .build();
    }

    @Test
    void getApiPerformanceChart_returnsEmptySeriesWhenNoActivities() {
        UUID jobId = UUID.randomUUID();

        when(testRunService.getRecentActivityByJobId(jobId, 10)).thenReturn(List.of());

        ChartResponseDTO result = performanceChartService.getApiPerformanceChart(jobId, 10);

        assertNotNull(result);
        assertEquals("API Performance", result.getTitle());
        assertEquals("runNumber", result.getXAxis());
        assertEquals(1, result.getSeries().size());
        assertEquals("Run Duration (ms)", result.getSeries().get(0).getName());
        assertEquals(0, result.getSeries().get(0).getData().size());

        verify(testRunService).getRecentActivityByJobId(jobId, 10);
    }

    @Test
    void getApiPerformanceChart_filtersOutFailedActivities() {
        UUID jobId = UUID.randomUUID();

        RecentActivityDTO passed = buildActivity(
                "Orders",
                "PASS",
                1200,
                Instant.parse("2025-01-01T10:00:00Z")
        );
        RecentActivityDTO failed = buildActivity(
                "Orders",
                "FAIL",
                900,
                Instant.parse("2025-01-01T11:00:00Z")
        );

        when(testRunService.getRecentActivityByJobId(jobId, 10))
                .thenReturn(List.of(passed, failed));

        ChartResponseDTO result = performanceChartService.getApiPerformanceChart(jobId, 10);

        List<ChartPointDTO> points = result.getSeries().get(0).getData();
        assertEquals(1, points.size());
        assertEquals("1", points.get(0).getLabel());
        assertEquals(1200, points.get(0).getValue());
    }

    @Test
    void getApiPerformanceChart_filtersOutZeroDurationActivities() {
        UUID jobId = UUID.randomUUID();

        RecentActivityDTO zeroDuration = buildActivity(
                "Orders",
                "PASS",
                0,
                Instant.parse("2025-01-01T10:00:00Z")
        );
        RecentActivityDTO valid = buildActivity(
                "Orders",
                "PASS",
                1500,
                Instant.parse("2025-01-01T11:00:00Z")
        );

        when(testRunService.getRecentActivityByJobId(jobId, 10))
                .thenReturn(List.of(zeroDuration, valid));

        ChartResponseDTO result = performanceChartService.getApiPerformanceChart(jobId, 10);

        List<ChartPointDTO> points = result.getSeries().get(0).getData();
        assertEquals(1, points.size());
        assertEquals("1", points.get(0).getLabel());
        assertEquals(1500, points.get(0).getValue());
    }

    @Test
    void getApiPerformanceChart_sortsByDateRunBeforeAssigningRunNumbers() {
        UUID jobId = UUID.randomUUID();

        RecentActivityDTO later = buildActivity(
                "Orders",
                "PASS",
                2000,
                Instant.parse("2025-01-03T10:00:00Z")
        );
        RecentActivityDTO earlier = buildActivity(
                "Orders",
                "PASS",
                1000,
                Instant.parse("2025-01-01T10:00:00Z")
        );
        RecentActivityDTO middle = buildActivity(
                "Orders",
                "PASS",
                1500,
                Instant.parse("2025-01-02T10:00:00Z")
        );

        when(testRunService.getRecentActivityByJobId(jobId, 10))
                .thenReturn(List.of(later, earlier, middle));

        ChartResponseDTO result = performanceChartService.getApiPerformanceChart(jobId, 10);

        List<ChartPointDTO> points = result.getSeries().get(0).getData();
        assertEquals(3, points.size());

        assertEquals("1", points.get(0).getLabel());
        assertEquals(1000, points.get(0).getValue());

        assertEquals("2", points.get(1).getLabel());
        assertEquals(1500, points.get(1).getValue());

        assertEquals("3", points.get(2).getLabel());
        assertEquals(2000, points.get(2).getValue());
    }

    @Test
    void getApiPerformanceChart_buildsTitleFromFirstSortedActivityTestName() {
        UUID jobId = UUID.randomUUID();

        RecentActivityDTO later = buildActivity(
                "Payments",
                "PASS",
                2200,
                Instant.parse("2025-01-02T10:00:00Z")
        );
        RecentActivityDTO earlier = buildActivity(
                "Payments",
                "PASS",
                1100,
                Instant.parse("2025-01-01T10:00:00Z")
        );

        when(testRunService.getRecentActivityByJobId(jobId, 10))
                .thenReturn(List.of(later, earlier));

        ChartResponseDTO result = performanceChartService.getApiPerformanceChart(jobId, 10);

        assertEquals("Payments API Performance", result.getTitle());
    }

    @Test
    void getApiPerformanceChart_usesDefaultTitleWhenAllActivitiesAreFilteredOut() {
        UUID jobId = UUID.randomUUID();

        RecentActivityDTO failed = buildActivity(
                "Payments",
                "FAIL",
                1000,
                Instant.parse("2025-01-01T10:00:00Z")
        );
        RecentActivityDTO zeroDuration = buildActivity(
                "Payments",
                "PASS",
                0,
                Instant.parse("2025-01-01T11:00:00Z")
        );

        when(testRunService.getRecentActivityByJobId(jobId, 10))
                .thenReturn(List.of(failed, zeroDuration));

        ChartResponseDTO result = performanceChartService.getApiPerformanceChart(jobId, 10);

        assertEquals("API Performance", result.getTitle());
        assertEquals(0, result.getSeries().get(0).getData().size());
    }

    @Test
    void getApiPerformanceChart_createsSingleSeriesWithExpectedMetadata() {
        UUID jobId = UUID.randomUUID();

        RecentActivityDTO activity = buildActivity(
                "Inventory",
                "PASS",
                1800,
                Instant.parse("2025-01-01T10:00:00Z")
        );

        when(testRunService.getRecentActivityByJobId(jobId, 5))
                .thenReturn(List.of(activity));

        ChartResponseDTO result = performanceChartService.getApiPerformanceChart(jobId, 5);

        assertEquals("Inventory API Performance", result.getTitle());
        assertEquals("runNumber", result.getXAxis());
        assertEquals(1, result.getSeries().size());

        ChartSeriesDTO series = result.getSeries().get(0);
        assertEquals("Run Duration (ms)", series.getName());
        assertEquals(1, series.getData().size());

        ChartPointDTO point = series.getData().get(0);
        assertEquals("1", point.getLabel());
        assertEquals(1800, point.getValue());
    }
}