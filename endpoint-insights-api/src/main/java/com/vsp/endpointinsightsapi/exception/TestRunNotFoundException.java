package com.vsp.endpointinsightsapi.exception;

import org.springframework.http.HttpStatus;

/** Thrown when a test run ID is not found. */
public class TestRunNotFoundException extends CustomException {

    public TestRunNotFoundException(String runId) {
        super(HttpStatus.NOT_FOUND,
                new ErrorResponse("TEST_RUN_NOT_FOUND", "Test run not found with ID: " + runId, null));
    }
}
