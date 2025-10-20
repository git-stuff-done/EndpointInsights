package com.vsp.endpointinsightsapi.controller;

import com.vsp.endpointinsightsapi.model.*;
import com.vsp.endpointinsightsapi.validation.ErrorMessages;
import com.vsp.endpointinsightsapi.validation.Patterns;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@Validated
public class JobsController {

	private final static Logger LOG = LoggerFactory.getLogger(JobsController.class);

	/**
	 * Endpoint to create a job.
	 *
	 * @param request the job details
	 * @return the created Job
	 * */
	@PostMapping
	public ResponseEntity<Job> createJob(@RequestBody @Valid JobCreateRequest request) {
		LOG.info("Creating job");
		return ResponseEntity.ok(new Job("1"));
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
			JobUpdateRequest request,
			@PathVariable("id")
			@NotNull(message = ErrorMessages.JOB_ID_REQUIRED)
			@Pattern(regexp = Patterns.JOB_ID_PATTERN, message = ErrorMessages.JOB_ID_INVALID_FORMAT)
			String jobId) {
		LOG.info("Updating job");
		return ResponseEntity.ok(new Job(jobId));
	}

	/**
	 * Endpoint to get job list
	 * @return all job ids as a List of Strings
	 * */
	@GetMapping
	public ResponseEntity<List<String>> getJobs() {
		return ResponseEntity.ok(List.of("1", "2", "5"));
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
			@Pattern(regexp = Patterns.JOB_ID_PATTERN, message = ErrorMessages.JOB_ID_INVALID_FORMAT)
			String jobId) {
		return ResponseEntity.ok(new Job(jobId));
	}

	/**
	 * Endpoint to delete a job
	 *
	 * @param jobId the id of the job to be deleted
	 * @return A status message indicating the job was deleted
	 * */
	@DeleteMapping("/{id}")
	public ResponseEntity<String> deleteJob(
			@PathVariable("id")
			@NotNull(message = ErrorMessages.JOB_ID_REQUIRED)
			@Pattern(regexp = Patterns.JOB_ID_PATTERN, message = ErrorMessages.JOB_ID_INVALID_FORMAT)
			String jobId) {
		return ResponseEntity.ok(String.format("Job %s deleted", jobId));
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
			@Pattern(regexp = Patterns.JOB_ID_PATTERN, message = ErrorMessages.JOB_ID_INVALID_FORMAT)
			String jobId) {
		// note to implementer: this is a great place to put some serious service level logic to aggregate data
		return ResponseEntity.ok(new JobRunHistory(List.of(new JobRun("1", jobId))));
	}




}
