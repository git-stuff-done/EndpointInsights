package com.vsp.endpointinsightsapi.runner;

import com.vsp.endpointinsightsapi.model.TestRunResult;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public interface TestInterpreter {
	TestRunResult processResults(File file) throws IOException;
}
