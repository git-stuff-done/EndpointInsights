package com.vsp.endpointinsightsapi.service;

import com.vsp.endpointinsightsapi.exception.JobNotFoundException;
import com.vsp.endpointinsightsapi.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JobService {

    private static final Logger LOG = LoggerFactory.getLogger(JobService.class);
    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Transactional
    public void deleteJobById(String jobId) {
        LOG.info("Attempting to delete job with ID {}", jobId);

        if (!jobRepository.existsById(jobId)) {
            LOG.warn("Job {} not found", jobId);
            throw new JobNotFoundException("Job not found: " + jobId);
        }

        jobRepository.deleteById(jobId);
        LOG.info("Job {} deleted successfully", jobId);
    }
}
