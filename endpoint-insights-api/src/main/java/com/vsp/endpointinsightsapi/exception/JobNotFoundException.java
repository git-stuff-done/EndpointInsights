package com.vsp.endpointinsightsapi.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a job with the given ID does not exist in the database.
 */
public class JobNotFoundException extends CustomException {

    public JobNotFoundException(String jobId) {
        super(HttpStatus.NOT_FOUND,
                new ErrorResponse("JOB_NOT_FOUND", "Job not found with ID: " + jobId, null));
    }
}