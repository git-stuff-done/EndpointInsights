package com.vsp.endpointinsightsapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
public class RecentActivityDTO {
    private String runId;
    private String jobId;
    private String batchId;
    private String testName;
    private String group;
    private Instant dateRun;
    private long durationMs;
    private String startedBy;
    private String status;
}
