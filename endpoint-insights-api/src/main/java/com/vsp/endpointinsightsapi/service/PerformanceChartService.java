package com.vsp.endpointinsightsapi.service;

import com.vsp.endpointinsightsapi.dto.RecentActivityDTO;
import com.vsp.endpointinsightsapi.dto.charts.ChartPointDTO;
import com.vsp.endpointinsightsapi.dto.charts.ChartResponseDTO;
import com.vsp.endpointinsightsapi.dto.charts.ChartSeriesDTO;
import com.vsp.endpointinsightsapi.model.entity.TestRun;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class PerformanceChartService {

    private final TestRunService testRunService;

    public PerformanceChartService(TestRunService testRunService) {
        this.testRunService = testRunService;
    }

    public ChartResponseDTO getApiPerformanceChart(UUID jobId, int limit) {
        List<RecentActivityDTO> activities = testRunService.getRecentActivityByJobId(jobId, limit).stream()
                .filter(activity -> "PASS".equalsIgnoreCase(activity.getStatus()))
                .filter(activity -> activity.getDurationMs() > 0)
                .sorted(Comparator.comparing(RecentActivityDTO::getDateRun))
                .toList();

        List<ChartPointDTO> durationPoints = new ArrayList<>();

        int runNumber = 1;
        for (RecentActivityDTO activity : activities) {
            durationPoints.add(new ChartPointDTO(
                    String.valueOf(runNumber),
                    activity.getDurationMs()
            ));
            runNumber++;
        }

        String title = activities.isEmpty()
                ? "API Performance"
                : activities.get(0).getTestName() + " API Performance";

        return new ChartResponseDTO(
                title,
                "runNumber",
                List.of(new ChartSeriesDTO("Run Duration (ms)", durationPoints))
        );
    }
}