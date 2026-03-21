package com.vsp.endpointinsightsapi.runner;

import com.vsp.endpointinsightsapi.factory.JobRunnerThreadFactory;
import com.vsp.endpointinsightsapi.model.BatchRunnerThreadStatus;
import com.vsp.endpointinsightsapi.model.Job;
import com.vsp.endpointinsightsapi.model.TestBatch;
import com.vsp.endpointinsightsapi.model.entity.TestRun;
import com.vsp.endpointinsightsapi.model.enums.TestRunStatus;
import com.vsp.endpointinsightsapi.repository.TestBatchRepository;
import com.vsp.endpointinsightsapi.repository.TestRunRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;

public class BatchRunnerThread implements Runnable {

	private static Logger LOG = LoggerFactory.getLogger(BatchRunnerThread.class);

	private TestBatch batch;
	private TestRun testRun;

	private final Consumer<BatchRunnerThreadStatus> onComplete;

	private final TestRunRepository testRunRepository;
	private final TestBatchRepository testBatchRepository;
	private final JobRunnerThreadFactory jobRunnerThreadFactory;


	public BatchRunnerThread(TestBatch batch,
							 TestRun testRun,
							 Consumer<BatchRunnerThreadStatus> onComplete,
							 TestRunRepository testRunRepository,
							 TestBatchRepository testBatchRepository,
							 JobRunnerThreadFactory jobRunnerThreadFactory) {
		this.batch = batch;
		this.testRun = testRun;
		this.onComplete = onComplete;

		this.testRunRepository = testRunRepository;
		this.testBatchRepository = testBatchRepository;
		this.jobRunnerThreadFactory = jobRunnerThreadFactory;
	}

	@Override
	public void run() {
		batch.setActive(true);
		batch.setStartTime(LocalDateTime.now());
		batch = testBatchRepository.save(batch);

		testRun.setStartedAt(Instant.now());
		testRun = testRunRepository.save(testRun);

		final Map<UUID, TestRunStatus> testRunMap = Collections.synchronizedMap(new HashMap<>());
		final List<Thread> joinThreads = new ArrayList<>();
		for (final Job job : batch.getJobs()) {
			Thread thread = jobRunnerThreadFactory.create(job, testRun, true, (status) -> {
				testRunMap.put(job.getJobId(), status.status());
			});

			joinThreads.add(thread);

			thread.start();
		}

		for (Thread thread : joinThreads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				LOG.error("Error joining thread with id {}: {}", thread.threadId(), e.getMessage());
				throw new RuntimeException(e);
			}
		}

		if (testRunMap.values().stream().anyMatch(status -> status != TestRunStatus.COMPLETED)) {
			LOG.error("Batch run (id={}) failed. Test run status: {}", batch.getBatchId(), testRunMap);
			onComplete.accept(new BatchRunnerThreadStatus(batch, testRun, TestRunStatus.FAILED));
			return;
		}

		LOG.info("Batch run (id={}) completed successfully.", batch.getBatchId());
		onComplete.accept(new BatchRunnerThreadStatus(batch, testRun, TestRunStatus.COMPLETED));
	}
}
