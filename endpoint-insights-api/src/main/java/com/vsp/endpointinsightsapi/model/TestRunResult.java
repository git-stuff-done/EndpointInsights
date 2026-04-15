package com.vsp.endpointinsightsapi.model;

import com.vsp.endpointinsightsapi.model.entity.TestResult;

import java.util.List;
import java.util.UUID;

public record TestRunResult(boolean passed, UUID resultId, List<TestResult> testResults) {
}
