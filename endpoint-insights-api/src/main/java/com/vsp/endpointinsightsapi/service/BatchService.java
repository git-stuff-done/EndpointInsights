package com.vsp.endpointinsightsapi.service;
import java.util.List;
import java.util.UUID;

import com.vsp.endpointinsightsapi.dto.BatchRequestDTO;
import com.vsp.endpointinsightsapi.model.TestBatch;
import com.vsp.endpointinsightsapi.repository.JobRepository;
import com.vsp.endpointinsightsapi.repository.TestBatchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.vsp.endpointinsightsapi.model.Job;

@Service
public class BatchService {

    private static final Logger LOG = LoggerFactory.getLogger(BatchService.class);
    private final TestBatchRepository batchRepository;
    private final JobRepository jobRepository;

    public BatchService(TestBatchRepository batchRepository, JobRepository jobRepository) {
        this.batchRepository = batchRepository;
        this.jobRepository = jobRepository;
    }

    // Create Batch
    public TestBatch createBatch(BatchRequestDTO request) {
        TestBatch batch = new TestBatch();
        batch.setBatchName(request.getName());
        if ((request.getJobIds() != null) && !request.getJobIds().isEmpty()) {
            List<Job> jobs = jobRepository.findAllById(request.getJobIds());
            if (jobs.size() != request.getJobIds().size()) {
                LOG.warn("Job ID(s) in the request do not exist");
            }
            batch.setJobs(jobs);
        }
        return batchRepository.save(batch);
    }
}


