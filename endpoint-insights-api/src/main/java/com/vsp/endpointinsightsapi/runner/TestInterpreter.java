package com.vsp.endpointinsightsapi.runner;

import com.vsp.endpointinsightsapi.model.TestRunResult;
import com.vsp.endpointinsightsapi.model.entity.TestRun;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public interface TestInterpreter {
	TestRunResult processResults(File file, UUID testRunId) throws IOException;
}
