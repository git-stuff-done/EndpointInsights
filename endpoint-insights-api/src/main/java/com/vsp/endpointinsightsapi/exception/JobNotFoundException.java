package com.vsp.endpointinsightsapi.exception;

import org.springframework.http.HttpStatus;

public class JobNotFoundException extends CustomException {

    public JobNotFoundException(String jobId) {
        super(HttpStatus.NOT_FOUND,
                new ErrorResponse("JOB_NOT_FOUND", "Job not found with ID: " + jobId, null));
    }
}
