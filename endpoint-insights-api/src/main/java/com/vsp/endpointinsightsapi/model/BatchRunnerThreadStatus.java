package com.vsp.endpointinsightsapi.model;

import com.vsp.endpointinsightsapi.model.entity.TestResult;
import com.vsp.endpointinsightsapi.model.entity.TestRun;
import com.vsp.endpointinsightsapi.model.enums.TestRunStatus;

import java.util.List;
import java.util.UUID;

public record BatchRunnerThreadStatus(TestBatch batch, TestRun run, TestRunStatus status, List<TestResult> testRuns) {
}
