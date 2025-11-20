package com.vsp.endpointinsightsapi.service;

import com.vsp.endpointinsightsapi.dto.BatchResponseDTO;
import com.vsp.endpointinsightsapi.exception.BatchNotFoundException;
import com.vsp.endpointinsightsapi.mapper.BatchMapper;
import com.vsp.endpointinsightsapi.model.TestBatch;
import com.vsp.endpointinsightsapi.repository.TestBatchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.Objects;
import java.util.UUID;

@Service
public class BatchService {

    private static final Logger LOG = LoggerFactory.getLogger(BatchService.class);
    private final TestBatchRepository testBatchRepository;
    private final BatchMapper batchMapper;

    public BatchService(TestBatchRepository testBatchRepository, BatchMapper batchMapper) {
        this.testBatchRepository = Objects.requireNonNull(testBatchRepository, "testBatchRepository must not be null");
        this.batchMapper = Objects.requireNonNull(batchMapper, "batchMapper must not be null");
    }

    public BatchResponseDTO getBatchById(UUID batchId) {
        TestBatch b = testBatchRepository.findById(batchId)
                .orElseThrow(() -> {
                    LOG.debug("Batch {} not found", batchId);
                    return new BatchNotFoundException(batchId.toString());
                });
        return batchMapper.toDto(b);
    }

    public void deleteBatchById(UUID batchId) {
        if (!testBatchRepository.existsById(batchId)) {
            LOG.debug("Batch {} not found", batchId);
            throw new BatchNotFoundException(batchId.toString());
        }

        testBatchRepository.deleteById(batchId);
        LOG.info("Deleted batch {}", batchId);
    }
}
