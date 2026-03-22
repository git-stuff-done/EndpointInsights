package com.vsp.endpointinsightsapi.service;

import com.vsp.endpointinsightsapi.exception.JobNotFoundException;
import com.vsp.endpointinsightsapi.factory.JobRunnerThreadFactory;
import com.vsp.endpointinsightsapi.factory.TestRunFactory;
import com.vsp.endpointinsightsapi.model.Job;
import com.vsp.endpointinsightsapi.model.JobCreateRequest;
import com.vsp.endpointinsightsapi.model.entity.TestRun;
import com.vsp.endpointinsightsapi.model.enums.TestRunStatus;
import com.vsp.endpointinsightsapi.repository.JobRepository;
import com.vsp.endpointinsightsapi.repository.TestRunRepository;
import com.vsp.endpointinsightsapi.runner.JobRunnerThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class JobService {

    private static final Logger LOG = LoggerFactory.getLogger(JobService.class);
    private final JobRepository jobRepository;
    private final GitRepositoryService gitRepositoryService;
    private final TestRunRepository testRunRepository;
    private final JobRunnerThreadFactory jobRunnerThreadFactory;
    private final TestRunFactory testRunFactory;
    private final ThreadPoolTaskScheduler vspTaskScheduler;


    public JobService(JobRepository jobRepository,
                      GitRepositoryService gitRepositoryService,
                      TestRunRepository testRunRepository,
                      JobRunnerThreadFactory jobRunnerThreadFactory,
                      TestRunFactory testRunFactory,
                      ThreadPoolTaskScheduler vspTaskScheduler) {
        this.jobRepository = jobRepository;
        this.gitRepositoryService = gitRepositoryService;
		this.testRunRepository = testRunRepository;
        this.jobRunnerThreadFactory = jobRunnerThreadFactory;
        this.testRunFactory = testRunFactory;
        this.vspTaskScheduler = vspTaskScheduler;
    }

    public Job createJob(JobCreateRequest jobRequest) {
        Job job = new Job();
        job.setName(jobRequest.getName());
        job.setDescription(jobRequest.getDescription());
        job.setGitUrl(jobRequest.getGitUrl());
        job.setRunCommand(jobRequest.getRunCommand());
        job.setCompileCommand(jobRequest.getCompileCommand());
        job.setJmeterTestName(jobRequest.getJmeterTestName());
        job.setJobType(jobRequest.getTestType());
        job.setConfig(jobRequest.getConfig());
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
        existingJob.setJobType(job.getJobType());
        existingJob.setJmeterTestName(job.getJmeterTestName());
        existingJob.setConfig(job.getConfig());
        existingJob.setGitUrl(job.getGitUrl());
        existingJob.setGitAuthType(job.getGitAuthType());
        existingJob.setGitUsername(job.getGitUsername());
        existingJob.setGitPassword(job.getGitPassword());
        existingJob.setGitSshPrivateKey(job.getGitSshPrivateKey());
        existingJob.setGitSshPassphrase(job.getGitSshPassphrase());
        existingJob.setThreshold(job.getThreshold());
        return jobRepository.save(existingJob);
    }

    public Path checkoutJobRepository(UUID jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new JobNotFoundException(jobId.toString()));
        return gitRepositoryService.checkoutJobRepository(job);
    }

    /**
     * Initiates the execution of the specified job by creating a new test run, persisting its initial metadata,
     * and starting a background process to execute the job. The method also ensures that the test run status and
     * completion time are updated appropriately once the job completes through a callback.
     *
     * @param job the job to be executed
     * @return the TestRun entity representing the initiated run
     */
    public TestRun runJob(Job job) {
        TestRun testRun = testRunFactory.createForJob(job);

        // ignoring returned thread
		JobRunnerThread jobRunnerThread = jobRunnerThreadFactory.create(job, testRun, false, (status) -> {
            // Only setting final status here because in a batch I'll need to wait for all jobs to finish
            TestRun run = status.run();
            TestRunStatus s = status.status();
            LOG.info("Job {} run {} completed with status {}", job.getJobId(), run.getRunId(), s);
            run.setStatus(s);
            run.setFinishedAt(Instant.now());
            testRunRepository.save(run);
        });
        vspTaskScheduler.execute(jobRunnerThread);

        return testRun;
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
