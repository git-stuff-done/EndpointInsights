package com.vsp.endpointinsightsapi.service;

import com.vsp.endpointinsightsapi.dto.DashboardSummaryResponseDTO;
import com.vsp.endpointinsightsapi.dto.DashboardTestActivityDTO;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {
    public DashboardSummaryResponseDTO calculate(List<DashboardTestActivityDTO> tests) {
        if (tests == null || tests.isEmpty()) {
            return new DashboardSummaryResponseDTO(
                    0, 0, 0, 0.0, 0.0,
                    new EnumMap<>(DashboardTestActivityDTO.Status.class),
                    List.of()
            );
        }

        long total = tests.size();
        long passed = tests.stream().filter(t -> t.status() == DashboardTestActivityDTO.Status.PASS).count();
        long failed = tests.stream().filter(t -> t.status() == DashboardTestActivityDTO.Status.FAIL).count();

        double passRate = total == 0 ? 0.0 : (double) passed / total;

        double avgDuration = tests.stream()
                .mapToLong(DashboardTestActivityDTO::durationMs)
                .average()
                .orElse(0.0);

        Map<DashboardTestActivityDTO.Status, Long> byStatus = tests.stream()
                .collect(() -> new EnumMap<>(DashboardTestActivityDTO.Status.class),
                        (m, t) -> m.merge(t.status(), 1L, Long::sum),
                        EnumMap::putAll);

        // sort newest first and take top 10 (tweak as needed)
        List<DashboardTestActivityDTO> recent = tests.stream()
                .sorted(Comparator.comparing(DashboardTestActivityDTO::dateRun).reversed())
                .limit(10)
                .toList();

        return new DashboardSummaryResponseDTO(total, passed, failed, passRate, avgDuration, byStatus, recent);
    }
}
