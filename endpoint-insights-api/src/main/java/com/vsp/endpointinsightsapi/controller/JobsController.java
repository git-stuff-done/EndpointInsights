package com.vsp.endpointinsightsapi.controller;
import com.vsp.endpointinsightsapi.model.*;
import com.vsp.endpointinsightsapi.service.JobService;
// import com.vsp.endpointinsightsapi.model.enums.JobStatus;
import com.vsp.endpointinsightsapi.validation.ErrorMessages;
import com.vsp.endpointinsightsapi.validation.Patterns;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

// import java.time.LocalDate;
// import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/jobs")
@Validated
@CrossOrigin(origins = "http://localhost:4200")
public class JobsController {

	private final static Logger LOG = LoggerFactory.getLogger(JobsController.class);
	private final JobService jobService;

	public JobsController(JobService jobService) {
		this.jobService = jobService;
	}

	/**
	 * Endpoint to create a job.
	 *
	 * @param request the job details
	 * @return the created Job
	 * */
	@PostMapping
	 public ResponseEntity<Job> createJob(@RequestBody @Valid Job jobRequest) {
	 	try {
	 		jobService.createJob(jobRequest);
	 		return new ResponseEntity<>(jobRequest, HttpStatus.CREATED);
	 	} catch (RuntimeException e) {
	 		LOG.error("Error creating job: {}", e.getMessage());
	 		return new ResponseEntity<>(null);
	 	}
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
		return ((Optional<List<Job>>) jobService.getAllJobs()).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
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




}
