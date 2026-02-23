package com.vsp.endpointinsightsapi.runner;

import java.io.File;
import java.io.IOException;

public interface TestInterpreter {
	boolean processResults(File file) throws IOException;
}
