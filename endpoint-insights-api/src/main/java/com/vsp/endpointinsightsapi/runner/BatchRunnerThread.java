package com.vsp.endpointinsightsapi.runner;

import com.vsp.endpointinsightsapi.model.BatchRunnerThreadStatus;
import com.vsp.endpointinsightsapi.model.TestBatch;
import com.vsp.endpointinsightsapi.model.entity.TestRun;
import com.vsp.endpointinsightsapi.model.enums.TestRunStatus;
import com.vsp.endpointinsightsapi.repository.TestBatchRepository;
import com.vsp.endpointinsightsapi.repository.TestRunRepository;
import com.vsp.endpointinsightsapi.service.NotificationService;

import java.time.LocalDateTime;
import java.util.function.Consumer;

public class BatchRunnerThread implements Runnable {

	private TestBatch batch;
	private TestRun testRun;

	private final Consumer<BatchRunnerThreadStatus> onComplete;

	private final TestRunRepository testRunRepository;
	private final NotificationService notificationService;
	private final TestBatchRepository testBatchRepository;



	public BatchRunnerThread(TestBatch batch, TestRun testRun, Consumer<BatchRunnerThreadStatus> onComplete, TestRunRepository testRunRepository, NotificationService notificationService, TestBatchRepository testBatchRepository) {
		this.batch = batch;
		this.testRun = testRun;
		this.onComplete = onComplete;

		this.testRunRepository = testRunRepository;
		this.notificationService = notificationService;
		this.testBatchRepository = testBatchRepository;
	}

	@Override
	public void run() {
		batch.setActive(true);
		batch.setStartTime(LocalDateTime.now());
		batch = testBatchRepository.save(batch);

		// todo: finish proper oncomplete
		onComplete.accept(new BatchRunnerThreadStatus(batch, testRun, TestRunStatus.COMPLETED));
	}
}
