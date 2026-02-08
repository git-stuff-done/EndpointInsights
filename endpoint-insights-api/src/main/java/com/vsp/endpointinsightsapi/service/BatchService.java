package com.vsp.endpointinsightsapi.service;

import com.vsp.endpointinsightsapi.dto.BatchResponseDTO;
import com.vsp.endpointinsightsapi.exception.BatchNotFoundException;
import com.vsp.endpointinsightsapi.exception.CustomExceptionBuilder;
import com.vsp.endpointinsightsapi.mapper.BatchMapper;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.vsp.endpointinsightsapi.dto.BatchRequestDTO;
import com.vsp.endpointinsightsapi.model.BatchUpdateRequest;
import com.vsp.endpointinsightsapi.model.Job;
import com.vsp.endpointinsightsapi.model.TestBatch;
import com.vsp.endpointinsightsapi.repository.JobRepository;
import com.vsp.endpointinsightsapi.repository.TestBatchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
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

    public TestBatch updateBatch(UUID id, BatchUpdateRequest request) {
		Optional<TestBatch> batchOptional = testBatchRepository.findById(id);
        if (batchOptional.isEmpty()) {
            throw new CustomExceptionBuilder(HttpStatus.NOT_FOUND, "Batch does not exist with id=" + id).build();
        }

		TestBatch batch = batchOptional.get();

        // handle adding jobs
        if (request.addJobs != null && !request.addJobs.isEmpty()) {
            for (UUID jobId : request.addJobs) {
				Optional<Job> job = jobRepository.findById(jobId);
                if (job.isEmpty()) {
                    throw new CustomExceptionBuilder(HttpStatus.BAD_REQUEST, "Job does not exist with id=" + jobId).build();
                }
                if (batch.getJobs().contains(job.get())) {
                    throw new CustomExceptionBuilder(HttpStatus.BAD_REQUEST, "Job already exists in batch with id=" + id).build();
                }
                batch.getJobs().add(job.get());
            }
        }

        // handle deleting jobs
        if (request.deleteJobs != null && !request.deleteJobs.isEmpty()) {
            for (UUID jobId : request.deleteJobs) {
				Optional<Job> job = jobRepository.findById(jobId);
                if (job.isEmpty()) {
                    throw new CustomExceptionBuilder(HttpStatus.BAD_REQUEST, "Job does not exist with id=" + jobId).build();
                }
                if (!batch.getJobs().contains(job.get())) {
                    throw new CustomExceptionBuilder(HttpStatus.BAD_REQUEST, "Job does not exist in batch with id=" + id).build();
                }
                batch.getJobs().remove(job.get());
            }
        }

        testBatchRepository.save(batch);
        return batch;
    }
}
