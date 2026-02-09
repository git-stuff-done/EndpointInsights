package com.vsp.endpointinsightsapi.service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

//import java.util.Optional;
import com.vsp.endpointinsightsapi.model.Job;
import com.vsp.endpointinsightsapi.model.JobCreateRequest;
import com.vsp.endpointinsightsapi.model.enums.JobStatus;
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

    public Job createJob(Job job) {
        return jobRepository.save(job);
    }

    public Job createJob(JobCreateRequest jobRequest) {
        Job job = new Job();
        job.setJobId(UUID.randomUUID());
        job.setName(jobRequest.getName());
        job.setDescription(jobRequest.getDescription());
        //TODO: Validate git URL
        job.setGitUrl(jobRequest.getGitUrl());
        job.setRunCommand(jobRequest.getRunCommand());
        job.setCompileCommand(jobRequest.getCompileCommand());
        job.setJobType(jobRequest.getTestType());
        job.setConfig(jobRequest.getConfig());
        job.setStatus(JobStatus.PENDING);
        return jobRepository.save(job);
    }

    public Optional<List<Job>> getAllJobs() {
        if (jobRepository.findAll().isEmpty()) {
            LOG.debug("No jobs found in the repository");
            throw new JobNotFoundException("No jobs available");
        }
        return Optional.of(jobRepository.findAll());
    }

    public Optional<Job> getJobById(UUID jobId) {
        if(!jobRepository.existsById(jobId)) {
            LOG.debug("Job {} not found", jobId);
            throw new JobNotFoundException(jobId.toString());
        }
        return jobRepository.findById(jobId);

    }

    public Job updateJob(UUID id, Job job) {
        Job existingJob = jobRepository.findById(id)
                .orElseThrow(()  -> new JobNotFoundException(job.getJobId().toString()));
        existingJob.setName(job.getName());
        existingJob.setDescription(job.getDescription());
        existingJob.setStatus(job.getStatus());
        existingJob.setTestBatches(job.getTestBatches());
        existingJob.setJobType(job.getJobType());
        existingJob.setConfig(job.getConfig());
        return jobRepository.save(existingJob);
    }

    @Transactional
    public void deleteJobById(UUID jobId) {
        LOG.debug("Attempting to delete job with ID {}", jobId);

        if (!jobRepository.existsById(jobId)) {
            LOG.debug("Job {} not found", jobId);
            throw new JobNotFoundException(jobId.toString());
        }

        jobRepository.deleteById(jobId);
        LOG.debug("Job {} deleted successfully", jobId);
    }
}
