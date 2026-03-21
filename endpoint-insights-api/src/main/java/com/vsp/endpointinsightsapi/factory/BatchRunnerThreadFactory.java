package com.vsp.endpointinsightsapi.factory;

import com.vsp.endpointinsightsapi.model.BatchRunnerThreadStatus;
import com.vsp.endpointinsightsapi.model.TestBatch;
import com.vsp.endpointinsightsapi.model.entity.TestRun;
import com.vsp.endpointinsightsapi.repository.TestBatchRepository;
import com.vsp.endpointinsightsapi.repository.TestRunRepository;
import com.vsp.endpointinsightsapi.runner.BatchRunnerThread;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class BatchRunnerThreadFactory {

	private final TestRunRepository testRunRepository;
	private final TestBatchRepository testBatchRepository;
	private final JobRunnerThreadFactory jobRunnerThreadFactory;

	public BatchRunnerThreadFactory(TestRunRepository testRunRepository, TestBatchRepository testBatchRepository, JobRunnerThreadFactory jobRunnerThreadFactory) {
		this.testRunRepository = testRunRepository;
		this.testBatchRepository = testBatchRepository;
		this.jobRunnerThreadFactory = jobRunnerThreadFactory;
	}

	/**
	 * Creates and returns a new {@link Thread} configured to execute a {@link BatchRunnerThread}
	 * for the given {@link TestBatch} and {@link TestRun}. The thread will execute the batch processing
	 * and, upon completion, invoke the specified callback with the resulting batch run status.
	 *
	 * @param batch      the test batch to be processed in the new thread
	 * @param testRun    the test run entity associated with this batch execution
	 * @param onComplete a {@link Consumer} to be called with the status of the batch runner upon completion
	 * @return a new {@link Thread} ready to execute the batch runner
	 */
	public Thread create(TestBatch batch, TestRun testRun, Consumer<BatchRunnerThreadStatus> onComplete) {
		return new Thread(new BatchRunnerThread(batch,
				testRun,
				onComplete,
				testRunRepository,
				testBatchRepository,
				jobRunnerThreadFactory));
	}

}
