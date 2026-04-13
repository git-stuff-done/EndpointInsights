package com.vsp.endpointinsightsapi.service;

import com.vsp.endpointinsightsapi.dto.BatchRequestDTO;
import com.vsp.endpointinsightsapi.dto.BatchResponseDTO;
import com.vsp.endpointinsightsapi.event.RunBatchEvent;
import com.vsp.endpointinsightsapi.exception.BatchNotFoundException;
import com.vsp.endpointinsightsapi.exception.CustomExceptionBuilder;
import com.vsp.endpointinsightsapi.factory.BatchRunnerThreadFactory;
import com.vsp.endpointinsightsapi.factory.TestRunFactory;
import com.vsp.endpointinsightsapi.mapper.BatchMapper;
import com.vsp.endpointinsightsapi.model.Job;
import com.vsp.endpointinsightsapi.model.TestBatch;
import com.vsp.endpointinsightsapi.model.entity.BatchUpdateRequest;
import com.vsp.endpointinsightsapi.model.entity.TestBatchEmailList;
import com.vsp.endpointinsightsapi.model.entity.TestResult;
import com.vsp.endpointinsightsapi.model.entity.TestRun;
import com.vsp.endpointinsightsapi.model.enums.TestRunStatus;
import com.vsp.endpointinsightsapi.repository.JobRepository;
import com.vsp.endpointinsightsapi.repository.TestBatchEmailListsRepository;
import com.vsp.endpointinsightsapi.repository.TestBatchRepository;
import com.vsp.endpointinsightsapi.repository.TestRunRepository;
import com.vsp.endpointinsightsapi.runner.BatchRunnerThread;
import jakarta.transaction.Transactional;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@ConfigurationProperties("batch")
public class BatchService {

    private static final Logger LOG = LoggerFactory.getLogger(BatchService.class);
    private final TestBatchRepository testBatchRepository;
    private final BatchMapper batchMapper;
    private final JobRepository jobRepository;
    private final TestBatchEmailListsRepository testBatchEmailListsRepository;
    private final TestRunFactory testRunFactory;
    private final BatchRunnerThreadFactory batchRunnerThreadFactory;
    private final TestRunRepository testRunRepository;
    private final BatchRunPersistenceService batchRunPersistenceService;
    private final NotificationService notificationService;
    private final ThreadPoolTaskScheduler vspTaskScheduler;
    private final BatchSchedulerService batchSchedulerService;

    @Setter
    private int staleBatchThresholdSeconds = 60;

	@Autowired
	public BatchService(TestBatchRepository testBatchRepository,
                        BatchMapper batchMapper,
                        JobRepository jobRepository,
                        TestBatchEmailListsRepository testBatchEmailListsRepository,
                        TestRunFactory testRunFactory,
                        BatchRunnerThreadFactory batchRunnerThreadFactory,
                        TestRunRepository testRunRepository,
                        BatchRunPersistenceService batchRunPersistenceService,
                        NotificationService notificationService,
                        ThreadPoolTaskScheduler vspTaskScheduler,
                        BatchSchedulerService batchSchedulerService) {
		this.testBatchRepository = testBatchRepository;
		this.batchMapper = batchMapper;
		this.jobRepository = jobRepository;
		this.testBatchEmailListsRepository = testBatchEmailListsRepository;
		this.testRunFactory = testRunFactory;
		this.batchRunnerThreadFactory = batchRunnerThreadFactory;
		this.testRunRepository = testRunRepository;
		this.batchRunPersistenceService = batchRunPersistenceService;
		this.notificationService = notificationService;
		this.vspTaskScheduler = vspTaskScheduler;
		this.batchSchedulerService = batchSchedulerService;
	}


	//TODO fill with search criteria when filter implemented, change parameters as well
    public List<BatchResponseDTO> getAllBatchesByCriteria(String batchName, LocalDateTime runDate) {
        List<TestBatch> res = testBatchRepository.findAllByCriteria(batchName, runDate);
        return res.stream().map(batchMapper::toDto).toList();
    }

