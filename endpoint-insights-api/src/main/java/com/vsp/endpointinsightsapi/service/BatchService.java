package com.vsp.endpointinsightsapi.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.vsp.endpointinsightsapi.dto.BatchRequestDTO;
import com.vsp.endpointinsightsapi.model.Job;
import com.vsp.endpointinsightsapi.model.TestBatch;
import com.vsp.endpointinsightsapi.repository.JobRepository;
import com.vsp.endpointinsightsapi.repository.TestBatchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BatchService {

    private static final Logger LOG = LoggerFactory.getLogger(BatchService.class);

    private final TestBatchRepository batchRepository;
    private final JobRepository jobRepository;

    public BatchService(TestBatchRepository batchRepository, JobRepository jobRepository) {
        this.batchRepository = batchRepository;
        this.jobRepository = jobRepository;
    }

    //GET by id — used by BatchesController and its unit test
    public Optional<TestBatch> getBatchById(UUID id) {
        return batchRepository.findById(id);
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
        return batchRepository.save(batch);
    }
}
