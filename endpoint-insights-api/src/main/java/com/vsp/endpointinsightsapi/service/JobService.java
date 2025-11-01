package com.vsp.endpointinsightsapi.service;
import java.util.List;
import java.util.Optional;
import com.vsp.endpointinsightsapi.model.Job;
import com.vsp.endpointinsightsapi.exception.JobNotFoundException;
import com.vsp.endpointinsightsapi.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class JobService {

    private static final Logger LOG = LoggerFactory.getLogger(JobService.class);
    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public Job createJob(Job job) {
        return jobRepository.save(job);
    }

    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    public Optional<Job> getJobById(String jobId) {
        // if (!jobRepository.existsById(jobId)) {
        //     LOG.warn("Job {} not found", jobId);
        //     throw new JobNotFoundException("Job not found: " + jobId);
        // }
        return jobRepository.findById(jobId);
    }

}