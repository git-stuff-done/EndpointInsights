package com.vsp.endpointinsightsapi.service;

import com.vsp.endpointinsightsapi.dto.BatchResponseDTO;
import com.vsp.endpointinsightsapi.exception.BatchNotFoundException;
import com.vsp.endpointinsightsapi.model.TestBatch;
import com.vsp.endpointinsightsapi.repository.TestBatchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


import java.util.Optional;
import java.util.UUID;

@Service
public class BatchService {

    private final Logger LOG = LoggerFactory.getLogger(BatchService.class);
    private TestBatchRepository testBatchRepository = null;

    public BatchService(TestBatchRepository testBatchRepository) {
        this.testBatchRepository = testBatchRepository;
    }

    public BatchResponseDTO getBatchById(UUID batchId) {
        TestBatch b = testBatchRepository.findById(batchId)
                .orElseThrow(() -> {
                    LOG.debug("Batch {} not found", batchId);
                    return new BatchNotFoundException(batchId.toString());
                });
        return mapToDto(b);
    }

    public void deleteBatchById(UUID batchId) {
        if (!testBatchRepository.existsById(batchId)) {
            LOG.debug("Batch {} not found", batchId);
            throw new BatchNotFoundException(batchId.toString());
        }

        testBatchRepository.deleteById(batchId);
        LOG.info("Deleted batch {}", batchId);
    }


    private BatchResponseDTO mapToDto(TestBatch b) {
        return new BatchResponseDTO(
                b.getBatch_id(),
                b.getBatchName(),
                b.getScheduleId(),
                b.getStartTime(),
                b.getLastTimeRun(),
                b.getActive()
//                b.getJobs() Jobs table not created yet
        );
    }
}
