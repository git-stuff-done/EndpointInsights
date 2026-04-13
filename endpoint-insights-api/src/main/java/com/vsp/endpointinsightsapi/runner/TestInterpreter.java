package com.vsp.endpointinsightsapi.runner;

import com.vsp.endpointinsightsapi.model.Job;
import com.vsp.endpointinsightsapi.model.TestRunResult;
import com.vsp.endpointinsightsapi.model.entity.TestRun;

import java.io.File;
import java.io.IOException;

public interface TestInterpreter {
	TestRunResult processResults(File file, TestRun testRunId, Job job) throws IOException;
}
