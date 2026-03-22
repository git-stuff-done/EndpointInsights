package com.vsp.endpointinsightsapi.factory;

import com.vsp.endpointinsightsapi.model.Job;
import com.vsp.endpointinsightsapi.model.TestBatch;
import com.vsp.endpointinsightsapi.model.entity.TestRun;
import com.vsp.endpointinsightsapi.model.enums.TestRunStatus;
import com.vsp.endpointinsightsapi.repository.TestRunRepository;
import com.vsp.endpointinsightsapi.util.CurrentUser;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class TestRunFactory {

	private final TestRunRepository testRunRepository;

	public TestRunFactory(TestRunRepository testRunRepository) {
		this.testRunRepository = testRunRepository;
	}

	/**
	 * Creates and persists a new {@link TestRun} associated with the given {@link Job},
	 * initializing it with the current timestamp and a pending status.
	 *
	 * @param job the job for which the test run is being created
	 * @return the persisted {@link TestRun} entity instance
	 */
	public TestRun createForJob(Job job) {
		TestRun testRun = new TestRun();
		testRun.setStartedAt(Instant.now());
		testRun.setStatus(TestRunStatus.PENDING);
		testRun.setJobId(job.getJobId());
		testRun.setRunBy(CurrentUser.getUserId());
		testRun = testRunRepository.save(testRun);

		return testRun;
	}

	/**
	 * Creates and persists a new {@link TestRun} associated with the given {@link TestBatch},
	 * initializing it with the current timestamp and a pending status.
	 *
	 * @param batch the test batch for which the test run is being created
	 * @return the persisted {@link TestRun} entity instance
	 */
	public TestRun createForBatch(TestBatch batch) {
		TestRun testRun = new TestRun();
		testRun.setStartedAt(Instant.now());
		testRun.setStatus(TestRunStatus.PENDING);
		testRun.setBatchId(batch.getBatchId());
		testRun.setRunBy(CurrentUser.getUserId());
		testRun = testRunRepository.save(testRun);

		return testRun;
	}

}
