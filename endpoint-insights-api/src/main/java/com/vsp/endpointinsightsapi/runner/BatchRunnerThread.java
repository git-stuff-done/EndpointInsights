package com.vsp.endpointinsightsapi.runner;

import com.vsp.endpointinsightsapi.model.BatchRunnerThreadStatus;
import com.vsp.endpointinsightsapi.model.TestBatch;
import com.vsp.endpointinsightsapi.model.entity.TestRun;
import com.vsp.endpointinsightsapi.repository.TestRunRepository;
import com.vsp.endpointinsightsapi.service.NotificationService;

import java.util.function.Consumer;

public class BatchRunnerThread implements Runnable {

	private TestBatch batch;
	private TestRun testRun;

	private final Consumer<BatchRunnerThreadStatus> onComplete;

	private final TestRunRepository testRunRepository;
	private final NotificationService notificationService;



	public BatchRunnerThread(TestBatch batch, TestRun testRun, Consumer<BatchRunnerThreadStatus> onComplete, TestRunRepository testRunRepository, NotificationService notificationService) {
		this.batch = batch;
		this.testRun = testRun;
		this.onComplete = onComplete;

		this.testRunRepository = testRunRepository;
		this.notificationService = notificationService;
	}

	@Override
	public void run() {

	}
}
