package com.vsp.endpointinsightsapi.model;

import com.vsp.endpointinsightsapi.model.entity.TestRun;
import com.vsp.endpointinsightsapi.model.enums.TestRunStatus;

public record JobRunnerThreadStatus(TestRun run, TestRunStatus status) {
}
