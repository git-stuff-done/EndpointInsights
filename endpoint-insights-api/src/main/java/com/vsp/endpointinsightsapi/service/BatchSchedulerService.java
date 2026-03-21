package com.vsp.endpointinsightsapi.service;

import com.vsp.endpointinsightsapi.event.RunBatchEvent;
import com.vsp.endpointinsightsapi.model.TestBatch;
import com.vsp.endpointinsightsapi.repository.TestBatchRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
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
	private final ApplicationEventPublisher applicationEventPublisher;

	public BatchSchedulerService(TestBatchRepository testBatchRepository,
								 TaskScheduler taskScheduler,
								 ApplicationEventPublisher applicationEventPublisher) {
		this.testBatchRepository = testBatchRepository;
		this.taskScheduler = taskScheduler;
		this.applicationEventPublisher = applicationEventPublisher;
	}

	@PostConstruct
	public void scheduleBatches() {
		List<TestBatch> batches = testBatchRepository.findAll();
		for (TestBatch batch : batches) {
			scheduleBatch(batch);
		}
	}

	public void scheduleBatch(TestBatch batch) {
		ScheduledFuture<?> prevFuture = scheduledBatches.get(batch.getBatchId());
		if (prevFuture != null) {
			if (!prevFuture.cancel(false)) {
				LOG.warn("Failed to cancel previous scheduled batch {}", batch.getBatchId());
			}
		}

		if (ObjectUtils.isEmpty(batch.getCronExpression())) {
			LOG.info("Skipping batch {} as it has no schedule", batch.getBatchId());
			return;
		}
		try {
			LocalDateTime next = getNextRunTime(batch);
			ScheduledFuture<?> scheduled = taskScheduler.schedule(() -> startBatch(batch.getBatchId()),
					new CronTrigger(batch.getCronExpression()));
			scheduledBatches.put(batch.getBatchId(), scheduled);
			LOG.info("Scheduled batch {} for {} (cron='{}')", batch.getBatchId(), next, batch.getCronExpression());
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

		LOG.info("Starting batch {}", batch.getBatchId());

		if (batch.getActive() != null && batch.getActive()) {
			LOG.info("Batch {} is already active.", batchId);
			return;
		}

		// To avoid circular dependencies I'm using the event system
		applicationEventPublisher.publishEvent(new RunBatchEvent(this, batch));
		LOG.info("Next run for batch {} will be at {}", batch.getBatchId(), getNextRunTime(batch));
	}

	private LocalDateTime getNextRunTime(TestBatch batch) throws IllegalArgumentException {
		CronExpression cronExpression = CronExpression.parse(batch.getCronExpression());
		return cronExpression.next(LocalDateTime.now());
	}

}
