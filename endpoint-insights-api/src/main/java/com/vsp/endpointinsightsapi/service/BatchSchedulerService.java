package com.vsp.endpointinsightsapi.service;

import com.vsp.endpointinsightsapi.model.TestBatch;
import com.vsp.endpointinsightsapi.repository.TestBatchRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ScheduledFuture;

@Service
public class BatchSchedulerService {

	private final Logger LOG = LoggerFactory.getLogger(BatchSchedulerService.class);

	private final Map<UUID, ScheduledFuture<?>> scheduledBatches = Collections.synchronizedMap(new HashMap<>());
	private final TestBatchRepository testBatchRepository;
	private final TaskScheduler taskScheduler;
	private final BatchService batchService;

	public BatchSchedulerService(TestBatchRepository testBatchRepository, TaskScheduler taskScheduler, BatchService batchService) {
		this.testBatchRepository = testBatchRepository;
		this.taskScheduler = taskScheduler;
		this.batchService = batchService;
	}

	@PostConstruct
	public void scheduleBatches() {
		List<TestBatch> batches = testBatchRepository.findAll();
		for (TestBatch batch : batches) {
			scheduleBatch(batch);
		}
	}

	private void scheduleBatch(TestBatch batch) {
		ScheduledFuture<?> prevFuture = scheduledBatches.get(batch.getBatchId());
		if (prevFuture != null) {
			prevFuture.cancel(false);
		}


		if (ObjectUtils.isEmpty(batch.getCronExpression())) {
			LOG.info("Skipping batch {} as it has no schedule", batch.getBatchId());
			return;
		}
		try {
			CronExpression cronExpression = CronExpression.parse(batch.getCronExpression());
			LocalDateTime next = cronExpression.next(LocalDateTime.now());
			LOG.info("Scheduled batch {} for {} (cron='{}')", batch.getBatchId(), next, batch.getCronExpression());
			ScheduledFuture<?> scheduled = taskScheduler.schedule(() -> startBatch(batch.getBatchId()),
					new CronTrigger(batch.getCronExpression()));
			scheduledBatches.put(batch.getBatchId(), scheduled);
			LOG.info("Successfully scheduled batch {}", batch.getBatchId());
		} catch (IllegalArgumentException e) {
			LOG.error("Invalid cron expression for batch {}: {}", batch.getBatchId(), batch.getCronExpression());
		}
	}

	private void startBatch(UUID batchId) {
		TestBatch batch = testBatchRepository.findById(batchId).orElse(null);

		if (batch == null) {
			LOG.warn("Batch {} not found. It may have been deleted before the schedule could proceed.", batchId);
			return;
		}

		if (batch.getActive() != null && batch.getActive()) {
			LOG.info("Batch {} is already active.", batchId);
			scheduleBatch(batch);
			return;
		}

		LOG.info("Starting batch {}", batch.getBatchId());
		batchService.runBatch(batch);
		LOG.info("Batch {} started successfully", batch.getBatchId());
		LOG.info("Rescheduling next run for batch {}", batch.getBatchId());
		scheduleBatch(batch);
	}

}
