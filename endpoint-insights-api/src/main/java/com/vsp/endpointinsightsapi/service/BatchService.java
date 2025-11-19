package com.vsp.endpointinsightsapi.service;

import com.vsp.endpointinsightsapi.dto.BatchResponseDTO;
import com.vsp.endpointinsightsapi.exception.BatchNotFoundException;
import com.vsp.endpointinsightsapi.mapper.BatchMapper;
import com.vsp.endpointinsightsapi.model.TestBatch;
import com.vsp.endpointinsightsapi.repository.TestBatchRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


import java.util.UUID;

@Service
public class BatchService {

    private final Logger LOG = LoggerFactory.getLogger(BatchService.class);
    private TestBatchRepository testBatchRepository = null;
    private final BatchMapper batchMapper;

    public BatchService(TestBatchRepository testBatchRepository, BatchMapper batchMapper) {
        this.testBatchRepository = testBatchRepository;
        this.batchMapper = batchMapper;
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
