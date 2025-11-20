package com.vsp.endpointinsightsapi.service;

import com.vsp.endpointinsightsapi.dto.BatchResponseDTO;
import com.vsp.endpointinsightsapi.exception.BatchNotFoundException;
import com.vsp.endpointinsightsapi.mapper.BatchMapper;
import java.util.List;
import java.util.UUID;

import com.vsp.endpointinsightsapi.dto.BatchRequestDTO;
import com.vsp.endpointinsightsapi.model.Job;
import com.vsp.endpointinsightsapi.model.TestBatch;
import com.vsp.endpointinsightsapi.repository.JobRepository;
import com.vsp.endpointinsightsapi.repository.TestBatchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.Objects;

@Service
public class BatchService {

    private static final Logger LOG = LoggerFactory.getLogger(BatchService.class);
    private final TestBatchRepository testBatchRepository;
    private final BatchMapper batchMapper;
    private final JobRepository jobRepository;

    public BatchService(TestBatchRepository testBatchRepository, BatchMapper batchMapper, JobRepository jobRepository) {
        this.testBatchRepository = Objects.requireNonNull(testBatchRepository, "testBatchRepository must not be null");
        this.batchMapper = Objects.requireNonNull(batchMapper, "batchMapper must not be null");
        this.jobRepository = Objects.requireNonNull(jobRepository, "jobRepository must not be null");
    }

    //Get Batch — used by GET /api/batches/{id}
    public BatchResponseDTO getBatchById(UUID batchId) {
        TestBatch b = testBatchRepository.findById(batchId)
                .orElseThrow(() -> {
                    LOG.debug("Batch {} not found", batchId);
                    return new BatchNotFoundException(batchId.toString());
                });
        return batchMapper.toDto(b);
    }

    //Delete Batch — used by DELETE /api/batches/{id}
    public void deleteBatchById(UUID batchId) {
        if (!testBatchRepository.existsById(batchId)) {
            LOG.debug("Batch {} not found", batchId);
            throw new BatchNotFoundException(batchId.toString());
        }

        testBatchRepository.deleteById(batchId);
        LOG.info("Deleted batch {}", batchId);

    }

    //Create Batch — used by POST /api/batches
    public TestBatch createBatch(BatchRequestDTO request) {
        TestBatch batch = new TestBatch();
        batch.setBatchName(request.getName());

        if (request.getJobIds() != null && !request.getJobIds().isEmpty()) {
            List<Job> jobs = jobRepository.findAllById(request.getJobIds());
            if (jobs.size() != request.getJobIds().size()) {
                LOG.warn("Job ID(s) in the request do not exist");
            }
            batch.setJobs(jobs);
        }
        return testBatchRepository.save(batch);
    }
}
