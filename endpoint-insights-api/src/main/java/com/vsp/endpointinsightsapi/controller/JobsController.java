package com.vsp.endpointinsightsapi.controller;

import com.vsp.endpointinsightsapi.dto.GitCheckoutResponse;
import com.vsp.endpointinsightsapi.exception.CustomExceptionBuilder;
import com.vsp.endpointinsightsapi.model.*;
import com.vsp.endpointinsightsapi.model.entity.TestRun;
import com.vsp.endpointinsightsapi.model.enums.TestType;
import com.vsp.endpointinsightsapi.service.JobService;
import com.vsp.endpointinsightsapi.validation.ErrorMessages;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/jobs")
@Validated
@Tag(name = "Jobs", description = "Job configuration, management, and execution endpoints")
public class JobsController {

	private final static Logger LOG = LoggerFactory.getLogger(JobsController.class);
	private final JobService jobService;

	public JobsController(JobService jobService) {
		this.jobService = jobService;
	}

	/**
	 * Endpoint to create a job.
	 *
	 * @param jobRequest the job details
	 * @return the created Job
	 * */
	@PostMapping
	@Operation(summary = "Create new job", description = "Creates a new performance test job with the specified configuration")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Job created successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid input - validation failed"),
			@ApiResponse(responseCode = "401", description = "Unauthorized")
	})
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
	 @Operation(summary = "Run job", description = "Executes a performance test job and creates a new test run")
	 @ApiResponses(value = {
	 		 @ApiResponse(responseCode = "200", description = "Job execution started"),
	 		 @ApiResponse(responseCode = "404", description = "Job not found"),
	 		 @ApiResponse(responseCode = "501", description = "Test type not supported"),
	 		 @ApiResponse(responseCode = "401", description = "Unauthorized")
	 })
	 public ResponseEntity<TestRun> runJob(
	 		 @Parameter(description = "Job ID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
	 		 @PathVariable("id") UUID jobId) {
		LOG.info("Running job {}", jobId);
		var job = jobService.getJobById(jobId);
		if (job.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		if (!TestType.PERF.equals(job.get().getJobType())) {
			throw new CustomExceptionBuilder(HttpStatus.NOT_IMPLEMENTED, "Test type is not supported at this time").build();
		}

		return ResponseEntity.ok(jobService.runJob(job.get()));
	 }

	/**
	 * Endpoint to update a job resource.
	 *
	 * @param request the update request
	 * @param jobId	the jobId
	 * @return the updated Job
	 * */
	@PutMapping("/{id}")
	@Operation(summary = "Update job", description = "Updates an existing job configuration")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Job updated successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid input"),
			@ApiResponse(responseCode = "404", description = "Job not found"),
			@ApiResponse(responseCode = "401", description = "Unauthorized")
	})
	public ResponseEntity<Job> updateJob(
			@RequestBody
			@Valid
			Job request,
			@Parameter(description = "Job ID", required = true)
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
	@Operation(summary = "List all jobs", description = "Retrieves a list of all performance test jobs")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Jobs retrieved successfully"),
			@ApiResponse(responseCode = "401", description = "Unauthorized")
	})
	public ResponseEntity<List<Job>> getJobs() {
		List<Job> j = jobService.getAllJobs().orElse(new ArrayList<>());
        return ResponseEntity.ok(j);
	}

	/**
	 * Endpoint to retrieve a job
	 *
	 * @param jobId the id of the job to be retrieved
	 * @return the Job with the given jobId
	 * */
	@GetMapping("/{id}")
	@Operation(summary = "Get job by ID", description = "Retrieves a specific job by its unique identifier")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Job found"),
			@ApiResponse(responseCode = "400", description = "Invalid job ID format"),
			@ApiResponse(responseCode = "404", description = "Job not found"),
			@ApiResponse(responseCode = "401", description = "Unauthorized")
	})
	public ResponseEntity<Job> getJob(
			@Parameter(description = "Job ID", required = true)
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
	@Operation(summary = "Delete job", description = "Permanently deletes a job by its ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "Job deleted successfully"),
			@ApiResponse(responseCode = "404", description = "Job not found"),
			@ApiResponse(responseCode = "401", description = "Unauthorized")
	})
	public ResponseEntity<Void> deleteJob(
			@Parameter(description = "Job ID", required = true)
			@PathVariable("id") UUID jobId) {
		jobService.deleteJobById(jobId);
		return ResponseEntity.noContent().build();
	}

	/**
	 * Endpoint to get the run history of a job
	 *
	 * @param jobId the id of the job to retrieve the history of
	 * @return A JobRunHistory object for the requested job
	 * */
	@GetMapping("/{id}/history")
	@Operation(summary = "Get job run history", description = "Retrieves the run history of a specific job")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Job history retrieved"),
			@ApiResponse(responseCode = "404", description = "Job not found"),
			@ApiResponse(responseCode = "401", description = "Unauthorized")
	})
	public ResponseEntity<JobRunHistory> getJobHistory(
			@Parameter(description = "Job ID", required = true)
			@PathVariable("id")
			@NotNull(message = ErrorMessages.JOB_ID_REQUIRED)
			String jobId) {
		// note to implementer: this is a great place t1 put some serious service level logic to aggregate data
		return ResponseEntity.ok(new JobRunHistory(List.of(new JobRun(UUID.fromString("1"), jobId))));
	}

	/**
	 * Endpoint to checkout the job repository.
	 *
	 * @param jobId the id of the job to checkout
	 * @return checkout information
	 * */
	@PostMapping("/{id}/checkout")
	@Operation(summary = "Checkout job repository", description = "Checks out the Git repository associated with a job")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Repository checked out successfully"),
			@ApiResponse(responseCode = "404", description = "Job not found"),
			@ApiResponse(responseCode = "401", description = "Unauthorized")
	})
	public ResponseEntity<GitCheckoutResponse> checkoutJobRepository(
			@Parameter(description = "Job ID", required = true)
			@PathVariable("id")
			@NotNull(message = ErrorMessages.JOB_ID_REQUIRED)
			UUID jobId) {
		return ResponseEntity.ok(new GitCheckoutResponse(jobId, jobService.checkoutJobRepository(jobId)));
	}

}
