package com.vsp.endpointinsightsapi.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a batch with the given ID does not exist in the database.
 */
public class BatchNotFoundException extends CustomException {

    public BatchNotFoundException(String batchId) {
        super(HttpStatus.NOT_FOUND,
                new ErrorResponse("BATCH_NOT_FOUND", "Batch not found with ID: " + batchId, null));
    }
}