    //Get Batch — used by GET /api/batches/{id}
    public BatchResponseDTO getBatchById(UUID batchId) {
        TestBatch b = testBatchRepository.findByIdWithJobsAndUsers(batchId)
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
    @Transactional
    public TestBatch createBatch(BatchRequestDTO request) {
        TestBatch batch = new TestBatch();
        batch.setBatchName(request.getBatchName());

        if (request.getJobs() != null && !request.getJobs().isEmpty()) {
            List<Job> jobs = jobRepository.findAllById(request.getJobs());
            if (jobs.size() != request.getJobs().size()) {
                LOG.warn("One or more job ID(s) in the request do not exist");
            }
            batch.setJobs(jobs);
        }

        TestBatch saved = testBatchRepository.saveAndFlush(batch);

        if (request.getEmails() != null && !request.getEmails().isEmpty()) {
            updateEmailsForBatch(saved.getBatchId(), request.getEmails());
        }

        batchSchedulerService.scheduleBatch(saved);

        // Reload with user relationships for DTO mapping
        return testBatchRepository.findByIdWithJobsAndUsers(saved.getBatchId())
                .orElseThrow(() -> new CustomExceptionBuilder(HttpStatus.NOT_FOUND, "Batch not found after creation").build());
    }

    //Update Batch — used by PUT /api/batches/{id}
    @Transactional
    public TestBatch updateBatch(UUID id, BatchUpdateRequest request) {
        Optional<TestBatch> batchOptional = testBatchRepository.findById(id);
        if (batchOptional.isEmpty()) {
            throw new CustomExceptionBuilder(HttpStatus.NOT_FOUND, "Batch does not exist with id=" + id).build();
        }

        TestBatch batch = batchOptional.get();

        if (request.getCronExpression() != null) {
            batch.setCronExpression(request.getCronExpression());
        }

        if (request.getBatchName() != null) {
            batch.setBatchName(request.getBatchName());
        }

        if (request.getJobs() != null) {

            List<Job> jobs = jobRepository.findAllById(request.getJobs());
            if (jobs.size() != request.getJobs().size()) {
                throw new CustomExceptionBuilder(HttpStatus.BAD_REQUEST, "One or more job IDs not found").build();
            }
            batch.setJobs(new ArrayList<>(jobs));
        }

        testBatchRepository.saveAndFlush(batch);

        if (request.getEmails() != null) {
            updateEmailsForBatch(id, request.getEmails());
        }

        batchSchedulerService.scheduleBatch(batch);

        // Reload with user relationships for DTO mapping
        return testBatchRepository.findByIdWithJobsAndUsers(id)
                .orElseThrow(() -> new CustomExceptionBuilder(HttpStatus.NOT_FOUND, "Batch not found after update").build());
    }

    private List<String> getEmailsForBatch(UUID batchId) {
        return testBatchEmailListsRepository.findAllByBatchId(batchId).stream()
                .map(TestBatchEmailList::getEmail)
                .toList();
    }

    @Transactional
    public void updateEmailsForBatch(UUID batchId, List<String> emails) {
        testBatchEmailListsRepository.deleteAllByBatchId(batchId);
        List<TestBatchEmailList> existingEmails = testBatchEmailListsRepository.findAllByBatchId(batchId);
        System.out.println(existingEmails);
        List<TestBatchEmailList> entities = emails.stream()
                .collect(Collectors.toMap(
                        String::toLowerCase,
                        email -> email,
                        (existing, duplicate) -> existing
                ))
                .values()
                .stream()
                .map(email -> {
                    TestBatchEmailList entity = new TestBatchEmailList();
                    entity.setBatchId(batchId);
                    entity.setEmail(email);
                    return entity;
                })
                .toList();

        testBatchEmailListsRepository.saveAll(entities);
    }


    /**
     * This is called when a TestBatch is already in an active state.
     * This checks if the batch is in an error state and attempts to recover it in the db.
     * */
    private boolean isBatchStale(TestBatch batch) {
        // try to see if last run is stale or in error state to try and recover
        var batches = testRunRepository.findTop10ByBatchIdOrderByStartedAtDesc(batch.getBatchId());


        // No recent test runs, definitely stale
        if (batches.isEmpty()) {
            return true;
        }

        var inProgressRuns = batches.stream()
                .filter(run -> ObjectUtils.isEmpty(run.getFinishedAt()) || !List.of(TestRunStatus.COMPLETED, TestRunStatus.FAILED).contains(run.getStatus())).toList();

        // All Test Runs must be in a valid state, so definitely stale
        if (inProgressRuns.isEmpty()) {
            return true;
        }

        if (inProgressRuns.size() > 1) {
            // Mark all runs as failed
            inProgressRuns.forEach(run -> {
                run.setStatus(TestRunStatus.FAILED);
                run.setFinishedAt(Instant.now());
            });
            testRunRepository.saveAll(inProgressRuns);


            LOG.error("More than one in progress run found for batch {}", batch.getBatchId());
            LOG.error("Marking {} in progress runs as FAILED", inProgressRuns.size());
            LOG.error("This indicates a genuine error state, please investigate");

            return false;
        }

        if (batch.getLastTimeRun().isBefore(LocalDateTime.now().minusSeconds(staleBatchThresholdSeconds))) {
            LOG.error("Batch {} is in an error state and is stale. Recovering for next run.", batch.getBatchId());
            return true;
        }

        // Run is currently in progress, do not recover
        return false;
    }

    public TestRun runBatch(TestBatch batch) {
        if (batch.getActive() != null && batch.getActive()) {
            if (isBatchStale(batch)) {
                LOG.info("Batch {} is stale, proceeding anyways", batch.getBatchId());
            } else {
                throw new CustomExceptionBuilder(HttpStatus.CONFLICT, "Batch is already running").build();
            }
        }

        TestRun testRun = testRunFactory.createForBatch(batch);

		BatchRunnerThread batchRunnerThread = batchRunnerThreadFactory.create(batch, testRun, (status) -> {
            TestBatch returnedBatch = status.batch();
            TestRun run = status.run();
            TestRunStatus s = status.status();

            LOG.info("Batch {} run {} completed with status {}", batch.getBatchId(), run.getRunId(), s);

            returnedBatch.setActive(false);

            run.setStatus(s);
            run.setFinishedAt(Instant.now());

            // This call is in a separate service to ensure @Transactional works.
            // Spring's @Transactional only applies when method invocation occurs from outside the declaring class.
            batchRunPersistenceService.save(returnedBatch, run);

            // Notify now that batch is completed
            notificationService.sendTestCompletionNotifications(returnedBatch.getBatchName(),run.getBatchId(), run, null);
        });
        vspTaskScheduler.execute(batchRunnerThread);

        return testRun;
    }

    @EventListener
    public void handleRunBatchEvent(RunBatchEvent event) {
        LOG.info("Received RunBatchEvent for batch from scheduler {}", event.getBatch().getBatchId());
        runBatch(event.getBatch());
    }

}
