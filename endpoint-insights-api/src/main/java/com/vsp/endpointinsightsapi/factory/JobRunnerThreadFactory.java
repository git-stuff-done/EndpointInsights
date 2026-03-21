package com.vsp.endpointinsightsapi.factory;

import com.vsp.endpointinsightsapi.model.Job;
import com.vsp.endpointinsightsapi.model.JobRunnerThreadStatus;
import com.vsp.endpointinsightsapi.model.entity.TestRun;
import com.vsp.endpointinsightsapi.model.enums.TestRunStatus;
import com.vsp.endpointinsightsapi.repository.TestRunRepository;
import com.vsp.endpointinsightsapi.runner.GitService;
import com.vsp.endpointinsightsapi.runner.JMeterCommandService;
import com.vsp.endpointinsightsapi.runner.JMeterInterpreterService;
import com.vsp.endpointinsightsapi.runner.JobRunnerThread;
import com.vsp.endpointinsightsapi.service.NotificationService;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.function.Consumer;

@Component
public class JobRunnerThreadFactory {

	private final TestRunRepository testRunRepository;
	private final JMeterInterpreterService jMeterInterpreterService;
	private final NotificationService notificationService;
	private final GitService gitService;
	private final JMeterCommandService jMeterCommandService;

	public JobRunnerThreadFactory(TestRunRepository testRunRepository, JMeterInterpreterService jMeterInterpreterService, NotificationService notificationService, GitService gitService, JMeterCommandService jMeterCommandService) {
		this.testRunRepository = testRunRepository;
		this.jMeterInterpreterService = jMeterInterpreterService;
		this.notificationService = notificationService;
		this.gitService = gitService;
		this.jMeterCommandService = jMeterCommandService;
	}

	/**
	 * Creates and returns a new {@link Thread} configured to execute a {@link JobRunnerThread}
	 * for the given {@link Job} and {@link TestRun}. The thread will execute the job and,
	 * upon completion, invoke the specified callback with the job run status.
	 *
	 * @param job        the job to be executed in the new thread
	 * @param testRun    the test run entity associated with this job execution
	 * @param onComplete a {@link Consumer} that will be called with the status of the job runner upon completion
	 * @param isBatchRun true when the job is executed as part of a batch
	 * @return a new {@link Thread} ready to execute the job runner
	 */
	public JobRunnerThread create(Job job, TestRun testRun, boolean isBatchRun, Consumer<JobRunnerThreadStatus> onComplete) {
		return new JobRunnerThread(job,
				testRun,
				testRunRepository,
				jMeterInterpreterService,
				notificationService,
				gitService,
				jMeterCommandService,
				onComplete,
				isBatchRun);
	}

}
