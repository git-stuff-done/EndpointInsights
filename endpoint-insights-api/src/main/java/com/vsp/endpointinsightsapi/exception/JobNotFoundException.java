package com.vsp.endpointinsightsapi.exception;

/**
 * Thrown when a job with the given ID does not exist in the database.
 */
public class JobNotFoundException extends RuntimeException {

    public JobNotFoundException(String message) {
        super(message);
    }
}