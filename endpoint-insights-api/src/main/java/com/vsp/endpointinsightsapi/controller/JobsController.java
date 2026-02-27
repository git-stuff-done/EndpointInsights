package com.vsp.endpointinsightsapi.controller;
import com.vsp.endpointinsightsapi.dto.GitCheckoutResponse;
import com.vsp.endpointinsightsapi.model.*;

import com.vsp.endpointinsightsapi.authentication.PublicAPI;
import com.vsp.endpointinsightsapi.model.Job;
import com.vsp.endpointinsightsapi.model.JobCreateRequest;
import com.vsp.endpointinsightsapi.model.JobRun;
import com.vsp.endpointinsightsapi.model.JobRunHistory;
import com.vsp.endpointinsightsapi.model.entity.TestRun;
import com.vsp.endpointinsightsapi.model.enums.TestRunStatus;
import com.vsp.endpointinsightsapi.repository.TestRunRepository;
import com.vsp.endpointinsightsapi.runner.GitService;
import com.vsp.endpointinsightsapi.runner.JMeterCommandEnhancer;
import com.vsp.endpointinsightsapi.runner.JMeterInterpreterService;
import com.vsp.endpointinsightsapi.runner.JobRunnerThread;
import com.vsp.endpointinsightsapi.service.JobService;
import com.vsp.endpointinsightsapi.validation.ErrorMessages;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/jobs")
@Validated
public class JobsController {

	private final static Logger LOG = LoggerFactory.getLogger(JobsController.class);
	private final JobService jobService;

	private final JMeterInterpreterService jMeterInterpreterService;
	private final TestRunRepository testRunRepository;

    private final GitService gitService;
    private final JMeterCommandEnhancer jMeterCommandEnhancer;

	public JobsController(JobService jobService, JMeterInterpreterService jMeterInterpreterService, TestRunRepository testRunRepository, GitService gitService, JMeterCommandEnhancer jMeterCommandEnhancer) {
		this.jobService = jobService;
		this.jMeterInterpreterService = jMeterInterpreterService;
		this.testRunRepository = testRunRepository;
        this.gitService = gitService;
        this.jMeterCommandEnhancer = jMeterCommandEnhancer;
	}

	/**
	 * Endpoint to create a job.
	 *
	 * @param jobRequest the job details
	 * @return the created Job
	 * */
	@PostMapping
	 public ResponseEntity<Job> createJob(@RequestBody @Valid JobCreateRequest jobRequest) {
	 	try {
	 		Job job = jobService.createJob(jobRequest);
             //TODO: Sanitize user input `job`
	 		return new ResponseEntity<>(job, HttpStatus.CREATED);
	 	} catch (RuntimeException e) {
	 		LOG.error("Error creating job: {}", e.getMessage());
	 		return new ResponseEntity<>(null);
	 	}
	 }


	 @PostMapping("/{id}/run")
	 public ResponseEntity<TestRun> runJob(@PathVariable("id") UUID jobId) {
		LOG.info("Running job {}", jobId);
		var job = jobService.getJobById(jobId);
		if (job.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		 TestRun testRun = new TestRun();
		 testRun.setStartedAt(Instant.now());
		 testRun.setStatus(TestRunStatus.PENDING);
		 testRun.setJobId(job.get().getJobId());
		 testRun.setRunBy("system"); //todo: needs to be updated
		 testRun = testRunRepository.save(testRun);

		Thread t = new Thread(new JobRunnerThread(job.get(), testRun, testRunRepository, jMeterInterpreterService, gitService, jMeterCommandEnhancer));
		t.start();

		return ResponseEntity.ok(testRun);
	 }

	/**
	 * Endpoint to update a job resource.
	 *
	 * @param request the update request
	 * @param jobId	the jobId
	 * @return the updated Job
	 * */
	@PutMapping("/{id}")
	public ResponseEntity<Job> updateJob(
			@RequestBody
			@Valid
			Job request,
			@PathVariable("id")
			@NotNull(message = ErrorMessages.JOB_ID_REQUIRED)
			UUID jobId) {
		LOG.info("Updating job");

        Job savedJob = jobService.updateJob(jobId, request);

		return ResponseEntity.ok(savedJob);
	}

	/**
	 * Endpoint to get job list
	 * @return all job ids as a List of Strings
	 * */
	@GetMapping
	public ResponseEntity<List<Job>> getJobs() {
		return jobService.getAllJobs().map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	/**
	 * Endpoint to retrieve a job
	 *
	 * @param jobId the id of the job to be retrieved
	 * @return the Job with the given jobId
	 * */
	@GetMapping("/{id}")
	public ResponseEntity<Job> getJob(
			@PathVariable("id")
			@NotNull(message = ErrorMessages.JOB_ID_REQUIRED)
			String jobId) {
		
		try{
			UUID jobUuid = UUID.fromString(jobId);
			return jobService.getJobById(jobUuid).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());

		} catch(IllegalArgumentException e){
			LOG.error("Invalid UUID format for jobId: {}", jobId);
			return ResponseEntity.badRequest().build();
		}
	}

	/**
	 * Endpoint to delete a job
	 *
	 * @param jobId the id of the job to be deleted
	 * @return A status message indicating the job was deleted
	 * */
	@DeleteMapping("/{id}")
	public ResponseEntity<String> deleteJob(@PathVariable("id") UUID jobId) {
		try {
			jobService.deleteJobById(jobId);
			return ResponseEntity.ok(String.format("Job %s deleted", jobId));
		} catch (RuntimeException e) {
			return ResponseEntity.notFound().build();
		}
	}

	/**
	 * Endpoint to get the run history of a job
	 *
	 * @param jobId the id of the job to retrieve the history of
	 * @return A JobRunHistory object for the requested job
	 * */
	@GetMapping("/{id}/history")
	public ResponseEntity<JobRunHistory> getJobHistory(
			@PathVariable("id")
			@NotNull(message = ErrorMessages.JOB_ID_REQUIRED)
			String jobId) {
		// note to implementer: this is a great place to put some serious service level logic to aggregate data
		return ResponseEntity.ok(new JobRunHistory(List.of(new JobRun(UUID.fromString("1"), jobId))));
	}

	/**
	 * Endpoint to checkout the job repository.
	 *
	 * @param jobId the id of the job to checkout
	 * @return checkout information
	 * */
	@PostMapping("/{id}/checkout")
	public ResponseEntity<GitCheckoutResponse> checkoutJobRepository(
			@PathVariable("id")
			@NotNull(message = ErrorMessages.JOB_ID_REQUIRED)
			UUID jobId) {
		return ResponseEntity.ok(new GitCheckoutResponse(jobId, jobService.checkoutJobRepository(jobId)));
	}




}
