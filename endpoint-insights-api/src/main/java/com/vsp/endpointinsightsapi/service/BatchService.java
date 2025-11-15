package com.vsp.endpointinsightsapi.service;

import com.vsp.endpointinsightsapi.model.TestBatch;
import com.vsp.endpointinsightsapi.repository.TestBatchRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class BatchService {

    private final TestBatchRepository testBatchRepository;

    public BatchService(TestBatchRepository testBatchRepository) {
        this.testBatchRepository = testBatchRepository;
    }


    //Retrieve a batch by its ID from the repository.
    //Returns an Optional that may be empty if the batch is not found.
    public Optional<TestBatch> getBatchById(UUID id) {
        return testBatchRepository.findById(id);
    }
}
